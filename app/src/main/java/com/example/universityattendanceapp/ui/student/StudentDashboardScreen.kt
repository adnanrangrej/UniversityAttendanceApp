package com.example.universityattendanceapp.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.ui.common.UiState
import com.example.universityattendanceapp.ui.navigation.Screen
import com.example.universityattendanceapp.ui.common.LogoutHandler
import com.example.universityattendanceapp.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    navController: NavController,
    viewModel: StudentViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Dashboard") },
                actions = {
                    IconButton(
                        onClick = { 
                            LogoutHandler.handleLogout(authViewModel)
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Welcome, ${state.data.name}",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    item {
                        Text(
                            text = "Your Courses",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(state.data.courses) { courseId ->
                        CourseCard(
                            courseId = courseId,
                            onViewAttendance = { 
                                navController.navigate(Screen.StudentAttendance.route + "/$courseId")
                            }
                        )
                    }

                    item {
                        Button(
                            onClick = { navController.navigate(Screen.CourseEnrollment.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text("Enroll in New Course")
                        }
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    courseId: String,
    onViewAttendance: () -> Unit,
    viewModel: StudentViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var course by remember { mutableStateOf<Course?>(null) }
    
    LaunchedEffect(courseId) {
        viewModel.getCourseDetails(courseId).fold(
            onSuccess = { course = it },
            onFailure = { /* Handle error */ }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            if (course != null) {
                Text(
                    text = course!!.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Code: ${course!!.code}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Schedule: ${course!!.schedule}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Text(
                    text = "Loading course details...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onViewAttendance,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("View Attendance")
            }
        }
    }
} 