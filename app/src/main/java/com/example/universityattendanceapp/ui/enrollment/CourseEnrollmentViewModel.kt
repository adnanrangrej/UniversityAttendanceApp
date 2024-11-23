package com.example.universityattendanceapp.ui.enrollment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.data.repository.AuthRepository
import com.example.universityattendanceapp.data.repository.CourseRepository
import com.example.universityattendanceapp.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CourseEnrollmentViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _coursesState = MutableStateFlow<UiState<List<Course>>>(UiState.Loading)
    val coursesState: StateFlow<UiState<List<Course>>> = _coursesState

    private val _enrollmentState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))
    val enrollmentState: StateFlow<UiState<Unit>> = _enrollmentState

    init {
        loadAvailableCourses()
    }

    private fun loadAvailableCourses() {
        viewModelScope.launch {
            _coursesState.value = UiState.Loading
            courseRepository.getAllAvailableCourses().fold(
                onSuccess = { _coursesState.value = UiState.Success(it) },
                onFailure = { _coursesState.value = UiState.Error(it.message ?: "Failed to load courses") }
            )
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _enrollmentState.value = UiState.Loading
            val userId = authRepository.getCurrentUser()?.uid
            if (userId == null) {
                _enrollmentState.value = UiState.Error("User not authenticated")
                return@launch
            }

            courseRepository.enrollStudent(courseId, userId).fold(
                onSuccess = { 
                    _enrollmentState.value = UiState.Success(Unit)
                    loadAvailableCourses() // Refresh the course list
                },
                onFailure = { 
                    _enrollmentState.value = UiState.Error(it.message ?: "Failed to enroll in course") 
                }
            )
        }
    }

    fun unenrollFromCourse(courseId: String) {
        viewModelScope.launch {
            _enrollmentState.value = UiState.Loading
            val userId = authRepository.getCurrentUser()?.uid
            if (userId == null) {
                _enrollmentState.value = UiState.Error("User not authenticated")
                return@launch
            }

            courseRepository.unenrollStudent(courseId, userId).fold(
                onSuccess = { 
                    _enrollmentState.value = UiState.Success(Unit)
                    loadAvailableCourses() // Refresh the course list
                },
                onFailure = { 
                    _enrollmentState.value = UiState.Error(it.message ?: "Failed to unenroll from course") 
                }
            )
        }
    }
} 