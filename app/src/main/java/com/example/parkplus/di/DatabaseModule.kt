package com.example.parkplus.di

import android.content.Context
import androidx.room.Room
import com.example.parkplus.data.local.dao.ParkDao
import com.example.parkplus.data.local.dao.PhotoDao
import com.example.parkplus.data.local.dao.SurveyDao
import com.example.parkplus.data.local.database.database
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): database {
        return Room.databaseBuilder(
            context,
            database::class.java,
            "parkpulse_database"
        ).build()
    }

    @Provides
    fun provideParkDao(
        database: database
    ): ParkDao = database.parkDao()

    @Provides
    fun provideSurveyDao(
        database: database
    ): SurveyDao = database.surveyDao()

    @Provides
    fun providePhotoDao(
        database: database
    ): PhotoDao = database.photoDao()
}