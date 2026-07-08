package com.daming.fushengzhi3.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.daming.fushengzhi3.data.BgmKey
import com.daming.fushengzhi3.data.GameAudioAssets
import com.daming.fushengzhi3.data.SfxKey
import java.io.File

class GameAudio(context: Context) {
    private val appContext = context.applicationContext
    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(audioAttributes)
        .build()
    private val sfxIds = mutableMapOf<SfxKey, Int>()
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgm: BgmKey? = null
    private var bgmVolume = 0.5f
    private var sfxVolume = 0.7f

    init {
        GameAudioAssets.sfx.forEach { (key, path) ->
            runCatching {
                sfxIds[key] = soundPool.load(materialize(path).absolutePath, 1)
            }
        }
    }

    fun playBgm(key: BgmKey) {
        if (currentBgm == key && bgmPlayer?.isPlaying == true) return
        val path = GameAudioAssets.bgm[key] ?: return
        stopBgm()
        val player = MediaPlayer()
        runCatching {
            player.setDataSource(materialize(path).absolutePath)
            player.setAudioAttributes(audioAttributes)
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
        val id = sfxIds[key] ?: return
        soundPool.play(id, sfxVolume, sfxVolume, 1, 0, 1f)
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
