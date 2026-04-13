package com.gemma.chat.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemma.chat.data.model.ChatSession
import com.gemma.chat.data.repository.ChatRepository
import com.gemma.chat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val sessions: StateFlow<List<ChatSession>> = chatRepository.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createNewSession(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = chatRepository.createSession()
            onCreated(id)
        }
    }

    fun deleteSession(session: ChatSession) {
        viewModelScope.launch {
            chatRepository.deleteSession(session)
        }
    }

    fun deleteAllSessions() {
        viewModelScope.launch {
            chatRepository.deleteAllSessions()
        }
    }
}
