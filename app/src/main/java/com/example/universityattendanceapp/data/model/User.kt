package com.example.universityattendanceapp.data.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: UserRole = UserRole.STUDENT,
    val courses: List<String> = emptyList()
)

enum class UserRole {
    STUDENT,
    INSTRUCTOR
} 