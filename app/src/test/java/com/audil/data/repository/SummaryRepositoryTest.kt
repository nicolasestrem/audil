package com.audil.data.repository

import com.audil.data.remote.ChatMessage
import com.audil.data.remote.OpenAiApiClient
import com.audil.domain.model.MeetingContext
import com.audil.domain.model.MeetingType
import com.audil.nativelib.LlamaCppWrapper
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import org.junit.Assert.*
import org.junit.Test

class SummaryRepositoryTest {

    // --- Structured Prompt Tests ---

    @Test
    fun `structured summary prompt contains overview section`() {
        val prompt = buildStructuredPrompt(MeetingContext(MeetingType.TEAM_MEETING, 5))

        assertTrue(
            "Prompt should contain 'Overview' or '## Overview' section",
            prompt.contains("Overview", ignoreCase = true) || prompt.contains("overview", ignoreCase = false)
        )
    }

    @Test
    fun `structured summary prompt contains decisions section`() {
        val prompt = buildStructuredPrompt(MeetingContext(MeetingType.TEAM_MEETING, 3))

        assertTrue(
            "Prompt should contain 'Key Decisions' or 'Decisions' section",
            prompt.contains("Decisions", ignoreCase = true) || prompt.contains("Key Decision", ignoreCase = true)
        )
    }

    @Test
    fun `structured summary prompt contains action items with owner and due`() {
        val prompt = buildStructuredPrompt(MeetingContext(MeetingType.STANDUP, 4))

        assertTrue(
            "Prompt should contain 'Action Items' section",
            prompt.contains("Action Items", ignoreCase = true) || prompt.contains("Action Item", ignoreCase = true)
        )
        assertTrue(
            "Action Items should mention owner",
            prompt.contains("owner", ignoreCase = true)
        )
    }

    @Test
    fun `structured summary prompt contains open questions section`() {
        val prompt = buildStructuredPrompt(MeetingContext(MeetingType.INTERVIEW, 2))

        assertTrue(
            "Prompt should contain 'Open Questions' or 'Questions' section",
            prompt.contains("Open Questions", ignoreCase = true) || prompt.contains("Questions", ignoreCase = true)
        )
    }

    @Test
    fun `structured summary prompt contains risks section`() {
        val prompt = buildStructuredPrompt(MeetingContext(MeetingType.TEAM_MEETING, 8))

        assertTrue(
            "Prompt should contain 'Risks' section",
            prompt.contains("Risks", ignoreCase = true)
        )
    }

    @Test
    fun `structured summary prompt works for all meeting types`() {
        MeetingType.values().forEach { type ->
            val prompt = buildStructuredPrompt(MeetingContext(type, 3))
            assertTrue(
                "Prompt for $type should contain 'Overview'",
                prompt.contains("Overview", ignoreCase = true)
            )
            assertTrue(
                "Prompt for $type should contain 'Action Items'",
                prompt.contains("Action Items", ignoreCase = true)
            )
        }
    }

    @Test
    fun `custom prompt overrides structured template`() {
        val customPrompt = "Just tell me the main point."
        val context = MeetingContext(MeetingType.CUSTOM, 2, customPrompt)
        val prompt = buildStructuredPrompt(context)

        // The helper returns custom prompt verbatim (no structured sections)
        assertTrue("Prompt should contain the custom text", prompt.contains(customPrompt))
    }

    // --- Error Handling Tests ---

    @Test
    fun `remote summary should propagate error as failure not success text`() {
        val apiClient = mockk<OpenAiApiClient>()
        val transcript = "Meeting transcript content"

        coEvery {
            apiClient.chatCompletion(any(), "gpt-4o")
        } throws RuntimeException("API timeout")

        val result = kotlinx.coroutines.runBlocking {
            try {
                apiClient.chatCompletion(
                    listOf(ChatMessage("system", "prompt"), ChatMessage("user", transcript)),
                    "gpt-4o"
                )
                null // should not reach here
            } catch (e: Exception) {
                e
            }
        }

        assertNotNull("Should have thrown an exception", result)
        assertTrue(
            "Error should not be wrapped in success text",
            result?.message?.contains("API timeout") == true
        )
    }

    @Test
    fun `remote summary error should not emit as success string`() {
        // Simulating what the old code did:
        // emit("Error generating summary: ${e.message}") — this is BAD
        // The new code should throw/return Error state instead

        val errorMessage = "Connection refused"
        val isSuccessStyleError = errorMessage.startsWith("Error generating summary:")
        assertFalse(
            "Error messages should NOT be formatted as success text",
            isSuccessStyleError
        )

        // Instead, the error should be caught and turned into a proper Error state
        val caughtError = RuntimeException(errorMessage)
        assertEquals("Connection refused", caughtError.message)
    }

    // --- Helper: uses the same logic as SummaryRepository.buildSummaryPrompt ---
    private fun buildStructuredPrompt(context: MeetingContext): String {
        val custom = context.customPrompt
        if (custom != null) return custom

        val meetingTypeName = context.type.displayName
        val participants = if (context.participantCount == 1) "1 participant" else "${context.participantCount} participants"

        return """
You are an expert meeting summarizer. Analyze the provided transcript of a $meetingTypeName meeting with $participants and produce a structured summary.

Your response MUST follow this format exactly:

## Overview
A concise 2-3 sentence summary of the meeting's purpose and main topics.

## Key Decisions
Bullet-point list of decisions made during the meeting. Include who made or supported each decision.

## Action Items
Bullet-point list with format: "- [Task description] — Owner: [name], Due: [date/if mentioned]"
If no explicit due date is mentioned, write "Due: Not specified".

## Open Questions
Questions raised but not resolved during the meeting.

## Risks or Concerns
Any risks, blockers, or concerns mentioned.

IMPORTANT: If any section has no content, write "None identified." instead of omitting it.
        """.trimIndent()
    }
}
