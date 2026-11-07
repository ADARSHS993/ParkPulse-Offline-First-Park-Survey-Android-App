package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.Cleanliness
import com.example.parkplus.domain.repository.SurveyRepository
import javax.inject.Inject

class UpdateSurveyDetailsUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(
        surveyId: String,
        treesCounted: Int,
        lightsWorking: Boolean,
        cleanliness: Cleanliness,
        notes: String
    ) {
        require(treesCounted >= 0) {
            "Trees count cannot be negative"
        }

        val existingSurvey = repository.getSurveyById(surveyId)
            ?: throw IllegalArgumentException("Survey not found")

        val updatedSurvey = existingSurvey.copy(
            treesCounted = treesCounted,
            lightsWorking = lightsWorking,
            cleanliness = cleanliness,
            notes = notes,
            isPendingSync = true
        )

        repository.saveSurvey(updatedSurvey)
    }
}