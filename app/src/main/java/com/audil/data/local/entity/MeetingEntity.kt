package com.audil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.audil.domain.model.MeetingType

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val durationMs: Long,
    val title: String,
    val type: MeetingType,
    val participantCount: Int,
    
    // File Paths
    val audioPath: String,
    val transcriptPath: String? = null,
    val summaryPath: String? = null,
    
    // Cached Content (Optional, for quick preview)
    val summaryPreview: String? = null
)
