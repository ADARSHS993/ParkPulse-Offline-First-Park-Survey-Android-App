package com.example.parkplus.presentation.auth

data class LoginUiState(
    val email : String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)
