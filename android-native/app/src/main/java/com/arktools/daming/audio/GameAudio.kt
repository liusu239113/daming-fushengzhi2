package com.arktools.daming.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.arktools.daming.data.BgmKey
import com.arktools.daming.data.GameAudioAssets
import com.arktools.daming.data.SfxKey
import java.io.File

class GameAudio(context: Context) {
    private val appContext = context.applicationContext
    private val bgmAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val sfxAudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(sfxAudioAttributes)
        .build()
    private val sfxIds = mutableMapOf<SfxKey, Int>()
    private val prefs = appContext.getSharedPreferences("game_audio", Context.MODE_PRIVATE)
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgm: BgmKey? = null
    private var bgmVolume = prefs.getFloat("bgm_volume", 0.5f)
    private var sfxVolume = prefs.getFloat("sfx_volume", 0.7f)
    private var suspended = false
    private var resumeBgmAfterSuspend = false

    val currentBgmVolume: Float
        get() = bgmVolume

    val currentSfxVolume: Float
        get() = sfxVolume

    init {
        GameAudioAssets.sfx.forEach { (key, path) ->
            runCatching {
                sfxIds[key] = soundPool.load(materialize(path).absolutePath, 1)
            }
        }
    }

    fun playBgm(key: BgmKey) {
        if (suspended) {
            currentBgm = key
            resumeBgmAfterSuspend = true
            return
        }
        if (currentBgm == key && bgmPlayer?.isPlaying == true) return
        val path = GameAudioAssets.bgm[key] ?: return
        stopBgm()
        val player = MediaPlayer()
        runCatching {
            player.setDataSource(materialize(path).absolutePath)
            player.setAudioAttributes(bgmAudioAttributes)
            player.isLooping = true
            player.setVolume(bgmVolume, bgmVolume)
            player.prepare()
            player.start()
            bgmPlayer = player
            currentBgm = key
        }.onFailure {
            player.release()
            currentBgm = null
        }
    }

    fun stopBgm() {
        bgmPlayer?.runCatchingStopAndRelease()
        bgmPlayer = null
        currentBgm = null
    }

    fun playSfx(key: SfxKey) {
        if (suspended || sfxVolume <= 0f) return
        val id = sfxIds[key] ?: return
        soundPool.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun setBgmVolume(value: Float) {
        bgmVolume = value.coerceIn(0f, 1f)
        prefs.edit().putFloat("bgm_volume", bgmVolume).apply()
        bgmPlayer?.setVolume(bgmVolume, bgmVolume)
    }

    fun setSfxVolume(value: Float) {
        sfxVolume = value.coerceIn(0f, 1f)
        prefs.edit().putFloat("sfx_volume", sfxVolume).apply()
    }

    fun suspendAudio() {
        if (suspended) return
        suspended = true
        resumeBgmAfterSuspend = bgmPlayer?.isPlaying == true
        runCatching { bgmPlayer?.pause() }
        soundPool.autoPause()
    }

    fun resumeAudio() {
        if (!suspended) return
        suspended = false
        soundPool.autoResume()
        if (resumeBgmAfterSuspend) {
            val player = bgmPlayer
            if (player != null) {
                runCatching { player.start() }
            } else {
                currentBgm?.let { playBgm(it) }
            }
        }
        resumeBgmAfterSuspend = false
    }

    fun click() = playSfx(SfxKey.UiClick)
    fun select() = playSfx(SfxKey.UiSelect)
    fun tabSwitch() = playSfx(SfxKey.UiTabSwitch)
    fun monthTick() = playSfx(SfxKey.MonthAdvance)

    fun release() {
        stopBgm()
        soundPool.release()
    }

    private fun materialize(path: String): File {
        val outFile = File(appContext.cacheDir, "game-audio/$path")
        if (outFile.exists() && outFile.length() > 0L) return outFile
        outFile.parentFile?.mkdirs()
        appContext.assets.open("game/assets/$path").use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        return outFile
    }

    private fun MediaPlayer.runCatchingStopAndRelease() {
        runCatching {
            if (isPlaying) stop()
        }
        release()
    }
}
