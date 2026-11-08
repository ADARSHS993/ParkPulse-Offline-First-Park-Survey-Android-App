
package com.example.parkplus.data.remote

import com.example.parkplus.domain.model.Park
import com.example.parkplus.domain.model.Survey
import com.example.parkplus.domain.model.PhotoRecord

interface MockSyncServer {
    suspend fun uploadPark(park: Park)
    suspend fun uploadSurvey(survey: Survey)
    suspend fun uploadPhoto(photo: PhotoRecord)
}
