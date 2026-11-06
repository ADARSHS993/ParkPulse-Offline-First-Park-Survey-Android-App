package com.example.parkplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.parkplus.data.local.entity.ParkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkDao {
    @Query("SELECT * FROM parks ORDER BY name ASC")
    fun getAllParks(): Flow<List<ParkEntity>>

    @Query("SELECT * FROM parks WHERE id = :parkId LIMIT 1")
    suspend fun getParkById(parkId: String): ParkEntity?

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
    fun getParksByStatus(status: String?): Flow<List<ParkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPark(park: ParkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllParks(parks: List<ParkEntity>)

    @Query("DELETE FROM parks WHERE id = :parkId")
    suspend fun deletePark(parkId: String)

    @Query("SELECT * FROM parks WHERE isPendingSync = 1")
    suspend fun getPendingParks(): List<ParkEntity>

    @Query("UPDATE parks SET isPendingSync = 0 WHERE id = :parkId")
    suspend fun markParkSynced(parkId: String)

    @Query("SELECT COUNT(*) FROM parks")
    suspend fun getParkCount(): Int
}