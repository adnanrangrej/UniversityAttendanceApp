package com.example.universityattendanceapp.ui.instructor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.data.model.User
import com.example.universityattendanceapp.data.repository.AuthRepository
import com.example.universityattendanceapp.data.repository.CourseRepository
import com.example.universityattendanceapp.data.repository.UserRepository
import com.example.universityattendanceapp.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InstructorViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val courseRepository: CourseRepository = CourseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val uiState: StateFlow<UiState<User>> = _uiState

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUser()?.uid
            if (userId != null) {
                userRepository.getUser(userId)
                    .fold(
                        onSuccess = { _uiState.value = UiState.Success(it) },
                        onFailure = { _uiState.value = UiState.Error(it.message ?: "Failed to load user data") }
                    )
            } else {
                _uiState.value = UiState.Error("User not authenticated")
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        // Navigation will be handled by the UI layer
    }

    suspend fun getCourseDetails(courseId: String): Result<Course> {
        return courseRepository.getCourse(courseId)
    }
} 