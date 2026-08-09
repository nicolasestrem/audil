package com.audil.di

import com.audil.data.remote.AndroidApiSettingsProvider
import com.audil.data.remote.ApiSettingsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindApiSettingsProvider(
        provider: AndroidApiSettingsProvider
    ): ApiSettingsProvider
}
