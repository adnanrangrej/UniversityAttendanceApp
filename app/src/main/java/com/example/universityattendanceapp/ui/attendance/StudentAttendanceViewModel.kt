package com.example.universityattendanceapp.ui.attendance

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universityattendanceapp.data.model.Attendance
import com.example.universityattendanceapp.data.model.AttendanceStatus
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.data.repository.AttendanceRepository
import com.example.universityattendanceapp.data.repository.AuthRepository
import com.example.universityattendanceapp.data.repository.CourseRepository
import com.example.universityattendanceapp.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentAttendanceViewModel(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository = CourseRepository(),
    private val attendanceRepository: AttendanceRepository = AttendanceRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val courseId: String = checkNotNull(savedStateHandle["courseId"])
    
    private val _courseState = MutableStateFlow<UiState<Course>>(UiState.Loading)
    val courseState: StateFlow<UiState<Course>> = _courseState

    private val _attendanceState = MutableStateFlow<UiState<List<Attendance>>>(UiState.Loading)
    val attendanceState: StateFlow<UiState<List<Attendance>>> = _attendanceState

    private val _stats = MutableStateFlow(AttendanceStats())
    val stats: StateFlow<AttendanceStats> = _stats

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUser()?.uid
                if (userId == null) {
                    _attendanceState.value = UiState.Error("User not authenticated")
                    return@launch
                }

                // Load course details
                courseRepository.getCourse(courseId).fold(
                    onSuccess = { _courseState.value = UiState.Success(it) },
                    onFailure = { _courseState.value = UiState.Error(it.message ?: "Failed to load course") }
                )

                // Load attendance records
                attendanceRepository.getStudentAttendance(userId, courseId).fold(
                    onSuccess = { attendances ->
                        Log.d("StudentAttendanceVM", "Loaded ${attendances.size} attendance records")
                        _attendanceState.value = UiState.Success(attendances)
                        
                        // Calculate and update stats
                        val present = attendances.count { it.status == AttendanceStatus.PRESENT }
                        val absent = attendances.count { it.status == AttendanceStatus.ABSENT }
                        val late = attendances.count { it.status == AttendanceStatus.LATE }
                        
                        Log.d("StudentAttendanceVM", "Calculated stats - Present: $present, Absent: $absent, Late: $late")
                        
                        _stats.value = AttendanceStats(present, absent, late)
                    },
                    onFailure = { 
                        Log.e("StudentAttendanceVM", "Failed to load attendance", it)
                        _attendanceState.value = UiState.Error(it.message ?: "Failed to load attendance") 
                    }
                )
            } catch (e: Exception) {
                Log.e("StudentAttendanceVM", "Error loading data", e)
                _attendanceState.value = UiState.Error(e.message ?: "Failed to load data")
            }
        }
    }

    fun refreshData() {
        loadData()
    }
}

data class AttendanceStats(
    val present: Int = 0,
    val absent: Int = 0,
    val late: Int = 0
) {
    val total: Int get() = present + absent + late
    val presentPercentage: Float get() = if (total > 0) (present + (late * 0.5f)) * 100f / total else 0f
} 