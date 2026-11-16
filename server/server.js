const WebSocket = require('ws');

const PORT = process.env.PORT || 8080;
const wss = new WebSocket.Server({ port: PORT });

const rooms = new Map();

function send(ws, obj) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(obj));
    }
}

function broadcast(room, obj, except = null) {
    for (const p of room.players) {
        if (p && p !== except) send(p, obj);
    }
}

wss.on('connection', (ws) => {
    ws.roomId = null;
    ws.slot = null;

    ws.on('message', (raw) => {
        let msg;
        try { msg = JSON.parse(raw); } catch { return; }

        switch (msg.type) {
            case 'join': {
                const roomId = String(msg.roomId || '').trim();
                if (!roomId) return send(ws, { type: 'error', message: 'no_room_id' });

                let room = rooms.get(roomId);
                if (!room) {
                    room = {
                        players: [null, null],
                        scores: [
                            { score: 0, level: 1, lines: 0 },
                            { score: 0, level: 1, lines: 0 },
                        ],
                    };
                    rooms.set(roomId, room);
                }

                if (room.players[0] && room.players[1]) {
                    return send(ws, { type: 'error', message: 'room_full' });
                }

                const slot = room.players[0] ? 1 : 0;
                room.players[slot] = ws;
                ws.roomId = roomId;
                ws.slot = slot;

                send(ws, {
                    type: 'joined',
                    roomId,
                    playerId: slot === 0 ? 'p1' : 'p2',
                    opponentConnected: !!room.players[1 - slot],
                });

                const other = room.players[1 - slot];
                if (other) {
                    send(other, {
                        type: 'joined',
                        roomId,
                        playerId: other.slot === 0 ? 'p1' : 'p2',
                        opponentConnected: true,
                    });
                    broadcast(room, { type: 'start' });
                }
                break;
            }

            case 'score_update': {
                const room = rooms.get(ws.roomId);
                if (!room) return;
                room.scores[ws.slot] = {
                    score: msg.score | 0,
                    level: msg.level | 0,
                    lines: msg.lines | 0,
                };
                broadcast(room, {
                    type: 'opponent_score',
                    score: msg.score | 0,
                    level: msg.level | 0,
                    lines: msg.lines | 0,
                }, ws);
                break;
            }

            case 'line_clear': {
                const room = rooms.get(ws.roomId);
                if (!room) return;
                const garbage = Math.max(0, (msg.lines | 0) - 1);
                if (garbage > 0) {
                    broadcast(room, { type: 'add_garbage', lines: garbage }, ws);
                }
                break;
            }

            case 'game_over': {
                const room = rooms.get(ws.roomId);
                if (!room) return;
                broadcast(room, { type: 'opponent_game_over' }, ws);
                break;
            }

            case 'win': {
                const room = rooms.get(ws.roomId);
                if (!room) return;
                broadcast(room, { type: 'opponent_win' }, ws);
                break;
            }

            case 'leave': {
                handleLeave(ws);
                break;
            }

            default:
                send(ws, { type: 'error', message: 'unknown_type' });
        }
    });

    ws.on('close', () => handleLeave(ws));
    ws.on('error', () => handleLeave(ws));
});

function handleLeave(ws) {
    const room = rooms.get(ws.roomId);
    if (!room) return;
    if (ws.slot !== null) room.players[ws.slot] = null;
    broadcast(room, { type: 'opponent_disconnected' });
    if (!room.players[0] && !room.players[1]) {
        rooms.delete(ws.roomId);
    }
    ws.roomId = null;
    ws.slot = null;
}

console.log(`PvP server listening on ws://0.0.0.0:${PORT}`);