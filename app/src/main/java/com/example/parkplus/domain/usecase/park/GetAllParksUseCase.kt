package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.repository.ParkRepository
import javax.inject.Inject

class GetAllParksUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke() = repository.getAllParks()
}