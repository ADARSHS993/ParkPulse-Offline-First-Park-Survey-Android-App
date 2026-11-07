package com.example.parkplus.domain.usecase.park

import com.example.parkplus.domain.repository.ParkRepository
import javax.inject.Inject

class SearchParksUseCase @Inject constructor(
    private val repository: ParkRepository
) {
    operator fun invoke(query: String) =
        repository.searchParks(query)
}