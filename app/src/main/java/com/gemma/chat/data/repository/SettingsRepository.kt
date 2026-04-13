package com.gemma.chat.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gemma_settings")

data class AppSettings(
    val modelPath: String = "",
    val temperature: Float = 0.8f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxTokens: Int = 2048,
    val systemPrompt: String = "",
    val isDarkTheme: Boolean = false,
    val useDynamicColor: Boolean = true,
    val streamingEnabled: Boolean = true,
    val contextWindowSize: Int = 10 // number of prior exchanges to include
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val MODEL_PATH = stringPreferencesKey("model_path")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val TOP_K = intPreferencesKey("top_k")
        val TOP_P = floatPreferencesKey("top_p")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val STREAMING_ENABLED = booleanPreferencesKey("streaming_enabled")
        val CONTEXT_WINDOW_SIZE = intPreferencesKey("context_window_size")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            modelPath = prefs[Keys.MODEL_PATH] ?: "",
            temperature = prefs[Keys.TEMPERATURE] ?: 0.8f,
            topK = prefs[Keys.TOP_K] ?: 40,
            topP = prefs[Keys.TOP_P] ?: 0.95f,
            maxTokens = prefs[Keys.MAX_TOKENS] ?: 2048,
            systemPrompt = prefs[Keys.SYSTEM_PROMPT] ?: "",
            isDarkTheme = prefs[Keys.IS_DARK_THEME] ?: false,
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: true,
            streamingEnabled = prefs[Keys.STREAMING_ENABLED] ?: true,
            contextWindowSize = prefs[Keys.CONTEXT_WINDOW_SIZE] ?: 10
        )
    }

    suspend fun saveModelPath(path: String) {
        context.dataStore.edit { it[Keys.MODEL_PATH] = path }
    }

    suspend fun saveTemperature(value: Float) {
        context.dataStore.edit { it[Keys.TEMPERATURE] = value }
    }

    suspend fun saveTopK(value: Int) {
        context.dataStore.edit { it[Keys.TOP_K] = value }
    }

    suspend fun saveTopP(value: Float) {
        context.dataStore.edit { it[Keys.TOP_P] = value }
    }

    suspend fun saveMaxTokens(value: Int) {
        context.dataStore.edit { it[Keys.MAX_TOKENS] = value }
    }

    suspend fun saveSystemPrompt(value: String) {
        context.dataStore.edit { it[Keys.SYSTEM_PROMPT] = value }
    }

    suspend fun saveDarkTheme(value: Boolean) {
        context.dataStore.edit { it[Keys.IS_DARK_THEME] = value }
    }

    suspend fun saveDynamicColor(value: Boolean) {
        context.dataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = value }
    }

    suspend fun saveStreamingEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.STREAMING_ENABLED] = value }
    }

    suspend fun saveContextWindowSize(value: Int) {
        context.dataStore.edit { it[Keys.CONTEXT_WINDOW_SIZE] = value }
    }
}
