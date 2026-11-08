package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.model.PhotoRecord
import com.example.parkplus.domain.repository.PhotoRepository
import java.util.UUID
import javax.inject.Inject

class SavePhotoUseCase @Inject constructor(
    private val repository: PhotoRepository
) {

    suspend operator fun invoke(
        surveyId: String,
        localPath: String,
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float
    ) {
        require(localPath.isNotBlank()) {
            "Photo path cannot be empty"
        }

        val photo = PhotoRecord(
            id = UUID.randomUUID().toString(),
            surveyId = surveyId,
            localPath = localPath,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            capturedAt = System.currentTimeMillis(),
            isPendingSync = true
        )

        repository.savePhoto(photo)
    }
}