package com.sellmyproduct.viewmodel

import androidx.lifecycle.ViewModel
import com.sellmyproduct.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.currentUser != null
    }
}

