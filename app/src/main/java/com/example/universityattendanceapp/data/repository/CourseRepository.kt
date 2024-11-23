package com.example.universityattendanceapp.data.repository

import android.util.Log
import com.example.universityattendanceapp.data.model.Course
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CourseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val coursesCollection = firestore.collection("courses")

    suspend fun createCourse(
        name: String,
        code: String,
        instructorId: String,
        schedule: String
    ): Result<Course> {
        return try {
            val course = Course(
                id = UUID.randomUUID().toString(),
                name = name,
                code = code,
                instructorId = instructorId,
                schedule = schedule,
                enrolledStudents = emptyList()
            )
            
            Log.d("CourseRepository", "Creating course: $course")
            coursesCollection.document(course.id).set(course).await()
            
            // Update instructor's courses list
            firestore.collection("users")
                .document(instructorId)
                .update("courses", com.google.firebase.firestore.FieldValue.arrayUnion(course.id))
                .await()
            
            Log.d("CourseRepository", "Course created successfully")
            Result.success(course)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error creating course", e)
            Result.failure(e)
        }
    }

    suspend fun getAllAvailableCourses(): Result<List<Course>> {
        return try {
            Log.d("CourseRepository", "Fetching all courses")
            val snapshot = coursesCollection.get().await()
            val courses = snapshot.documents.mapNotNull { 
                it.toObject(Course::class.java)?.also { course ->
                    Log.d("CourseRepository", "Found course: $course")
                }
            }
            Log.d("CourseRepository", "Total courses found: ${courses.size}")
            Result.success(courses)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching courses", e)
            Result.failure(e)
        }
    }

    suspend fun getCourse(courseId: String): Result<Course> {
        return try {
            Log.d("CourseRepository", "Fetching course: $courseId")
            val document = coursesCollection.document(courseId).get().await()
            val course = document.toObject(Course::class.java)
            if (course != null) {
                Log.d("CourseRepository", "Course found: $course")
                Result.success(course)
            } else {
                Log.e("CourseRepository", "Course not found: $courseId")
                Result.failure(Exception("Course not found"))
            }
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error fetching course", e)
            Result.failure(e)
        }
    }

    suspend fun enrollStudent(courseId: String, studentId: String): Result<Unit> {
        return try {
            Log.d("CourseRepository", "Enrolling student $studentId in course $courseId")
            
            // First verify the course exists
            val course = getCourse(courseId).getOrNull()
            if (course == null) {
                Log.e("CourseRepository", "Course not found for enrollment")
                return Result.failure(Exception("Course not found"))
            }

            // Update course's enrolled students
            coursesCollection.document(courseId)
                .update("enrolledStudents", com.google.firebase.firestore.FieldValue.arrayUnion(studentId))
                .await()
            
            // Update student's courses list
            firestore.collection("users")
                .document(studentId)
                .update("courses", com.google.firebase.firestore.FieldValue.arrayUnion(courseId))
                .await()
            
            Log.d("CourseRepository", "Enrollment successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error during enrollment", e)
            Result.failure(e)
        }
    }

    suspend fun unenrollStudent(courseId: String, studentId: String): Result<Unit> {
        return try {
            Log.d("CourseRepository", "Unenrolling student $studentId from course $courseId")
            
            coursesCollection.document(courseId)
                .update("enrolledStudents", com.google.firebase.firestore.FieldValue.arrayRemove(studentId))
                .await()
            
            firestore.collection("users")
                .document(studentId)
                .update("courses", com.google.firebase.firestore.FieldValue.arrayRemove(courseId))
                .await()
            
            Log.d("CourseRepository", "Unenrollment successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("CourseRepository", "Error during unenrollment", e)
            Result.failure(e)
        }
    }
} 