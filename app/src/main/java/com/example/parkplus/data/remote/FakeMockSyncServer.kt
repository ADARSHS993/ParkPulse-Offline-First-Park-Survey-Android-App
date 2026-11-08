
package com.example.parkplus.data.remote

import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.model.Survey
import com.example.parkplus.domain.model.PhotoRecord
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FakeMockSyncServer @Inject constructor() : MockSyncServer {

    private val parks = mutableMapOf<String, Park>()
    private val surveys = mutableMapOf<String, Survey>()
    private val photos = mutableMapOf<String, PhotoRecord>()

    private suspend fun simulateNetwork() {
        delay(700)

        if (Random.nextInt(100) < 15) {
            throw IllegalStateException(
                "Mock server temporarily unavailable. Please retry."
            )
        }
    }

    override suspend fun uploadPark(park: Park) {
        simulateNetwork()
        parks[park.id] = park
    }

    override suspend fun uploadSurvey(survey: Survey) {
        simulateNetwork()
        surveys[survey.id] = survey
    }

    override suspend fun uploadPhoto(photo: PhotoRecord) {
        simulateNetwork()
        photos[photo.id] = photo
    }
}
