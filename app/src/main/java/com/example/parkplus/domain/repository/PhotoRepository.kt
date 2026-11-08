package com.example.parkplus.domain.repository

import com.example.parkplus.domain.model.PhotoRecord
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    suspend fun savePhoto(photo: PhotoRecord)

    suspend fun getPhotosBySurvey(surveyId: String): Flow<List<PhotoRecord>>

    suspend fun getPendingPhotos(): List<PhotoRecord>
}