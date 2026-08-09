package com.audil.domain.model

enum class MeetingType(val displayName: String, val promptTemplate: String) {
    STANDUP("Daily Standup", "Summarize this standup meeting. Identify what each person did yesterday, what they are doing today, and any blockers."),
    TEAM_MEETING("Team Meeting", "Summarize this team meeting. List key decisions made, important updates, and action items with owners."),
    INTERVIEW("Interview", "Summarize this interview. Highlight the candidate's strengths, weaknesses, and key qualifications mentioned."),
    LECTURE("Lecture", "Summarize this lecture. List the key concepts defined and providing examples for each."),
    PERSONAL_NOTE("Personal Note", "Summarize this personal note. Extract the key ideas, tasks, and reminders mentioned."),
    CUSTOM("Custom", "Summarize this text."),
}

data class MeetingContext(
    val type: MeetingType,
    val participantCount: Int = 2,
    val customPrompt: String? = null
) {
    fun getFullPrompt(): String {
        val participants = if (participantCount == 1) "1 participant" else "$participantCount participants"
        val soloContext = if (participantCount == 1 || type == MeetingType.PERSONAL_NOTE) " This is a solo voice memo/recording." else ""

        val structuredFormat = """

Structure your summary with these sections:
## Overview
2-3 sentence summary of the meeting's purpose and outcome.

## Key Decisions
List decisions made, with context.

## Action Items
Format: "- [Owner] Action description (Due: date if mentioned)"

## Open Questions
Unresolved questions that need follow-up.

## Risks & Concerns
Any flagged risks, blockers, or dependencies."""

        val contextSpecific = if (customPrompt != null) {
            "$customPrompt (Meeting type: ${type.displayName}, $participants.)$structuredFormat"
        } else {
            "${type.promptTemplate} The meeting had $participants.$soloContext$structuredFormat"
        }
        return contextSpecific
    }
}
