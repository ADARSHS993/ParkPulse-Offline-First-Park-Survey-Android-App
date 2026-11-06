package com.example.parkplus.domain.repository

import com.example.parkplus.domain.model.Survey
import kotlinx.coroutines.flow.Flow

interface SurveyRepository {
    fun getSurveysByPark(
        parkId: String
    ): Flow<List<Survey>>

    suspend fun saveSurvey(survey: Survey)

    suspend fun getPendingSurveys(): List<Survey>
}