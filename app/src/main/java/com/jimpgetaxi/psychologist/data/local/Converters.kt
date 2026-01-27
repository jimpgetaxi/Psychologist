package com.jimpgetaxi.psychologist.data.local

import androidx.room.TypeConverter
import com.jimpgetaxi.psychologist.domain.model.Sender

class Converters {
    @TypeConverter
    fun fromSender(sender: Sender): String {
        return sender.name
    }

    @TypeConverter
    fun toSender(value: String): Sender {
        return Sender.valueOf(value)
    }
}
