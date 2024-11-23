package com.example.universityattendanceapp.data.repository

import android.util.Log
import com.example.universityattendanceapp.data.model.User
import com.example.universityattendanceapp.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    suspend fun createUser(userId: String, email: String, name: String, role: UserRole): Result<User> {
        return try {
            Log.d("UserRepository", "Creating user: $userId, $email, $name, $role")
            val user = User(
                id = userId,
                email = email,
                name = name,
                role = role,
                courses = emptyList()
            )
            
            // Convert user object to a map for Firestore
            val userMap = mapOf(
                "id" to user.id,
                "email" to user.email,
                "name" to user.name,
                "role" to user.role.name,
                "courses" to user.courses
            )
            
            usersCollection.document(userId).set(userMap).await()
            Log.d("UserRepository", "User created successfully")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("UserRepository", "Error creating user", e)
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User> {
        return try {
            Log.d("UserRepository", "Getting user: $userId")
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                val data = document.data
                if (data != null) {
                    val user = User(
                        id = data["id"] as String,
                        email = data["email"] as String,
                        name = data["name"] as String,
                        role = UserRole.valueOf(data["role"] as String),
                        courses = (data["courses"] as? List<String>) ?: emptyList()
                    )
                    Log.d("UserRepository", "User found: $user")
                    Result.success(user)
                } else {
                    Log.e("UserRepository", "User document data is null")
                    Result.failure(Exception("User data not found"))
                }
            } else {
                Log.e("UserRepository", "User document does not exist")
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Error getting user", e)
            Result.failure(e)
        }
    }
} 