package com.example.universityattendanceapp.ui.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StudentAttendanceViewModelFactory(
    private val courseId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentAttendanceViewModel::class.java)) {
            return StudentAttendanceViewModel(
                SavedStateHandle(mapOf("courseId" to courseId))
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 