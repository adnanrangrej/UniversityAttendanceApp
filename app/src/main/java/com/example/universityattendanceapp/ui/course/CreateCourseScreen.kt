package com.example.universityattendanceapp.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.universityattendanceapp.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(
    navController: NavController,
    viewModel: CreateCourseViewModel
) {
    var courseName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    
    val createCourseState by viewModel.createCourseState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Course") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = courseCode,
                onValueChange = { courseCode = it },
                label = { Text("Course Code") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = schedule,
                onValueChange = { schedule = it },
                label = { Text("Schedule (e.g., Mon/Wed 10:00-11:30)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { 
                    if (courseName.isBlank() || courseCode.isBlank() || schedule.isBlank()) {
                        viewModel.setError("All fields are required")
                    } else {
                        viewModel.createCourse(courseName, courseCode, schedule)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Course")
            }

            when (createCourseState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text(
                    text = (createCourseState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
                is UiState.Success -> {
                    val course = (createCourseState as UiState.Success).data
                    LaunchedEffect(course) {
                        if (course.id.isNotEmpty()) {
                            navController.navigateUp()
                        }
                    }
                }
            }
        }
    }
} 