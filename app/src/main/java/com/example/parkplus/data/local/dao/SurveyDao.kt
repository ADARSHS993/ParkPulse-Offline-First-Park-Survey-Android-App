package com.example.parkplus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.parkplus.data.local.entity.SurveyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {
    @Query("""
        SELECT * FROM surveys
        WHERE parkId = :parkId
        ORDER BY checkedInAt DESC
    """)
    fun getSurveysByPark(
        parkId: String
    ): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE id = :surveyId LIMIT 1")
    suspend fun getSurveyById(
        surveyId: String
    ): SurveyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSurveys(surveys: List<SurveyEntity>)

    @Query("SELECT * FROM surveys WHERE isPendingSync = 1")
    suspend fun getPendingSurveys(): List<SurveyEntity>

    @Query("UPDATE surveys SET isPendingSync = 0 WHERE id = :surveyId")
    suspend fun markSurveySynced(surveyId: String)

    @Query("DELETE FROM surveys WHERE id = :surveyId")
    suspend fun deleteSurvey(surveyId: String)
}