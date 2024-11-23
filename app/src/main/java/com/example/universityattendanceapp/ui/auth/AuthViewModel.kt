package com.example.universityattendanceapp.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.UserRole
import com.example.universityattendanceapp.data.repository.AuthRepository
import com.example.universityattendanceapp.data.repository.UserRepository
import com.example.universityattendanceapp.ui.common.UiState
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val authState: StateFlow<UiState<Unit>> = _authState

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole: StateFlow<UserRole?> = _userRole

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        try {
            val user = authRepository.getCurrentUser()
            Log.d("AuthViewModel", "Current user: ${user?.uid}")
            _currentUser.value = user
            if (user != null) {
                loadUserRole(user.uid)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error checking current user", e)
        }
    }

    private fun loadUserRole(userId: String) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "Loading user role for: $userId")
            userRepository.getUser(userId).fold(
                onSuccess = { user ->
                    Log.d("AuthViewModel", "User role loaded: ${user.role}")
                    _userRole.value = user.role
                },
                onFailure = {
                    Log.e("AuthViewModel", "Failed to load user role", it)
                    _authState.value = UiState.Error(it.message ?: "Failed to load user role")
                }
            )
        }
    }

    fun signUp(email: String, password: String, name: String, role: UserRole) {
        viewModelScope.launch {
            try {
                Log.d("AuthViewModel", "Starting sign up process")
                _authState.value = UiState.Loading

                // Create Firebase Auth user
                val authResult = authRepository.signUp(email, password).getOrThrow()
                Log.d("AuthViewModel", "Firebase Auth user created: ${authResult.uid}")

                // Create user document in Firestore
                val userResult = userRepository.createUser(
                    userId = authResult.uid,
                    email = email,
                    name = name,
                    role = role
                ).getOrThrow()

                Log.d("AuthViewModel", "User document created in Firestore")

                // Update state
                _currentUser.value = authResult
                _userRole.value = role
                _authState.value = UiState.Success(Unit)

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign up failed", e)
                _authState.value = UiState.Error(e.message ?: "Registration failed")
                // Clean up if user creation fails
                _currentUser.value?.let { authRepository.signOut() }
                _currentUser.value = null
                _userRole.value = null
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = UiState.Loading
                val user = authRepository.signIn(email, password).getOrThrow()
                _currentUser.value = user
                loadUserRole(user.uid)
                _authState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Sign in failed", e)
                _authState.value = UiState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    fun signOut() {
        try {
            authRepository.signOut()
            _currentUser.value = null
            _userRole.value = null
            _authState.value = UiState.Success(Unit)
            // Clear any other state if needed
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Sign out failed", e)
            _authState.value = UiState.Error(e.message ?: "Sign out failed")
        }
    }

    fun setError(message: String) {
        _authState.value = UiState.Error(message)
    }

    fun resetAuthState() {
        _authState.value = UiState.Success(Unit)
    }
} 