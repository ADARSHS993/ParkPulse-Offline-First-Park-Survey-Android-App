package com.example.parkplus.di

import com.example.parkplus.data.repository.ParkRepositoryImpl
import com.example.parkplus.domain.repository.ParkRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParkModule {
    @Binds
    @Singleton
    abstract fun bindParkRepository(
        implementation: ParkRepositoryImpl
    ): ParkRepository
}