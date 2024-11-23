package com.example.universityattendanceapp.ui.course

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.data.repository.AuthRepository
import com.example.universityattendanceapp.data.repository.CourseRepository
import com.example.universityattendanceapp.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateCourseViewModel(
    private val courseRepository: CourseRepository = CourseRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _createCourseState = MutableStateFlow<UiState<Course>>(UiState.Success(Course()))
    val createCourseState: StateFlow<UiState<Course>> = _createCourseState

    fun createCourse(name: String, code: String, schedule: String) {
        viewModelScope.launch {
            try {
                _createCourseState.value = UiState.Loading
                val instructorId = authRepository.getCurrentUser()?.uid
                    ?: throw Exception("User not authenticated")

                Log.d("CreateCourseViewModel", "Creating course with name: $name, code: $code")
                
                courseRepository.createCourse(
                    name = name,
                    code = code,
                    instructorId = instructorId,
                    schedule = schedule
                ).fold(
                    onSuccess = { course ->
                        Log.d("CreateCourseViewModel", "Course created successfully: ${course.id}")
                        _createCourseState.value = UiState.Success(course)
                    },
                    onFailure = { error ->
                        Log.e("CreateCourseViewModel", "Failed to create course", error)
                        _createCourseState.value = UiState.Error(error.message ?: "Failed to create course")
                    }
                )
            } catch (e: Exception) {
                Log.e("CreateCourseViewModel", "Error creating course", e)
                _createCourseState.value = UiState.Error(e.message ?: "Failed to create course")
            }
        }
    }

    fun setError(message: String) {
        _createCourseState.value = UiState.Error(message)
    }
} 