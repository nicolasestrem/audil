package com.audil.di

import android.content.Context
import androidx.room.Room
import com.audil.data.local.AppDatabase
import com.audil.data.local.MIGRATION_1_2
import com.audil.data.local.dao.MeetingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "audil_db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun provideMeetingDao(database: AppDatabase): MeetingDao {
        return database.meetingDao()
    }
}
