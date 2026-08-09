package com.audil.di

import com.audil.data.remote.RoutedTranscriptionClient
import com.audil.data.remote.TranscriptionClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the [TranscriptionClient] to [RoutedTranscriptionClient],
 * which delegates to local (sherpa-onnx) or remote (OpenAI) at runtime
 * based on [com.audil.data.repository.SettingsRepository.useLocalTranscription].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionClient(
        router: RoutedTranscriptionClient
    ): TranscriptionClient
}
