package com.example.universityattendanceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.universityattendanceapp.data.model.UserRole
import com.example.universityattendanceapp.ui.auth.AuthViewModel
import com.example.universityattendanceapp.ui.auth.LoginScreen
import com.example.universityattendanceapp.ui.auth.RegisterScreen
import com.example.universityattendanceapp.ui.navigation.Screen
import com.example.universityattendanceapp.ui.theme.UniversityAttendanceAppTheme
import com.example.universityattendanceapp.ui.student.StudentDashboardScreen
import com.example.universityattendanceapp.ui.student.StudentViewModel
import com.example.universityattendanceapp.ui.instructor.InstructorDashboardScreen
import com.example.universityattendanceapp.ui.instructor.InstructorViewModel
import com.example.universityattendanceapp.ui.course.CreateCourseViewModel
import com.example.universityattendanceapp.ui.course.CreateCourseScreen
import com.example.universityattendanceapp.ui.attendance.ManageAttendanceViewModel
import com.example.universityattendanceapp.ui.attendance.ManageAttendanceScreen
import com.example.universityattendanceapp.ui.attendance.StudentAttendanceViewModel
import com.example.universityattendanceapp.ui.attendance.StudentAttendanceScreen
import com.example.universityattendanceapp.ui.enrollment.CourseEnrollmentViewModel
import com.example.universityattendanceapp.ui.enrollment.CourseEnrollmentScreen
import com.example.universityattendanceapp.ui.attendance.ManageAttendanceViewModelFactory
import com.example.universityattendanceapp.ui.attendance.StudentAttendanceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check Google Play Services availability
        UniversityAttendanceApp.checkPlayServices(this)

        setContent {
            UniversityAttendanceAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    val currentUser by authViewModel.currentUser.collectAsState()

    // This LaunchedEffect will handle navigation when user is null (logged out)
    LaunchedEffect(currentUser) {
        if (currentUser == null) {
            navController.navigate(Screen.Login.route) {
                // Clear the back stack when logging out
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(Screen.StudentDashboard.route) {
            val studentViewModel: StudentViewModel = viewModel()
            StudentDashboardScreen(
                navController = navController,
                viewModel = studentViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.InstructorDashboard.route) {
            val instructorViewModel: InstructorViewModel = viewModel()
            InstructorDashboardScreen(
                navController = navController,
                viewModel = instructorViewModel,
                authViewModel = authViewModel
            )
        }
        composable(Screen.CreateCourse.route) {
            val createCourseViewModel: CreateCourseViewModel = viewModel()
            CreateCourseScreen(navController, createCourseViewModel)
        }
        composable(
            route = Screen.ManageAttendance.route + "/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val factory = ManageAttendanceViewModelFactory(courseId)
            val manageAttendanceViewModel: ManageAttendanceViewModel = viewModel(factory = factory)
            ManageAttendanceScreen(navController, manageAttendanceViewModel)
        }
        composable(
            route = Screen.StudentAttendance.route + "/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val factory = StudentAttendanceViewModelFactory(courseId)
            val studentAttendanceViewModel: StudentAttendanceViewModel = viewModel(factory = factory)
            StudentAttendanceScreen(navController, studentAttendanceViewModel)
        }
        composable(Screen.CourseEnrollment.route) {
            val courseEnrollmentViewModel: CourseEnrollmentViewModel = viewModel()
            CourseEnrollmentScreen(navController, courseEnrollmentViewModel)
        }
    }
}