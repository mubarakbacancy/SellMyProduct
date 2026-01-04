package com.sellmyproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.sellmyproduct.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = email.trim().isNotEmpty() && 
                password.isNotEmpty() &&
                emailError == null &&
                passwordError == null &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                password.length >= 6
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        val emailTrimmed = email.trim()
        var emailError: String? = null
        
        if (emailTrimmed.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            emailError = "Please enter a valid email"
        }
        
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = emailError,
            errorMessage = null
        )
    }

    fun updatePassword(password: String) {
        var passwordError: String? = null
        
        if (password.isNotEmpty() && password.length < 6) {
            passwordError = "Password must be at least 6 characters"
        }
        
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError,
            errorMessage = null
        )
    }

    fun validateInputs(): Boolean {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var isValid = true
        var emailError: String? = null
        var passwordError: String? = null

        // Email validation
        if (email.isEmpty()) {
            emailError = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Please enter a valid email"
            isValid = false
        }

        // Password validation
        if (password.isEmpty()) {
            passwordError = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        }

        _uiState.value = _uiState.value.copy(
            emailError = emailError,
            passwordError = passwordError
        )

        return isValid
    }

    fun login() {
        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val email = _uiState.value.email.trim()
            val password = _uiState.value.password

            authRepository.signInWithEmailAndPassword(email, password)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    )
                }
                .onFailure { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                                "ERROR_WRONG_PASSWORD" -> "Wrong password"
                                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                                "ERROR_USER_DISABLED" -> "This account has been disabled"
                                "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later"
                                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection"
                                else -> "Login failed: ${exception.message}"
                            }
                        }
                        else -> "Login failed: ${exception.message}"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
        }
    }

    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(
            isLoginSuccessful = false,
            errorMessage = null
        )
    }
}


