package com.gemma.chat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemma.chat.data.repository.AppSettings
import com.gemma.chat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTemperature(value: Float) = viewModelScope.launch { settingsRepository.saveTemperature(value) }
    fun setTopK(value: Int) = viewModelScope.launch { settingsRepository.saveTopK(value) }
    fun setTopP(value: Float) = viewModelScope.launch { settingsRepository.saveTopP(value) }
    fun setMaxTokens(value: Int) = viewModelScope.launch { settingsRepository.saveMaxTokens(value) }
    fun setSystemPrompt(value: String) = viewModelScope.launch { settingsRepository.saveSystemPrompt(value) }
    fun setDarkTheme(value: Boolean) = viewModelScope.launch { settingsRepository.saveDarkTheme(value) }
    fun setDynamicColor(value: Boolean) = viewModelScope.launch { settingsRepository.saveDynamicColor(value) }
    fun setStreamingEnabled(value: Boolean) = viewModelScope.launch { settingsRepository.saveStreamingEnabled(value) }
    fun setContextWindowSize(value: Int) = viewModelScope.launch { settingsRepository.saveContextWindowSize(value) }
}
