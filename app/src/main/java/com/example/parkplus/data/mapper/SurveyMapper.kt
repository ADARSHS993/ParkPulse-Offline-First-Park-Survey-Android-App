
package com.example.parkplus.data.mapper

import com.example.parkplus.data.local.entity.SurveyEntity
import com.example.parkplus.domain.Cleanliness
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Survey

fun SurveyEntity.toDomain(): Survey {
    return Survey(
        id = id,
        parkId = parkId,
        checkedInAt = checkedInAt,
        checkInLocation = GeoPoint(
            latitude = latitude,
            longitude = longitude
        ),
        gpsAccuracyMeters = gpsAccuracyMeters,
        treesCounted = treesCounted,
        lightsWorking = lightsWorking,
        cleanliness = Cleanliness.valueOf(cleanliness),
        notes = notes,
        photoIds = emptyList(),
        isPendingSync = isPendingSync
    )
}

fun Survey.toEntity(): SurveyEntity {
    return SurveyEntity(
        id = id,
        parkId = parkId,
        checkedInAt = checkedInAt,
        latitude = checkInLocation.latitude,
        longitude = checkInLocation.longitude,
        gpsAccuracyMeters = gpsAccuracyMeters,
        treesCounted = treesCounted,
        lightsWorking = lightsWorking,
        cleanliness = cleanliness.name,
        notes = notes,
        isPendingSync = isPendingSync
    )
}
