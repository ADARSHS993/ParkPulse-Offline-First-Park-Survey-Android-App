package com.example.parkplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.parkplus.data.local.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("""
        SELECT * FROM photos
        WHERE surveyId = :surveyId
        ORDER BY capturedAt DESC
    """)
    fun getPhotosBySurvey(
        surveyId: String
    ): Flow<List<PhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPhotos(photos: List<PhotoEntity>)

    @Query("SELECT * FROM photos WHERE isPendingSync = 1")
    suspend fun getPendingPhotos(): List<PhotoEntity>

    @Query("UPDATE photos SET isPendingSync = 0 WHERE id = :photoId")
    suspend fun markPhotoSynced(photoId: String)

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhoto(photoId: String)
}