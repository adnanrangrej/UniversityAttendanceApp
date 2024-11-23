package com.example.universityattendanceapp.data.model

import java.util.Date

data class Attendance(
    val id: String = "",
    val courseId: String = "",
    val studentId: String = "",
    val date: Date = Date(),
    val status: AttendanceStatus = AttendanceStatus.ABSENT
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE
} 