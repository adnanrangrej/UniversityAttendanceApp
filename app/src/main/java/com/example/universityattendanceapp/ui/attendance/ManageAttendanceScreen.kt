package com.example.universityattendanceapp.ui.attendance

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
import com.example.universityattendanceapp.data.model.AttendanceStatus
import com.example.universityattendanceapp.data.model.User
import com.example.universityattendanceapp.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAttendanceScreen(
    navController: NavController,
    viewModel: ManageAttendanceViewModel
) {
    val courseState by viewModel.courseState.collectAsStateWithLifecycle()
    val studentsState by viewModel.studentsState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Attendance") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        when {
            courseState is UiState.Loading || studentsState is UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            courseState is UiState.Error -> {
                Text(
                    text = (courseState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
            courseState is UiState.Success && studentsState is UiState.Success -> {
                val course = (courseState as UiState.Success).data
                val students = (studentsState as UiState.Success).data

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = course.name,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    items(students) { student ->
                        StudentAttendanceCard(
                            student = student,
                            onMarkAttendance = { status ->
                                viewModel.markAttendance(student.id, status)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentAttendanceCard(
    student: User,
    onMarkAttendance: (AttendanceStatus) -> Unit
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
                text = student.name,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttendanceStatus.values().forEach { status ->
                    Button(
                        onClick = { onMarkAttendance(status) },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text(status.name)
                    }
                }
            }
        }
    }
} 