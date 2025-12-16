package com.audil.data.repository

import com.audil.data.local.dao.MeetingDao
import com.audil.data.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val meetingDao: MeetingDao
) {
    fun getAllMeetings(): Flow<List<MeetingEntity>> = meetingDao.getAllMeetings()

    suspend fun getMeetingById(id: Long): MeetingEntity? = meetingDao.getMeetingById(id)

    suspend fun saveMeeting(meeting: MeetingEntity): Long {
        return if (meeting.id == 0L) {
            meetingDao.insertMeeting(meeting)
        } else {
            meetingDao.updateMeeting(meeting)
            meeting.id
        }
    }

    suspend fun updateSummary(id: Long, summaryPath: String, summaryPreview: String) {
        meetingDao.updateSummary(id, summaryPath, summaryPreview)
    }

    suspend fun deleteMeeting(meeting: MeetingEntity) {
        meetingDao.deleteMeeting(meeting)
    }
}
