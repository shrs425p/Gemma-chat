package com.gemma.chat.data.db

import androidx.room.TypeConverter
import com.gemma.chat.data.model.MessageRole

class Converters {
    @TypeConverter
    fun fromMessageRole(role: MessageRole): String = role.name

    @TypeConverter
    fun toMessageRole(name: String): MessageRole = MessageRole.valueOf(name)
}
