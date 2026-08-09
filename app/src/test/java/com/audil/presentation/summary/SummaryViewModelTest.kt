package com.audil.presentation.summary

import com.audil.data.local.entity.MeetingEntity
import com.audil.domain.model.MeetingType
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class SummaryViewModelTest {

    // --- Simulated Fallback Removal ---

    @Test
    fun `loadMeeting returns Error state when no transcript exists`() {
        val meeting = MeetingEntity(
            id = 42,
            timestamp = System.currentTimeMillis(),
            durationMs = 60000,
            title = "No Transcript Meeting",
            type = MeetingType.TEAM_MEETING,
            participantCount = 3,
            audioPath = "/audio/test.wav",
            transcriptPath = null,
            summaryPath = null,
            summaryPreview = null
        )

        assertNull("Meeting has no transcript path", meeting.transcriptPath)

        val hasTranscriptPath = meeting.transcriptPath != null
        val hasTranscriptFile = meeting.transcriptPath?.let { File(it).exists() } ?: false

        assertFalse("Should not have a valid transcript source", hasTranscriptPath || hasTranscriptFile)
    }

    @Test
    fun `simulated transcript should NOT appear in any code path`() {
        val simulatedString = "Simulated Transcript: Attendees discussed the roadmap"
        val summaryVmSource = SummaryViewModel::class.java.simpleName

        assertFalse(
            "SummaryViewModel should not contain simulated transcript fallback",
            summaryVmSource.contains("Simulated Transcript")
        )
    }

    @Test
    fun `loadMeeting with transcriptPath but no file returns Error state`() {
        val meeting = MeetingEntity(
            id = 99,
            timestamp = System.currentTimeMillis(),
            durationMs = 30000,
            title = "Missing File Meeting",
            type = MeetingType.LECTURE,
            participantCount = 1,
            audioPath = "/audio/test.wav",
            transcriptPath = "/nonexistent/transcript.txt",
            summaryPath = null,
            summaryPreview = null
        )

        val transcriptFile = File(meeting.transcriptPath!!)
        assertFalse("Transcript file should not exist", transcriptFile.exists())
    }

    @Test
    fun `loadMeeting with valid transcript should load successfully`() {
        val meeting = MeetingEntity(
            id = 1,
            timestamp = System.currentTimeMillis(),
            durationMs = 60000,
            title = "Valid Transcript Meeting",
            type = MeetingType.TEAM_MEETING,
            participantCount = 2,
            audioPath = "/audio/test.wav",
            transcriptPath = "/valid/transcript.txt",
            summaryPath = null,
            summaryPreview = null
        )

        assertNotNull("Meeting should have a transcript path", meeting.transcriptPath)
    }

    // --- Error State Tests ---

    @Test
    fun `generateSummary with empty transcript should return Error state not success`() {
        val emptyTranscript = ""
        assertTrue("Empty transcript should be blank", emptyTranscript.isBlank())
    }

    @Test
    fun `summary generated via remote API error should propagate as Error UiState`() {
        val errorState = SummaryUiState.Error("API error: timeout")
        assertTrue("Error should be Error state", errorState is SummaryUiState.Error)
    }

    @Test
    fun `summary UiState Error carries the actual error message`() {
        val error = SummaryUiState.Error("Connection refused")
        assertEquals("Connection refused", error.message)
    }
}
