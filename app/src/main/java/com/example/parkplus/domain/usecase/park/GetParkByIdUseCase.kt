package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.repository.ParkRepository
import javax.inject.Inject

class GetParkByIdUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    suspend operator fun invoke(id: String) =
        repository.getParkById(id)
}