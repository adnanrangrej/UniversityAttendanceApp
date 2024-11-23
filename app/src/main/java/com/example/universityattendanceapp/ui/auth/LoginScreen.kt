package com.example.universityattendanceapp.ui.auth

import androidx.compose.foundation.layout.*
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
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") }
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
                    .padding(bottom = 32.dp)
            )

            Button(
                onClick = { viewModel.signIn(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            TextButton(
                onClick = { 
                    viewModel.resetAuthState()
                    navController.navigate(Screen.Register.route)
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Don't have an account? Register")
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
                        LaunchedEffect(Unit) {
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