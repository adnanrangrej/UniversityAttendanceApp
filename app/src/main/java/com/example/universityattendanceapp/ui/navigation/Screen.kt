package com.example.universityattendanceapp.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object StudentDashboard : Screen("student_dashboard")
    object InstructorDashboard : Screen("instructor_dashboard")
    object CreateCourse : Screen("create_course")
    object ManageAttendance : Screen("manage_attendance")
    object StudentAttendance : Screen("student_attendance")
    object CourseEnrollment : Screen("course_enrollment")
} 