
package com.example.parkplus.data.repository

import com.example.parkplus.data.local.dao.SurveyDao
import com.example.parkplus.data.mapper.toDomain
import com.example.parkplus.data.mapper.toEntity
import com.example.parkplus.domain.model.Survey
import com.example.parkplus.domain.repository.SurveyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    private val surveyDao: SurveyDao
) : SurveyRepository {

    override fun getSurveysByPark(
        parkId: String
    ): Flow<List<Survey>> {
        return surveyDao.getSurveysByPark(parkId).map { surveys ->
            surveys.map { it.toDomain() }
        }
    }

    override suspend fun getSurveyById(surveyId: String): Survey? {
        return surveyDao.getSurveyById(surveyId)?.toDomain()
    }

    override suspend fun saveSurvey(survey: Survey) {
        surveyDao.insertSurvey(survey.toEntity())
    }

    override suspend fun getPendingSurveys(): List<Survey> {
        return surveyDao.getPendingSurveys().map {
            it.toDomain()
        }
    }

    override suspend fun markSurveySynced(surveyId: String) {
        surveyDao.markSurveySynced(surveyId)
    }
}
