
package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.utils.GeoUtils
import javax.inject.Inject

class CalculateParkAreaUseCase @Inject constructor() {

    operator fun invoke(
        points: List<GeoPoint>
    ): Double {
        return GeoUtils.calculateAreaSquareMeters(points)
    }
}
