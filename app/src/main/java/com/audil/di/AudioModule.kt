package com.audil.di

import com.audil.data.local.AndroidAudioRecorder
import com.audil.data.local.AudioRecorder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    @Binds
    @Singleton
    abstract fun bindAudioRecorder(
        audioRecorder: AndroidAudioRecorder
    ): AudioRecorder
}
