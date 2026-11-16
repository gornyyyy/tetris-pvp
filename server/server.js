const WebSocket = require('ws');

const PORT = process.env.PORT || 8080;

// --- Настройки защиты ---
const MAX_PAYLOAD_BYTES = 4096;            // 1) пакет > 4 КБ → разрыв (close 1009)
const RATE_LIMIT = 30;                     // 1) макс. 30 сообщений/сек на клиента
const RATE_WINDOW_MS = 1000;
const HEARTBEAT_MS = 30000;                // 2) пинг каждые 30 секунд
const EMPTY_ROOM_TTL_MS = 5 * 60 * 1000;   // 3) пустая комната живёт 5 минут

const wss = new WebSocket.Server({ port: PORT, maxPayload: MAX_PAYLOAD_BYTES });

const rooms = new Map();

// --- Логирование ---
function ts() {
    return new Date().toISOString().substring(11, 23); // HH:MM:SS.mmm
}
function log(...args) {
    console.log(`[${ts()}]`, ...args);
}
function logErr(...args) {
    console.error(`[${ts()}] ERROR`, ...args);
}
function roomInfo(roomId, room) {
    const p0 = room.players[0] ? `p1(${room.names[0]})` : '—';
    const p1 = room.players[1] ? `p2(${room.names[1]})` : '—';
    return `room=${roomId} [${p0} | ${p1}]`;
}

function send(ws, obj) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const payload = JSON.stringify(obj);
        log(`→ ${ws.slot ?? '?'} ${payload}`);
        ws.send(payload);
    }
}

function broadcast(room, obj, except = null) {
    for (const p of room.players) {
        if (p && p !== except) send(p, obj);
    }
}

function newRoom() {
    return {
        players: [null, null],
        names: [null, null],
        scores: [
            { score: 0, level: 1, lines: 0 },
            { score: 0, level: 1, lines: 0 },
        ],
        cleanupTimer: null,
    };
}

