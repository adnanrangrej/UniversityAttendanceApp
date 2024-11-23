package com.example.universityattendanceapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.universityattendanceapp.data.model.UserRole
import com.example.universityattendanceapp.ui.common.UiState
import com.example.universityattendanceapp.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register") },
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
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UserRole.values().forEach { role ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                        Text(role.name)
                    }
                }
            }

            Button(
                onClick = { 
                    if (email.isBlank() || password.isBlank() || name.isBlank()) {
                        // Show error for empty fields
                        viewModel.setError("All fields are required")
                    } else if (password.length < 6) {
                        // Firebase requires minimum 6 characters for password
                        viewModel.setError("Password must be at least 6 characters")
                    } else {
                        viewModel.signUp(email, password, name, selectedRole)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            TextButton(
                onClick = { 
                    // Reset auth state before navigating back
                    viewModel.resetAuthState()
                    navController.navigateUp()
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Already have an account? Login")
            }

            when (authState) {
                is UiState.Loading -> CircularProgressIndicator()
                is UiState.Error -> Text(
                    text = (authState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
                is UiState.Success -> {
                    val user = viewModel.currentUser.collectAsState().value
                    val role = viewModel.userRole.collectAsState().value
                    if (user != null && role != null) {
                        LaunchedEffect(user, role) {
                            val route = when (role) {
                                UserRole.STUDENT -> Screen.StudentDashboard.route
                                UserRole.INSTRUCTOR -> Screen.InstructorDashboard.route
                            }
                            navController.navigate(route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            }
        }
    }
} 