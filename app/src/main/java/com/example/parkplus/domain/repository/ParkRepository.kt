package com.example.parkplus.domain.repository

import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.Park
import kotlinx.coroutines.flow.Flow

interface ParkRepository {

    fun getAllParks(): Flow<List<Park>>

    suspend fun getParkById(id: String): Park?

    fun searchParks(query: String): Flow<List<Park>>

    fun getParksByStatus(
        status: ParkStatus?
    ): Flow<List<Park>>

    suspend fun insertPark(park: Park)

    suspend fun updatePark(park: Park)

    suspend fun deletePark(id: String)

}