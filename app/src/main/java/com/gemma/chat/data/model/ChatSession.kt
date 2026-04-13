package com.gemma.chat.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val messageCount: Int = 0
) {
    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """You are Gemma, a helpful, harmless, and honest AI assistant created by Google. 
You are running completely offline on the user's device using the Gemma 4 E2B model.
Be concise, helpful, and friendly. Format responses with markdown when appropriate."""
    }
}
