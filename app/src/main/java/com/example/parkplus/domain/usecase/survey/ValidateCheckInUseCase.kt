
package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import javax.inject.Inject
import kotlin.math.*

data class CheckInValidationResult(
    val canStart: Boolean,
    val isInsidePark: Boolean,
    val hasAccurateGps: Boolean,
    val distanceOutsideMeters: Double,
    val message: String
)

class ValidateCheckInUseCase @Inject constructor() {

    companion object {
        private const val EARTH_RADIUS_METERS = 6371000.0
        private const val GPS_ACCURACY_LIMIT = 25f
        private const val BOUNDARY_TOLERANCE_METERS = 0.5
    }

    operator fun invoke(
        park: Park,
        userLocation: GeoPoint,
        accuracyMeters: Float
    ): CheckInValidationResult {

        val boundary = park.boundary

        if (boundary.size < 3) {
            return CheckInValidationResult(
                canStart = false,
                isInsidePark = false,
                hasAccurateGps = false,
                distanceOutsideMeters = 0.0,
                message = "Invalid park boundary. At least 3 points are required."
            )
        }

        val accurate =
            accuracyMeters.isFinite() &&
                    accuracyMeters >= 0f &&
                    accuracyMeters <= GPS_ACCURACY_LIMIT

        val distanceToBoundary = distanceToParkBoundary(
            userLocation,
            boundary
        )

        val inside = isInsidePark(
            userLocation,
            boundary
        ) || distanceToBoundary <= BOUNDARY_TOLERANCE_METERS

        val distanceOutside = if (inside) {
            0.0
        } else {
            distanceToBoundary
        }

        val canStart = inside && accurate

        val message = when {
            !accurate ->
                "GPS signal is too weak. Accuracy must be 25 metres or better."

            !inside ->
                "You are approximately ${distanceOutside.roundToInt()} metres outside this park."

            else ->
                "Check-in successful. You can start the survey."
        }

        return CheckInValidationResult(
            canStart = canStart,
            isInsidePark = inside,
            hasAccurateGps = accurate,
            distanceOutsideMeters = distanceOutside,
            message = message
        )
    }

    // Ray-casting point-in-polygon algorithm.
    private fun isInsidePark(
        location: GeoPoint,
        boundary: List<GeoPoint>
    ): Boolean {

        var inside = false
        var j = boundary.lastIndex

        for (i in boundary.indices) {

            val xi = boundary[i].longitude
            val yi = boundary[i].latitude

            val xj = boundary[j].longitude
            val yj = boundary[j].latitude

            val intersects =
                ((yi > location.latitude) !=
                        (yj > location.latitude)) &&
                        (location.longitude <
                                (xj - xi) *
                                (location.latitude - yi) /
                                (yj - yi) + xi)

            if (intersects) {
                inside = !inside
            }

            j = i
        }

        return inside
    }

    // Find the shortest distance to every boundary segment.
    private fun distanceToParkBoundary(
        location: GeoPoint,
        boundary: List<GeoPoint>
    ): Double {

        if (boundary.size < 2) return 0.0

        var minimumDistance = Double.MAX_VALUE

        for (i in boundary.indices) {

            val start = boundary[i]
            val end = boundary[(i + 1) % boundary.size]

            val distance = distanceToSegment(
                location,
                start,
                end
            )

            minimumDistance = min(
                minimumDistance,
                distance
            )
        }

        return minimumDistance
    }

    // Calculate point-to-segment distance in metres.
    private fun distanceToSegment(
        point: GeoPoint,
        start: GeoPoint,
        end: GeoPoint
    ): Double {

        val referenceLatitude = Math.toRadians(point.latitude)

        fun toLocalX(longitude: Double): Double {
            return EARTH_RADIUS_METERS *
                    Math.toRadians(longitude - point.longitude) *
                    cos(referenceLatitude)
        }

        fun toLocalY(latitude: Double): Double {
            return EARTH_RADIUS_METERS *
                    Math.toRadians(latitude - point.latitude)
        }

        val px = 0.0
        val py = 0.0

        val ax = toLocalX(start.longitude)
        val ay = toLocalY(start.latitude)

        val bx = toLocalX(end.longitude)
        val by = toLocalY(end.latitude)

        val dx = bx - ax
        val dy = by - ay

        val lengthSquared = dx * dx + dy * dy

        if (lengthSquared == 0.0) {
            return hypot(px - ax, py - ay)
        }

        val projection = (
                (px - ax) * dx + (py - ay) * dy
                ) / lengthSquared

        val t = projection.coerceIn(0.0, 1.0)

        val closestX = ax + t * dx
        val closestY = ay + t * dy

        return hypot(
            px - closestX,
            py - closestY
        )
    }
}
