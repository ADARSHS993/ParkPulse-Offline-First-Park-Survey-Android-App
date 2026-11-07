package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.repository.ParkRepository
import javax.inject.Inject

class GetParksByStatusUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke(status: ParkStatus?) =
        repository.getParksByStatus(status)
}