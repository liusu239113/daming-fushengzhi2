package com.daming.fushengzhi2.persistence

import android.content.Context
import com.daming.fushengzhi2.data.GameState
import com.daming.fushengzhi2.data.SAVE_VERSION
import com.daming.fushengzhi2.data.SaveEnvelope
import kotlinx.serialization.json.Json
import java.io.File

class SaveStore(context: Context) {
    private val saveDir = File(context.filesDir, "saves")
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    enum class Slot(val fileName: String, val label: String) {
        Auto("auto.json", "自动存档"),
        Manual("manual.json", "手动存档")
    }

    init {
        saveDir.mkdirs()
    }

    fun save(state: GameState, slot: Slot = Slot.Auto): Boolean = runCatching {
        val envelope = SaveEnvelope(version = SAVE_VERSION, timestamp = System.currentTimeMillis(), state = state)
        File(saveDir, slot.fileName).writeText(json.encodeToString(SaveEnvelope.serializer(), envelope))
        true
    }.getOrDefault(false)

    fun load(slot: Slot): GameState? = runCatching {
        val file = File(saveDir, slot.fileName)
        if (!file.exists()) return null
        val envelope = json.decodeFromString(SaveEnvelope.serializer(), file.readText())
        if (envelope.version > SAVE_VERSION) return null
        envelope.state
    }.getOrNull()

    fun hasSave(slot: Slot): Boolean = File(saveDir, slot.fileName).exists()

    fun latestSlot(): Slot? {
        val candidates = Slot.entries
            .mapNotNull { slot -> File(saveDir, slot.fileName).takeIf { it.exists() }?.let { slot to it.lastModified() } }
        return candidates.maxByOrNull { it.second }?.first
    }

    fun loadLatest(): GameState? = latestSlot()?.let { load(it) }

    fun delete(slot: Slot): Boolean = File(saveDir, slot.fileName).delete()
}
