package com.audil.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.audil.data.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY timestamp DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingById(id: Long): MeetingEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity)

    @Query("UPDATE meetings SET summaryPath = :summaryPath, summaryPreview = :summaryPreview WHERE id = :id")
    suspend fun updateSummary(id: Long, summaryPath: String, summaryPreview: String)

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity)
}
