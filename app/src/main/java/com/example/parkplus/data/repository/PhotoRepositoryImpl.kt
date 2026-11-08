package com.example.parkplus.data.repository

import com.example.parkplus.data.local.dao.PhotoDao
import com.example.parkplus.data.mapper.toDomain
import com.example.parkplus.data.mapper.toEntity
import com.example.parkplus.domain.model.PhotoRecord
import com.example.parkplus.domain.repository.PhotoRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class PhotoRepositoryImpl @Inject constructor(
    private val photoDao: PhotoDao
) : PhotoRepository {
    override suspend fun savePhoto(photo: PhotoRecord) {
        photoDao.insertPhoto(photo.toEntity())
    }

    override suspend fun getPhotosBySurvey(surveyId: String): Flow<List<PhotoRecord>> {
        return photoDao.getPhotosBySurvey(surveyId).map { photos ->
            photos.map { photo ->
                photo.toDomain()
            }
        }
    }

    override suspend fun getPendingPhotos(): List<PhotoRecord> {
        return photoDao.getPendingPhotos().map { photo ->
            photo.toDomain()
        }
    }

}