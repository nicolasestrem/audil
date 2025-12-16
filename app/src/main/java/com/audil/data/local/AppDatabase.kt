package com.audil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.audil.data.local.dao.MeetingDao
import com.audil.data.local.entity.MeetingEntity

@Database(entities = [MeetingEntity::class], version = 1, exportSchema = false)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
}
