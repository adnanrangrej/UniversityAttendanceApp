package com.example.universityattendanceapp.data.repository

import android.util.Log
import com.example.universityattendanceapp.data.model.Attendance
import com.example.universityattendanceapp.data.model.AttendanceStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import java.util.Calendar

class AttendanceRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val attendanceCollection = firestore.collection("attendance")

    suspend fun markAttendance(
        courseId: String,
        studentId: String,
        status: AttendanceStatus,
        date: Date = Date()
    ): Result<Attendance> {
        return try {
            Log.d("AttendanceRepo", "Marking attendance for student: $studentId in course: $courseId")
            
            // Simplified query to avoid index requirement while checking for existing attendance
            val existingAttendance = attendanceCollection
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
                .documents
                .firstOrNull { doc ->
                    val attendanceDate = (doc.get("date") as? com.google.firebase.Timestamp)?.toDate()
                    if (attendanceDate != null) {
                        isSameDay(attendanceDate, date)
                    } else false
                }

            if (existingAttendance != null) {
                // Update existing attendance
                val attendance = Attendance(
                    id = existingAttendance.id,
                    courseId = courseId,
                    studentId = studentId,
                    date = date,
                    status = status
                )
                attendanceCollection.document(existingAttendance.id).set(attendance).await()
                Log.d("AttendanceRepo", "Updated existing attendance: $attendance")
                Result.success(attendance)
            } else {
                // Create new attendance
                val attendance = Attendance(
                    id = UUID.randomUUID().toString(),
                    courseId = courseId,
                    studentId = studentId,
                    date = date,
                    status = status
                )
                attendanceCollection.document(attendance.id).set(attendance).await()
                Log.d("AttendanceRepo", "Created new attendance: $attendance")
                Result.success(attendance)
            }
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Error marking attendance", e)
            Result.failure(e)
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    suspend fun getAttendanceForCourse(courseId: String): Result<List<Attendance>> {
        return try {
            Log.d("AttendanceRepo", "Fetching attendance for course: $courseId")
            val snapshot = attendanceCollection
                .whereEqualTo("courseId", courseId)
                .get()
                .await()
            
            val attendanceList = snapshot.documents.mapNotNull { 
                it.toObject(Attendance::class.java)
            }.sortedByDescending { it.date }
            
            Log.d("AttendanceRepo", "Found ${attendanceList.size} attendance records")
            Result.success(attendanceList)
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Error fetching attendance", e)
            Result.failure(e)
        }
    }

    suspend fun getStudentAttendance(studentId: String, courseId: String): Result<List<Attendance>> {
        return try {
            Log.d("AttendanceRepo", "Fetching attendance for student: $studentId in course: $courseId")
            
            // Use a simpler query while index is building
            val snapshot = attendanceCollection
                .whereEqualTo("courseId", courseId)
                .whereEqualTo("studentId", studentId)
                .get()
                .await()
            
            val attendanceList = snapshot.documents.mapNotNull { 
                it.toObject(Attendance::class.java)
            }.sortedByDescending { it.date }
            
            Log.d("AttendanceRepo", "Found ${attendanceList.size} attendance records")
            Result.success(attendanceList)
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "Error fetching attendance", e)
            Result.failure(e)
        }
    }
} 