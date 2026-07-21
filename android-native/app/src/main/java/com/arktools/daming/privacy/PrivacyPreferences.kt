package com.arktools.daming.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.privacyDataStore by preferencesDataStore(name = "privacy_preferences")

class PrivacyPreferences(private val context: Context) {
    private companion object {
        val Accepted = booleanPreferencesKey("privacy_accepted_v1")
    }

    val accepted: Flow<Boolean> = context.privacyDataStore.data.map { preferences ->
        preferences[Accepted] ?: false
    }

    suspend fun accept() {
        context.privacyDataStore.edit { it[Accepted] = true }
    }

    suspend fun revoke() {
        context.privacyDataStore.edit { it.remove(Accepted) }
    }
}
