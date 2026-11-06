package com.example.parkplus.domain.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
){
    init {
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
    }
}
