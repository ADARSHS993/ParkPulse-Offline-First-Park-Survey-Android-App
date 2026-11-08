package com.example.parkplus.data.repository

import com.example.parkplus.data.local.preferences.SessionManager
import com.example.parkplus.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val sessionManager: SessionManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean>
        get() = sessionManager.isLoggedIn

    override val userEmail: Flow<String>
        get() = sessionManager.userEmail

    companion object {
        private const val DEMO_EMAIL = "surveyor@parkpulse.com"
        private const val DEMO_PASSWORD = "ParkPulse@123"
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Unit> {

        if (email.isBlank() || password.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Please enter email and password")
            )
        }

        if (
            email.trim().equals(DEMO_EMAIL, ignoreCase = true) &&
            password == DEMO_PASSWORD
        ) {
            sessionManager.saveSession(DEMO_EMAIL)
            return Result.success(Unit)
        }

        return Result.failure(
            IllegalArgumentException("Invalid email or password")
        )
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }
}