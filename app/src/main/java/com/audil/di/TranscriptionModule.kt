package com.audil.di

import com.audil.data.remote.RemoteTranscriptionClient
import com.audil.data.remote.TranscriptionClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the [TranscriptionClient] implementation.
 *
 * Currently binds [RemoteTranscriptionClient] by default.
 * To switch to local transcription, change the binding to [LocalTranscriptionClient].
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionClient(
        remoteClient: RemoteTranscriptionClient
    ): TranscriptionClient
}
