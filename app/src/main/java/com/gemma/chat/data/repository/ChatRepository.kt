package com.gemma.chat.data.repository

import com.gemma.chat.data.db.ChatMessageDao
import com.gemma.chat.data.db.ChatSessionDao
import com.gemma.chat.data.model.ChatMessage
import com.gemma.chat.data.model.ChatSession
import com.gemma.chat.data.model.MessageRole
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) {
    // Sessions
    fun getAllSessions(): Flow<List<ChatSession>> = sessionDao.getAllSessions()

    suspend fun getSession(id: Long): ChatSession? = sessionDao.getSessionById(id)

    suspend fun createSession(systemPrompt: String = ChatSession.DEFAULT_SYSTEM_PROMPT): Long {
        val session = ChatSession(
            title = "New Chat",
            systemPrompt = systemPrompt
        )
        return sessionDao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        sessionDao.updateTitle(sessionId, title)
    }

    suspend fun deleteSession(session: ChatSession) {
        sessionDao.deleteSession(session)
    }

    suspend fun deleteAllSessions() {
        sessionDao.deleteAllSessions()
    }

    // Messages
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessage>> =
        messageDao.getMessagesForSession(sessionId)

    suspend fun getMessagesForSessionSync(sessionId: Long): List<ChatMessage> =
        messageDao.getMessagesForSessionSync(sessionId)

    suspend fun insertMessage(message: ChatMessage): Long {
        val id = messageDao.insertMessage(message)
        sessionDao.incrementMessageCount(message.sessionId)
        return id
    }

    suspend fun updateMessage(message: ChatMessage) {
        messageDao.updateMessage(message)
    }

    suspend fun deleteAllMessagesForSession(sessionId: Long) {
        messageDao.deleteAllMessagesForSession(sessionId)
    }

    suspend fun getRecentMessagesForContext(sessionId: Long, limit: Int = 10): List<ChatMessage> {
        val messages = messageDao.getRecentMessages(sessionId, limit * 2)
        return messages.reversed() // oldest first
    }

    /**
     * Auto-generate a session title from the first user message.
     */
    fun generateTitle(firstUserMessage: String): String {
        val cleaned = firstUserMessage.trim()
        return if (cleaned.length <= 40) {
            cleaned
        } else {
            cleaned.take(37).trimEnd() + "..."
        }
    }

    /**
     * Build context pairs (user, assistant) for the prompt builder.
     * Returns pairs of (userMessage, assistantResponse).
     */
    suspend fun buildContextPairs(
        sessionId: Long,
        contextWindowSize: Int = 10
    ): List<Pair<String, String?>> {
        val messages = getRecentMessagesForContext(sessionId, contextWindowSize)
        val result = mutableListOf<Pair<String, String?>>()

        var i = 0
        while (i < messages.size) {
            val msg = messages[i]
            if (msg.role == MessageRole.USER) {
                val next = messages.getOrNull(i + 1)
                if (next?.role == MessageRole.ASSISTANT) {
                    result.add(Pair(msg.content, next.content))
                    i += 2
                } else {
                    result.add(Pair(msg.content, null))
                    i++
                }
            } else {
                i++
            }
        }
        return result
    }
}
