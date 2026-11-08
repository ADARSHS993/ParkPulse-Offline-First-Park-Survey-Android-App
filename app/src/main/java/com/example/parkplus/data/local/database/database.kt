package com.example.parkplus.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.parkplus.data.local.dao.ParkDao
import com.example.parkplus.data.local.dao.PhotoDao
import com.example.parkplus.data.local.dao.SurveyDao
import com.example.parkplus.data.local.entity.ParkEntity
import com.example.parkplus.data.local.entity.PhotoEntity
import com.example.parkplus.data.local.entity.SurveyEntity

@Database(
    entities = [
        ParkEntity::class,
        SurveyEntity::class,
        PhotoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class database : RoomDatabase() {

    abstract fun parkDao() : ParkDao
    abstract fun surveyDao() : SurveyDao
    abstract fun photoDao() : PhotoDao
}