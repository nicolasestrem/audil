package com.audil.data.remote

import com.audil.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes transcription to either [LocalTranscriptionClient] or [RemoteTranscriptionClient]
 * based on the user's [SettingsRepository.useLocalTranscription] preference.
 *
 * This avoids complex conditional DI — Hilt always injects this single router,
 * which decides at runtime where to send the request.
 */
@Singleton
class RoutedTranscriptionClient @Inject constructor(
    private val local: LocalTranscriptionClient,
    private val remote: RemoteTranscriptionClient,
    private val settings: SettingsRepository
) : TranscriptionClient {

    override suspend fun transcribe(
        file: File,
        model: String,
        language: String?
    ): Result<String> {
        val useLocal = settings.useLocalTranscription.first()
        return if (useLocal) {
            local.transcribe(file, model, language)
        } else {
            remote.transcribe(file, model, language)
        }
    }
}
