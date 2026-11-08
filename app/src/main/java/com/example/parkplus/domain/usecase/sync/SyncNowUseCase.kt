
package com.example.parkplus.domain.usecase.sync

import com.example.parkplus.data.remote.MockSyncServer
import com.example.parkplus.data.remote.NetworkMonitor
import com.example.parkplus.domain.repository.ParkRepository
import com.example.parkplus.domain.repository.SurveyRepository
import com.example.parkplus.domain.repository.PhotoRepository
import javax.inject.Inject

data class SyncResult(
    val parksSynced: Int = 0,
    val surveysSynced: Int = 0,
    val photosSynced: Int = 0,
    val failed: Int = 0,
    val message: String
)

class SyncNowUseCase @Inject constructor(
    private val parkRepository: ParkRepository,
    private val surveyRepository: SurveyRepository,
    private val photoRepository: PhotoRepository,
    private val server: MockSyncServer,
    private val networkMonitor: NetworkMonitor
) {
    suspend operator fun invoke(): SyncResult {
        if (!networkMonitor.isOnline()) {
            return SyncResult(
                failed = 1,
                message = "No internet connection. Your data is saved locally."
            )
        }

        var parksSynced = 0
        var surveysSynced = 0
        var photosSynced = 0
        var failed = 0

        // Upload parks first.
        parkRepository.getPendingParks().forEach { park ->
            try {
                server.uploadPark(park)
                parkRepository.markParkSynced(park.id)
                parksSynced++
            } catch (_: Exception) {
                failed++
            }
        }

        // Upload surveys.
        surveyRepository.getPendingSurveys().forEach { survey ->
            try {
                server.uploadSurvey(survey)
                surveyRepository.markSurveySynced(survey.id)
                surveysSynced++
            } catch (_: Exception) {
                failed++
            }
        }

        // Upload photo records.
        photoRepository.getPendingPhotos().forEach { photo ->
            try {
                server.uploadPhoto(photo)
                photoRepository.markPhotoSynced(photo.id)
                photosSynced++
            } catch (_: Exception) {
                failed++
            }
        }

        val total = parksSynced + surveysSynced + photosSynced

        val message = when {
            failed > 0 ->
                "$total records synced. $failed failed; try Sync Now again."

            total == 0 ->
                "Everything is already synchronized."

            else ->
                "Successfully synchronized $total records."
        }

        return SyncResult(
            parksSynced = parksSynced,
            surveysSynced = surveysSynced,
            photosSynced = photosSynced,
            failed = failed,
            message = message
        )
    }
}
