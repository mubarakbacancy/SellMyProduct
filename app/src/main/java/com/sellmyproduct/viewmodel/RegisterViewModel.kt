package com.sellmyproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.sellmyproduct.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isRegisterSuccessful: Boolean = false,
    val errorMessage: String? = null
) {
    val isFormValid: Boolean
        get() = name.trim().isNotEmpty() &&
                email.trim().isNotEmpty() &&
                password.isNotEmpty() &&
                confirmPassword.isNotEmpty() &&
                nameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                name.trim().length >= 3 &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                password.length >= 6 &&
                password == confirmPassword
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        val nameTrimmed = name.trim()
        var nameError: String? = null
        
        if (nameTrimmed.isNotEmpty() && nameTrimmed.length < 3) {
            nameError = "Username must be at least 3 characters"
        }
        
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = nameError,
            errorMessage = null
        )
    }

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
        
        val confirmPasswordError = if (_uiState.value.confirmPassword.isNotEmpty() && 
            password != _uiState.value.confirmPassword) {
            "Passwords do not match"
        } else null
        
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            errorMessage = null
        )
    }

    fun updateConfirmPassword(confirmPassword: String) {
        var confirmPasswordError: String? = null
        
        if (confirmPassword.isNotEmpty() && confirmPassword != _uiState.value.password) {
            confirmPasswordError = "Passwords do not match"
        }
        
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = confirmPasswordError,
            errorMessage = null
        )
    }

    fun validateInputs(): Boolean {
        val name = _uiState.value.name.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        var isValid = true
        var nameError: String? = null
        var emailError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null

        // Username validation
        if (name.isEmpty()) {
            nameError = "Username is required"
            isValid = false
        } else if (name.length < 3) {
            nameError = "Username must be at least 3 characters"
            isValid = false
        }

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

        // Confirm Password validation
        if (confirmPassword.isEmpty()) {
            confirmPasswordError = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        }

        _uiState.value = _uiState.value.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )

        return isValid
    }

    fun register() {
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
            val name = _uiState.value.name.trim()

            authRepository.createUserWithEmailAndPassword(email, password)
                .onSuccess { user ->
                    // Create user document in Firestore
                    try {
                        val userData = hashMapOf(
                            "email" to email,
                            "username" to name,
                            "uid" to user.uid,
                            "displayName" to name, // Keep for backward compatibility
                            "createdAt" to Date(),
                            "updatedAt" to Date(),
                            "emailVerified" to false,
                            "accountStatus" to "active"
                        )

                        // Store in "users" collection with uid as document ID
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .await()

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isRegisterSuccessful = true
                        )
                    } catch (e: Exception) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Account created but failed to save user data: ${e.message}"
                        )
                    }
                }
                .onFailure { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthException -> {
                            when (exception.errorCode) {
                                "ERROR_INVALID_EMAIL" -> "Invalid email address"
                                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email"
                                "ERROR_WEAK_PASSWORD" -> "Password is too weak"
                                "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection"
                                else -> "Registration failed: ${exception.message}"
                            }
                        }
                        else -> "Registration failed: ${exception.message}"
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
        }
    }

    fun resetRegisterState() {
        _uiState.value = _uiState.value.copy(
            isRegisterSuccessful = false,
            errorMessage = null
        )
    }
}

