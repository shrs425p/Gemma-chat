package com.gemma.chat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gemma.chat.data.inference.GemmaInferenceEngine
import com.gemma.chat.data.model.ChatMessage
import com.gemma.chat.data.model.MessageRole
import com.gemma.chat.data.repository.ChatRepository
import com.gemma.chat.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val sessionId: Long = -1L,
    val sessionTitle: String = "New Chat",
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null,
    val inputText: String = "",
    val attachedMediaUris: List<String> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    private val inferenceEngine: GemmaInferenceEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: -1L

    private val _uiState = MutableStateFlow(ChatUiState(sessionId = sessionId))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var generationJob: Job? = null
    private var isFirstMessage = true

    init {
        loadSession()
        observeMessages()
    }

    private fun loadSession() {
        viewModelScope.launch {
            val session = chatRepository.getSession(sessionId)
            _uiState.value = _uiState.value.copy(
                sessionTitle = session?.title ?: "Chat"
            )
            isFirstMessage = (session?.messageCount ?: 0) == 0
        }
    }

    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesForSession(sessionId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val input = _uiState.value.inputText.trim()
        if (input.isBlank() || _uiState.value.isGenerating) return

        val attachments = _uiState.value.attachedMediaUris.toList()
        _uiState.value = _uiState.value.copy(inputText = "", attachedMediaUris = emptyList(), isGenerating = true, error = null)

        generationJob = viewModelScope.launch {
            try {
                val isFirst = isFirstMessage
                
                // Save user message to DB (save the clean input, not the system-prompt injected one)
                val userMessage = ChatMessage(
                    sessionId = sessionId,
                    role = MessageRole.USER,
                    content = input,
                    attachmentUris = attachments
                )
                chatRepository.insertMessage(userMessage)

                // Auto-title session from first message
                if (isFirstMessage) {
                    isFirstMessage = false
                    val title = chatRepository.generateTitle(input)
                    chatRepository.updateSessionTitle(sessionId, title)
                    _uiState.value = _uiState.value.copy(sessionTitle = title)
                }

                // Streaming generation — LiteRT-LM Conversation handles context
                val sb = StringBuilder()
                _uiState.value = _uiState.value.copy(streamingContent = "")

                val systemPrompt = settingsRepository.settingsFlow.first().systemPrompt
                val promptToSend = if (isFirst && systemPrompt.isNotBlank()) {
                    "$systemPrompt\n\n$input"
                } else {
                    input
                }

                inferenceEngine.generateStreamingResponse(promptToSend).collect { chunk ->
                    sb.append(chunk)
                    _uiState.value = _uiState.value.copy(streamingContent = sb.toString())
                }

                // Save completed assistant message to DB
                val assistantMessage = ChatMessage(
                    sessionId = sessionId,
                    role = MessageRole.ASSISTANT,
                    content = sb.toString()
                )
                chatRepository.insertMessage(assistantMessage)

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    streamingContent = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    streamingContent = "",
                    error = "Generation failed: ${e.message}"
                )
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            streamingContent = ""
        )
    }

    fun regenerateLastResponse() {
        val messages = _uiState.value.messages
        val lastUserMessage = messages.lastOrNull { it.role == MessageRole.USER }
        if (lastUserMessage != null) {
            // Remove last assistant message if present
            val lastAssistant = messages.lastOrNull { it.role == MessageRole.ASSISTANT }
            viewModelScope.launch {
                if (lastAssistant != null) {
                    chatRepository.updateMessage(
                        lastAssistant.copy(content = "")
                    )
                }
                // Re-trigger with original message
                _uiState.value = _uiState.value.copy(
                    inputText = lastUserMessage.content
                )
                sendMessage()
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.deleteAllMessagesForSession(sessionId)
            isFirstMessage = true
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun addAttachment(uri: String) {
        val current = _uiState.value.attachedMediaUris.toMutableList()
        current.add(uri)
        _uiState.value = _uiState.value.copy(attachedMediaUris = current)
    }

    fun removeAttachment(uri: String) {
        val current = _uiState.value.attachedMediaUris.toMutableList()
        current.remove(uri)
        _uiState.value = _uiState.value.copy(attachedMediaUris = current)
    }
}
