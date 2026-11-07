package com.example.parkplus.data.mapper

import com.example.parkplus.data.local.entity.ParkEntity
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.ParkType
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import org.json.JSONArray
import org.json.JSONObject

fun ParkEntity.toDomain(): Park {
    val jsonArray = JSONArray(boundary)

    val points = buildList {
        for (i in 0 until jsonArray.length()) {
            val point = jsonArray.getJSONObject(i)
            add(
                GeoPoint(
                    latitude = point.getDouble("latitude"),
                    longitude = point.getDouble("longitude")
                )
            )
        }
    }

    return Park(
        id = id,
        name = name,
        type = ParkType.valueOf(type),
        areaSquareMeters = areaSquareMeters,
        status = ParkStatus.valueOf(status),
        lastSurveyDate = lastSurveyDate,
        boundary = points,
        isPendingSync = isPendingSync
    )
}

fun Park.toEntity(): ParkEntity {
    val jsonArray = JSONArray()

    boundary.forEach { point ->
        jsonArray.put(
            JSONObject().apply {
                put("latitude", point.latitude)
                put("longitude", point.longitude)
            }
        )
    }

    return ParkEntity(
        id = id,
        name = name,
        type = type.name,
        areaSquareMeters = areaSquareMeters,
        status = status.name,
        lastSurveyDate = lastSurveyDate,
        boundary = jsonArray.toString(),
        isPendingSync = isPendingSync
    )
}