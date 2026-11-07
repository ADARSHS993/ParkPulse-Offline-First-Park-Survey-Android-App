
package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.utils.GeoUtils
import javax.inject.Inject

data class CheckInValidationResult(
    val canStart: Boolean,
    val isInsidePark: Boolean,
    val hasAccurateGps: Boolean,
    val distanceOutsideMeters: Double,
    val message: String
)

class ValidateCheckInUseCase @Inject constructor() {

    operator fun invoke(
        park: Park,
        userLocation: GeoPoint,
        accuracyMeters: Float
    ): CheckInValidationResult {

        val inside = isInsidePark(
            userLocation,
            park
        )

        val accurate = accuracyMeters.isFinite() &&
                accuracyMeters >= 0f &&
                accuracyMeters <= 25f

        val distanceOutside = if (inside) {
            0.0
        } else {
            distanceToParkBoundary(
                userLocation,
                park.boundary
            )
        }

        val message = when {
            !accurate ->
                "GPS signal is too weak. Accuracy must be 25 metres or better."

            !inside ->
                "You are approximately ${distanceOutside.toInt()} metres outside this park."

            else ->
                "Check-in successful. You can start the survey."
        }

        return CheckInValidationResult(
            canStart = inside && accurate,
            isInsidePark = inside,
            hasAccurateGps = accurate,
            distanceOutsideMeters = distanceOutside,
            message = message
        )
    }

    private fun isInsidePark(
        location: GeoPoint,
        park: Park
    ): Boolean {

        val points = park.boundary
        if (points.size < 3) return false

        var inside = false
        var j = points.lastIndex

        for (i in points.indices) {
            val xi = points[i].longitude
            val yi = points[i].latitude
            val xj = points[j].longitude
            val yj = points[j].latitude

            val intersects =
                ((yi > location.latitude) !=
                        (yj > location.latitude)) &&
                        (location.longitude <
                                (xj - xi) *
                                (location.latitude - yi) /
                                (yj - yi) + xi)

            if (intersects) inside = !inside
            j = i
        }

        return inside
    }

    private fun distanceToParkBoundary(
        location: GeoPoint,
        boundary: List<GeoPoint>
    ): Double {

        if (boundary.size < 2) return 0.0

        // Approximate distance using the nearest boundary vertex.
        // A later GIS refinement can calculate distance to each segment.
        return boundary.minOf { point ->
            GeoUtils.calculateDistance(location, point)
        }
    }
}
