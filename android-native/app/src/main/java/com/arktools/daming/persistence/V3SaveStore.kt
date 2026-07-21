package com.arktools.daming.persistence

import android.content.Context
import com.arktools.daming.v3.data.V3GameState
import com.arktools.daming.v3.data.V3SaveEnvelope
import com.arktools.daming.v3.data.V3_SAVE_VERSION
import kotlinx.serialization.json.Json
import java.io.File

class V3SaveStore(context: Context) {
    private val saveDir = File(context.filesDir, "saves_v3")
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    enum class Slot(val fileName: String) {
        Auto("v3_auto.json")
    }

    init {
        saveDir.mkdirs()
    }

    fun save(state: V3GameState, slot: Slot = Slot.Auto): Boolean = runCatching {
        val envelope = V3SaveEnvelope(version = V3_SAVE_VERSION, timestamp = System.currentTimeMillis(), state = state)
        File(saveDir, slot.fileName).writeText(json.encodeToString(V3SaveEnvelope.serializer(), envelope))
        true
    }.getOrDefault(false)

    fun load(slot: Slot = Slot.Auto): V3GameState? = runCatching {
        val file = File(saveDir, slot.fileName)
        if (!file.exists()) return null
        val envelope = json.decodeFromString(V3SaveEnvelope.serializer(), file.readText())
        if (envelope.version != V3_SAVE_VERSION) return null
        envelope.state
    }.getOrNull()

    fun hasSave(slot: Slot = Slot.Auto): Boolean = load(slot) != null
}
