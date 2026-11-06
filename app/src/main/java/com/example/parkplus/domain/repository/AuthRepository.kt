package com.example.parkplus.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>

    val userEmail: Flow<String>

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit>

    suspend fun logout()
}