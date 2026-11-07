
package com.example.parkplus.domain.usecase.survey

import com.example.parkplus.domain.Cleanliness
import com.example.parkplus.domain.model.GeoPoint
import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.model.Survey
import com.example.parkplus.domain.repository.SurveyRepository
import java.util.UUID
import javax.inject.Inject

data class StartSurveyResult(
    val validation: CheckInValidationResult,
    val survey: Survey? = null
)

class StartSurveyUseCase @Inject constructor(
    private val validateCheckInUseCase: ValidateCheckInUseCase,
    private val surveyRepository: SurveyRepository
) {

    suspend operator fun invoke(
        park: Park,
        userLocation: GeoPoint,
        accuracyMeters: Float
    ): StartSurveyResult {

        val validation = validateCheckInUseCase(
            park = park,
            userLocation = userLocation,
            accuracyMeters = accuracyMeters
        )

        if (!validation.canStart) {
            return StartSurveyResult(
                validation = validation
            )
        }

        val survey = Survey(
            id = UUID.randomUUID().toString(),
            parkId = park.id,
            checkedInAt = System.currentTimeMillis(),
            checkInLocation = userLocation,
            gpsAccuracyMeters = accuracyMeters,
            treesCounted = 0,
            lightsWorking = false,
            cleanliness = Cleanliness.GOOD,
            notes = "",
            isPendingSync = true
        )

        surveyRepository.saveSurvey(survey)

        return StartSurveyResult(
            validation = validation,
            survey = survey
        )
    }
}
