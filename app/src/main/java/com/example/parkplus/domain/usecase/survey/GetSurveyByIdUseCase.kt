package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.model.Survey
import com.example.parkplus.domain.repository.SurveyRepository
import javax.inject.Inject

class GetSurveyByIdUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(surveyId: String): Survey? {
        return repository.getSurveyById(surveyId)
    }
}