package com.example.parkplus.di

import com.example.parkplus.data.repository.PhotoRepositoryImpl
import com.example.parkplus.domain.repository.PhotoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoModule {

    @Binds
    @Singleton
    abstract fun bindPhotoRepository(
        implementation: PhotoRepositoryImpl
    ): PhotoRepository
}