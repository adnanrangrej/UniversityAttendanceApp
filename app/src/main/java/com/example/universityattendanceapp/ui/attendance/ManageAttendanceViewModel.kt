package com.example.universityattendanceapp.ui.attendance

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.AttendanceStatus
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.data.model.User
import com.example.universityattendanceapp.data.repository.AttendanceRepository
import com.example.universityattendanceapp.data.repository.CourseRepository
import com.example.universityattendanceapp.data.repository.UserRepository
import com.example.universityattendanceapp.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ManageAttendanceViewModel(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository = CourseRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository()
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])
    
    private val _courseState = MutableStateFlow<UiState<Course>>(UiState.Loading)
    val courseState: StateFlow<UiState<Course>> = _courseState

    private val _studentsState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val studentsState: StateFlow<UiState<List<User>>> = _studentsState

    init {
        loadCourseData()
    }

    private fun loadCourseData() {
        viewModelScope.launch {
            try {
                Log.d("ManageAttendanceViewModel", "Loading course data for courseId: $courseId")
                _courseState.value = UiState.Loading
                courseRepository.getCourse(courseId).fold(
                    onSuccess = { course ->
                        Log.d("ManageAttendanceViewModel", "Course loaded successfully: ${course.name}")
                        _courseState.value = UiState.Success(course)
                        loadEnrolledStudents(course.enrolledStudents)
                    },
                    onFailure = { 
                        Log.e("ManageAttendanceViewModel", "Failed to load course", it)
                        _courseState.value = UiState.Error(it.message ?: "Failed to load course") 
                    }
                )
            } catch (e: Throwable) {
                Log.e("ManageAttendanceViewModel", "Error loading course data", e)
                _courseState.value = UiState.Error(e.message ?: "Failed to load course")
            }
        }
    }

    private fun loadEnrolledStudents(studentIds: List<String>) {
        viewModelScope.launch {
            try {
                Log.d("ManageAttendanceViewModel", "Loading enrolled students: ${studentIds.size}")
                _studentsState.value = UiState.Loading
                val students = studentIds.mapNotNull { studentId ->
                    userRepository.getUser(studentId).getOrNull()
                }
                Log.d("ManageAttendanceViewModel", "Loaded ${students.size} students")
                _studentsState.value = UiState.Success(students)
            } catch (e: Throwable) {
                Log.e("ManageAttendanceViewModel", "Error loading students", e)
                _studentsState.value = UiState.Error(e.message ?: "Failed to load students")
            }
        }
    }

    private fun handleAttendanceError(e: Throwable) {
        Log.e("ManageAttendanceVM", "Error marking attendance", e)
        if (e.message?.contains("requires an index") == true) {
            _studentsState.value = UiState.Error(
                "Database index is being created. Please wait a few minutes and try again."
            )
        } else {
            _studentsState.value = UiState.Error(e.message ?: "Failed to mark attendance")
        }
    }

    fun markAttendance(studentId: String, status: AttendanceStatus) {
        viewModelScope.launch {
            try {
                Log.d("ManageAttendanceViewModel", "Marking attendance for student: $studentId with status: $status")
                attendanceRepository.markAttendance(
                    courseId = courseId,
                    studentId = studentId,
                    status = status
                ).onSuccess { 
                    loadCourseData()
                }.onFailure { 
                    handleAttendanceError(it)
                }
            } catch (e: Throwable) {
                handleAttendanceError(e)
            }
        }
    }
} 