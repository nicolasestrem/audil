package com.audil.data.local

import androidx.room.TypeConverter
import com.audil.domain.model.MeetingType

class Converters {
    @TypeConverter
    fun fromMeetingType(value: MeetingType): String {
        return value.name
    }

    @TypeConverter
    fun toMeetingType(value: String): MeetingType {
        return try {
            MeetingType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MeetingType.TEAM_MEETING // Fallback
        }
    }
}
