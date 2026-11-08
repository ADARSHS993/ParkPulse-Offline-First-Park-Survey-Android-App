package com.example.parkplus.data.mapper

import com.example.parkplus.data.local.entity.PhotoEntity
import com.example.parkplus.domain.model.PhotoRecord

fun PhotoEntity.toDomain(): PhotoRecord {
    return PhotoRecord(
        id = id,
        surveyId = surveyId,
        localPath = localPath,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        capturedAt = capturedAt,
        isPendingSync = isPendingSync
    )
}

fun PhotoRecord.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        surveyId = surveyId,
        localPath = localPath,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        capturedAt = capturedAt,
        isPendingSync = isPendingSync
    )
}