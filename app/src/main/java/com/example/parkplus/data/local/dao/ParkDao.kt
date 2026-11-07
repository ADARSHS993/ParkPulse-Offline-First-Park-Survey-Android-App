package com.example.parkplus.data.local.dao

import androidx.room.*
import com.example.parkplus.data.local.entity.ParkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkDao {

    @Query("SELECT * FROM parks ORDER BY name ASC")
    fun getAllParks(): Flow<List<ParkEntity>>

    @Query("SELECT * FROM parks WHERE id = :id")
    suspend fun getParkById(id: String): ParkEntity?

    @Query("""
        SELECT * FROM parks
        WHERE name LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchParks(query: String): Flow<List<ParkEntity>>

    @Query("""
        SELECT * FROM parks
        WHERE (:status IS NULL OR status = :status)
        ORDER BY name ASC
    """)
    fun getParksByStatus(
        status: String?
    ): Flow<List<ParkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPark(park: ParkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(parks: List<ParkEntity>)

    @Update
    suspend fun updatePark(park: ParkEntity)

    @Query("DELETE FROM parks WHERE id = :id")
    suspend fun deletePark(id: String)
}