wss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    log(`+ client connected from ${ip}`);

    ws.roomId = null;
    ws.slot = null;

    // 2) Heartbeat: метка живости, pong ставит её обратно
    ws.isAlive = true;
    ws.on('pong', () => { ws.isAlive = true; });

    // 1) Rate limit: счётчик сообщений в текущем окне
    ws.rateCount = 0;
    ws.rateWindowStart = Date.now();
    ws.rateLimitNotified = false;

    ws.on('message', (raw) => {
        // 1) Rate limit проверяем ДО JSON.parse, чтобы флуд не нагружал парсер
        const now = Date.now();
        if (now - ws.rateWindowStart >= RATE_WINDOW_MS) {
            ws.rateWindowStart = now;
            ws.rateCount = 0;
            ws.rateLimitNotified = false;
        }
        if (++ws.rateCount > RATE_LIMIT) {
            if (!ws.rateLimitNotified) {
                ws.rateLimitNotified = true; // ошибку шлём 1 раз за окно, а не на каждый пакет
                logErr(`rate_limit from ${ws.slot ?? '?'} (${ip}): > ${RATE_LIMIT} msg/s`);
                send(ws, { type: 'error', message: 'rate_limit' });
            }
            return; // лишний пакет игнорируем
        }

        let msg;
        try {
            msg = JSON.parse(raw);
        } catch (e) {
            logErr(`invalid JSON from ${ip}: ${raw}`);
            return send(ws, { type: 'error', message: 'invalid_json' });
        }
        // JSON.parse может вернуть null/число/строку — без проверки msg.type уронит сервер
        if (!msg || typeof msg !== 'object') {
            logErr(`non-object JSON from ${ip}: ${raw}`);
            return send(ws, { type: 'error', message: 'invalid_json' });
        }

        log(`← ${ws.slot ?? '?'} ${raw}`);

        switch (msg.type) {
            case 'create_room': {
                const roomId = String(msg.roomId || '').trim().toUpperCase();
                if (!/^[A-Z0-9]{4,8}$/.test(roomId)) {
                    logErr(`invalid roomId "${roomId}" for create`);
                    return send(ws, { type: 'error', message: 'invalid_room_id' });
                }
                if (rooms.has(roomId)) {
                    logErr(`room ${roomId} already exists`);
                    return send(ws, { type: 'error', message: 'room_exists' });
                }
                const room = newRoom();
                // 3) Если за 5 минут никто не зашёл — удаляем комнату
                room.cleanupTimer = setTimeout(() => {
                    const r = rooms.get(roomId);
                    if (r && !r.players[0] && !r.players[1]) {
                        rooms.delete(roomId);
                        log(`- room ${roomId} removed (nobody joined in 5 min)`);
                    }
                }, EMPTY_ROOM_TTL_MS);
                rooms.set(roomId, room);
                log(`+ created empty room ${roomId} (by ${msg.name || 'unknown'}), ttl=5min`);
                send(ws, { type: 'room_created', roomId });
                break;
            }
            case 'join': {
                const roomId = String(msg.roomId || '').trim().toUpperCase();
                if (!/^[A-Z0-9]{4,8}$/.test(roomId)) {
                    logErr(`invalid roomId "${roomId}" from ${ip}`);
                    return send(ws, { type: 'error', message: 'invalid_room_id' });
                }
                const name = String(msg.name || 'Player').slice(0, 32);

                let room = rooms.get(roomId);
                if (!room) {
                    room = newRoom();
                    rooms.set(roomId, room);
                    log(`+ created room ${roomId}`);
                }

                if (room.players[0] && room.players[1]) {
                    logErr(`room ${roomId} is full, reject ${name}`);
                    return send(ws, { type: 'error', message: 'room_full' });
                }

                const slot = room.players[0] ? 1 : 0;
                room.players[slot] = ws;
                room.names[slot] = name;
                ws.roomId = roomId;
                ws.slot = slot;

                // 3) Первый игрок зашёл — отменяем автоудаление комнаты
                if (room.cleanupTimer) {
                    clearTimeout(room.cleanupTimer);
                    room.cleanupTimer = null;
                    log(`  room ${roomId}: cleanup timer cancelled (first join)`);
                }

                log(`  ${name} joined ${roomInfo(roomId, room)}`);

                const opponentSlot = 1 - slot;
                const opponentName = room.names[opponentSlot] || null;

                send(ws, {
                    type: 'joined',
                    roomId,
                    playerId: slot === 0 ? 'p1' : 'p2',
                    opponentConnected: !!room.players[opponentSlot],
                    opponentName,
                });

                const other = room.players[opponentSlot];
                if (other) {
                    send(other, {
                        type: 'joined',
                        roomId,
                        playerId: other.slot === 0 ? 'p1' : 'p2',
                        opponentConnected: true,
                        opponentName: name,
                    });
                    broadcast(room, { type: 'start' });
                    log(`  start match in ${roomId}`);
                }
                break;
            }

            case 'score_update': {
                const room = rooms.get(ws.roomId);
                if (!room) {
                    logErr(`score_update from ${ws.slot ?? '?'} without room`);
                    return;
                }
                room.scores[ws.slot] = {
                    score: msg.score | 0,
                    level: msg.level | 0,
                    lines: msg.lines | 0,
                };
                log(`  score ${ws.slot} = ${msg.score} (lvl ${msg.level}, lines ${msg.lines})`);
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
                if (!room) {
                    logErr(`line_clear from ${ws.slot ?? '?'} without room`);
                    return;
                }
                const garbage = Math.max(0, (msg.lines | 0) - 1);
                log(`  ${ws.slot} cleared ${msg.lines} lines → garbage ${garbage}`);
                if (garbage > 0) {
                    broadcast(room, { type: 'add_garbage', lines: garbage }, ws);
                }
                break;
            }

            case 'game_over': {
                const room = rooms.get(ws.roomId);
                if (!room) {
                    logErr(`game_over from ${ws.slot ?? '?'} without room`);
                    return;
                }
                log(`  ${ws.slot} game over`);
                broadcast(room, { type: 'opponent_game_over' }, ws);
                break;
            }

            case 'win': {
                const room = rooms.get(ws.roomId);
                if (!room) {
                    logErr(`win from ${ws.slot ?? '?'} without room`);
                    return;
                }
                log(`  ${ws.slot} win`);
                broadcast(room, { type: 'opponent_win' }, ws);
                break;
            }

            case 'leave': {
                log(`  ${ws.slot ?? '?'} leave`);
                handleLeave(ws);
                break;
            }

            default:
                logErr(`unknown type "${msg.type}" from ${ws.slot ?? '?'}`);
                send(ws, { type: 'error', message: 'unknown_type' });
        }
    });

    ws.on('close', (code, reason) => {
        log(`- client disconnected: ${ws.slot ?? '?'} code=${code} reason=${reason || '—'}`);
        handleLeave(ws);
    });

    ws.on('error', (err) => {
        logErr(`websocket error: ${err.message}`);
        handleLeave(ws);
    });
});

function handleLeave(ws) {
    const room = rooms.get(ws.roomId);
    if (!room) return; // повторный вызов (error + close) безопасен
    if (ws.slot !== null) {
        room.players[ws.slot] = null;
        room.names[ws.slot] = null;
    }
    log(`  ${ws.slot ?? '?'} left ${roomInfo(ws.roomId, room)}`);
    broadcast(room, { type: 'opponent_disconnected' });
    if (!room.players[0] && !room.players[1]) {
        if (room.cleanupTimer) clearTimeout(room.cleanupTimer);
        rooms.delete(ws.roomId);
        log(`- room ${ws.roomId} removed (empty)`);
    }
    ws.roomId = null;
    ws.slot = null;
}

// 2) Heartbeat: раз в 30 сек пингуем всех, кто не ответил с прошлого раза — terminate
const heartbeat = setInterval(() => {
    for (const ws of wss.clients) {
        if (ws.isAlive === false) {
            log(`  heartbeat: no pong from ${ws.slot ?? '?'} → terminate`);
            ws.terminate(); // вызовет 'close' → handleLeave(ws) освободит слот
            continue;
        }
        ws.isAlive = false;
        if (ws.readyState === WebSocket.OPEN) ws.ping();
    }
}, HEARTBEAT_MS);

wss.on('close', () => clearInterval(heartbeat));

log(`PvP server listening on ws://0.0.0.0:${PORT}`);