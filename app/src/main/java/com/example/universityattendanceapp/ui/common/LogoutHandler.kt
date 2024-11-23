package com.example.universityattendanceapp.ui.common

import androidx.navigation.NavController
import com.example.universityattendanceapp.ui.auth.AuthViewModel
import com.example.universityattendanceapp.ui.navigation.Screen

object LogoutHandler {
    fun handleLogout(viewModel: AuthViewModel) {
        viewModel.signOut()
    }
} 