
package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.repository.ParkRepository
import javax.inject.Inject

class AddParkUseCase @Inject constructor(
    private val repository: ParkRepository
) {

    suspend operator fun invoke(park: Park) {
        require(park.boundary.size >= 3) {
            "A park boundary must have at least 3 points"
        }

        require(park.name.isNotBlank()) {
            "Park name cannot be empty"
        }

        repository.insertPark(park)
    }
}
