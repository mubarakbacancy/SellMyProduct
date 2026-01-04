package com.sellmyproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sellmyproduct.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class HomeUiState(
    val welcomeMessage: String = "Welcome!",
    val userEmail: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadUserData() {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    val userEmail = currentUser.email ?: ""
                    
                    // Try to get username from Firestore (fallback to displayName for backward compatibility)
                    val userDoc = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()

                    val username = userDoc.getString("username") 
                        ?: userDoc.getString("displayName") 
                        ?: ""
                    
                    val welcomeMessage = if (username.isNotEmpty()) {
                        "Welcome, $username!"
                    } else {
                        "Welcome!"
                    }

                    _uiState.value = _uiState.value.copy(
                        welcomeMessage = welcomeMessage,
                        userEmail = userEmail,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        welcomeMessage = "Welcome!",
                        userEmail = "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Fallback to email if Firestore fails
                val currentUser = authRepository.currentUser
                val userEmail = currentUser?.email ?: ""
                _uiState.value = _uiState.value.copy(
                    welcomeMessage = "Welcome!",
                    userEmail = userEmail,
                    isLoading = false
                )
            }
        }
    }

    fun logout() {
        authRepository.signOut()
    }
}

