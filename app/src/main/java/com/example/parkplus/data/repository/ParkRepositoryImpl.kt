package com.example.parkplus.data.repository

import com.example.parkplus.data.local.dao.ParkDao
import com.example.parkplus.data.mapper.toDomain
import com.example.parkplus.data.mapper.toEntity
import com.example.parkplus.domain.ParkStatus
import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.repository.ParkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ParkRepositoryImpl @Inject constructor(
    private val parkDao: ParkDao
) : ParkRepository {

    override fun getAllParks(): Flow<List<Park>> =
        parkDao.getAllParks().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getParkById(id: String): Park? =
        parkDao.getParkById(id)?.toDomain()

    override fun searchParks(query: String): Flow<List<Park>> =
        parkDao.searchParks(query).map { list ->
            list.map { it.toDomain() }
        }

    override fun getParksByStatus(
        status: ParkStatus?
    ): Flow<List<Park>> =
        parkDao.getParksByStatus(status?.name).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun insertPark(park: Park) {
        parkDao.insertPark(park.toEntity())
    }

    override suspend fun updatePark(park: Park) {
        parkDao.updatePark(park.toEntity())
    }

    override suspend fun deletePark(id: String) {
        parkDao.deletePark(id)
    }

    override suspend fun getPendingParks(): List<Park> { return parkDao.getPendingParks().map { it.toDomain() }
    }

    override suspend fun markParkSynced(parkId: String) {
        parkDao.markParkSynced(parkId)
    }
}