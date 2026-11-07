
package com.example.parkplus.di

import com.example.parkplus.data.repository.SurveyRepositoryImpl
import com.example.parkplus.domain.repository.SurveyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SurveyModule {

    @Binds
    @Singleton
    abstract fun bindSurveyRepository(
        implementation: SurveyRepositoryImpl
    ): SurveyRepository
}
