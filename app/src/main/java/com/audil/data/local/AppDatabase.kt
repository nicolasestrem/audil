package com.audil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.audil.data.local.dao.MeetingDao
import com.audil.data.local.entity.MeetingEntity

@Database(entities = [MeetingEntity::class], version = 2, exportSchema = false)
@androidx.room.TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE INDEX IF NOT EXISTS index_meetings_timestamp ON meetings(timestamp DESC)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_meetings_type ON meetings(type)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_meetings_participantCount ON meetings(participantCount)")
    }
}
