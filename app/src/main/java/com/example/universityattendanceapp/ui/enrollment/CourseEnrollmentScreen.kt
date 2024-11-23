package com.example.universityattendanceapp.ui.enrollment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.universityattendanceapp.data.model.Course
import com.example.universityattendanceapp.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEnrollmentScreen(
    navController: NavController,
    viewModel: CourseEnrollmentViewModel
) {
    val coursesState by viewModel.coursesState.collectAsStateWithLifecycle()
    val enrollmentState by viewModel.enrollmentState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Courses") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = coursesState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.data) { course ->
                        CourseEnrollmentCard(
                            course = course,
                            onEnroll = { viewModel.enrollInCourse(course.id) },
                            onUnenroll = { viewModel.unenrollFromCourse(course.id) }
                        )
                    }
                }

                // Show enrollment status
                if (enrollmentState is UiState.Error) {
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text((enrollmentState as UiState.Error).message)
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseEnrollmentCard(
    course: Course,
    onEnroll: () -> Unit,
    onUnenroll: () -> Unit
) {
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
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Code: ${course.code}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Schedule: ${course.schedule}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onEnroll,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Enroll")
                }
                OutlinedButton(onClick = onUnenroll) {
                    Text("Unenroll")
                }
            }
        }
    }
} 