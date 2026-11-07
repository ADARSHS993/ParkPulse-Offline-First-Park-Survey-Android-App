
package com.example.parkplus.domain.utils

import com.example.parkplus.domain.model.GeoPoint
import kotlin.math.*

object GeoUtils {

    private const val EARTH_RADIUS_METERS = 6_371_000.0
    private const val SQUARE_METERS_PER_ACRE = 4046.8564224

    /**
     * Calculates the distance between two geographic points
     * using the Haversine formula.
     */
    fun calculateDistance(
        point1: GeoPoint,
        point2: GeoPoint
    ): Double {

        val lat1 = Math.toRadians(point1.latitude)
        val lat2 = Math.toRadians(point2.latitude)

        val deltaLat = lat2 - lat1
        val deltaLon = Math.toRadians(
            point2.longitude - point1.longitude
        )

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2).pow(2)

        val c = 2 * atan2(
            sqrt(a),
            sqrt(1 - a)
        )

        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates the perimeter of a polygon in meters.
     * The last point is automatically connected to the first.
     */
    fun calculatePerimeter(
        points: List<GeoPoint>
    ): Double {

        if (points.size < 3) return 0.0

        var perimeter = 0.0

        for (i in points.indices) {

            val current = points[i]
            val next = points[(i + 1) % points.size]

            perimeter += calculateDistance(
                current,
                next
            )
        }

        return perimeter
    }

    /**
     * Calculates polygon area on the Earth's surface
     * using a spherical polygon area approximation.
     */
    fun calculateAreaSquareMeters(
        points: List<GeoPoint>
    ): Double {

        if (points.size < 3) return 0.0

        var total = 0.0

        for (i in points.indices) {

            val current = points[i]
            val next = points[(i + 1) % points.size]

            val lat1 = Math.toRadians(current.latitude)
            val lat2 = Math.toRadians(next.latitude)

            var deltaLongitude = Math.toRadians(
                next.longitude - current.longitude
            )

            // Normalize longitude difference.
            if (deltaLongitude > Math.PI) {
                deltaLongitude -= 2 * Math.PI
            } else if (deltaLongitude < -Math.PI) {
                deltaLongitude += 2 * Math.PI
            }

            total += deltaLongitude *
                    (2 + sin(lat1) + sin(lat2))
        }

        val area = abs(total) *
                EARTH_RADIUS_METERS.pow(2) / 2.0

        // Return the smaller area on the sphere.
        val earthSurfaceArea =
            4 * Math.PI * EARTH_RADIUS_METERS.pow(2)

        return minOf(area, earthSurfaceArea - area)
    }

    /**
     * Converts square meters into acres.
     */
    fun squareMetersToAcres(
        squareMeters: Double
    ): Double {
        return squareMeters / SQUARE_METERS_PER_ACRE
    }

    /**
     * Returns area in both square meters and acres.
     */
    fun calculateAreaInAcres(
        points: List<GeoPoint>
    ): Double {
        return squareMetersToAcres(
            calculateAreaSquareMeters(points)
        )
    }
}
