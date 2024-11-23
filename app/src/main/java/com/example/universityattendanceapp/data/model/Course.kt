package com.example.universityattendanceapp.data.model

data class Course(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val instructorId: String = "",
    val schedule: String = "",
    val enrolledStudents: List<String> = emptyList()
) 