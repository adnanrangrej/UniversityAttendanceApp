package com.example.universityattendanceapp

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import android.util.Log

class UniversityAttendanceApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize Firebase
            FirebaseApp.initializeApp(this)
            
            // Check Google Play Services
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(this)
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                Log.e("UniversityApp", "Google Play Services not available: $resultCode")
            } else {
                Log.d("UniversityApp", "Google Play Services available")
            }
        } catch (e: Exception) {
            Log.e("UniversityApp", "Error initializing app", e)
        }
    }

    companion object {
        fun checkPlayServices(activity: android.app.Activity): Boolean {
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(activity)
            
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                if (availability.isUserResolvableError(resultCode)) {
                    availability.getErrorDialog(activity, resultCode, 9000)?.show()
                }
                return false
            }
            return true
        }
    }
} 