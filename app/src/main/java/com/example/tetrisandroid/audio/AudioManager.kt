package com.example.tetrisandroid.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.tetrisandroid.R
import com.example.tetrisandroid.data.AudioSettings
import kotlin.random.Random

class GameAudioManager(
    private val context: Context,
    private val audioSettings: AudioSettings
) {
    private var musicPlayer: MediaPlayer? = null
    private var dialogPlayer: MediaPlayer? = null

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()

    // Короткие SFX
    private var sfx4Lines = 0
    private var sfx123Lines = 0
    private var sfxLvlUp = 0
    private var sfxGarbage = 0
    private var sfxWin = 0
    private var sfxLose = 0

    private var currentMenuTrackIndex = 0
    private val menuTracks = listOf(R.raw.music_menu, R.raw.music_menu_2)

    private var currentGameTrack: Int = 0
    private var currentGameLevel: Int = 1
    private var isGameMusicMode = false

    init {
        loadSounds()
    }

    private fun loadSounds() {
        sfx4Lines = soundPool.load(context, R.raw.sfx_4lines, 1)
        sfx123Lines = soundPool.load(context, R.raw.sfx_123lines, 1)
        sfxLvlUp = soundPool.load(context, R.raw.sfx_lvl_up, 1)
        sfxGarbage = soundPool.load(context, R.raw.sfx_garbage_line, 1)
        sfxWin = soundPool.load(context, R.raw.sfx_win, 1)
        sfxLose = soundPool.load(context, R.raw.sfx_lose, 1)
    }

    private fun calculateSfxVolume(): Float {
        if (!audioSettings.isSoundEnabled) return 0f
        return audioSettings.masterVolume * audioSettings.sfxVolume
    }

    private fun calculateMusicVolume(): Float {
        if (!audioSettings.isSoundEnabled || !audioSettings.isMusicEnabled) return 0f
        return audioSettings.masterVolume * audioSettings.musicVolume
    }

    private fun playMusicTrack(resId: Int) {
        stopMusic()
        musicPlayer = MediaPlayer.create(context, resId)?.apply {
            setVolume(calculateMusicVolume(), calculateMusicVolume())
            setOnCompletionListener {
                if (isGameMusicMode) playNextGameTrack() else playNextMenuTrack()
            }
            start()
        }
    }

    fun stopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

    fun pauseMusic() {
        if (musicPlayer?.isPlaying == true) musicPlayer?.pause()
    }

    fun resumeMusic() {
        musicPlayer?.start()
    }

    fun updateMusicVolume() {
        musicPlayer?.setVolume(calculateMusicVolume(), calculateMusicVolume())
        dialogPlayer?.setVolume(calculateMusicVolume(), calculateMusicVolume())
    }

    fun playRandomMenuMusic() {
        isGameMusicMode = false
        currentMenuTrackIndex = Random.nextInt(menuTracks.size)
        playMusicTrack(menuTracks[currentMenuTrackIndex])
    }

    private fun playNextMenuTrack() {
        if (!isGameMusicMode) {
            currentMenuTrackIndex = (currentMenuTrackIndex + 1) % menuTracks.size
            playMusicTrack(menuTracks[currentMenuTrackIndex])
        }
    }

    fun startGameMusic(level: Int) {
        isGameMusicMode = true
        currentGameLevel = level
        if (musicPlayer?.isPlaying == true) return
        if (musicPlayer != null) {
            resumeMusic()
        } else {
            currentGameTrack = getGameTrackForLevel(level)
            playMusicTrack(currentGameTrack)
        }
    }

    fun updateGameLevel(level: Int) {
        currentGameLevel = level
    }

    private fun playNextGameTrack() {
        if (isGameMusicMode) {
            currentGameTrack = getGameTrackForLevel(currentGameLevel)
            playMusicTrack(currentGameTrack)
        }
    }

    private fun getGameTrackForLevel(level: Int): Int {
        return when (level) {
            in 1..7 -> R.raw.music_playtime_slow
            in 8..19 -> R.raw.music_playtime_mid
            else -> R.raw.music_playtime
        }
    }

    fun playDialogMusic(isWin: Boolean, isHighScore: Boolean) {
        dialogPlayer?.release()
        val track = when {
            isWin -> R.raw.music_gameover_highscore_update
            isHighScore -> R.raw.music_gameover_highscore_update
            else -> R.raw.music_gameover_dialog
        }
        dialogPlayer = MediaPlayer.create(context, track)?.apply {
            setVolume(calculateMusicVolume(), calculateMusicVolume())
            start()
        }
    }

    fun stopDialogSound() {
        dialogPlayer?.stop()
        dialogPlayer?.release()
        dialogPlayer = null
    }

    // --- SFX (короткие) ---
    fun playSfx(soundId: Int) {
        val volume = calculateSfxVolume()
        if (volume > 0f) soundPool.play(soundId, volume, volume, 1, 0, 1f)
    }

    fun playLineClear(lines: Int) {
        if (lines == 4) playSfx(sfx4Lines) else playSfx(sfx123Lines)
    }

    fun playLevelUp() = playSfx(sfxLvlUp)
    fun playGarbageLine() = playSfx(sfxGarbage)
    fun playWinSfx() = playSfx(sfxWin)
    fun playLoseSfx() = playSfx(sfxLose)

    fun release() {
        stopMusic()
        stopDialogSound()
        soundPool.release()
    }
}