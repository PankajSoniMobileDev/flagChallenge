package com.appadore.flagchallenge.util

import android.content.Context
import com.appadore.flagchallenge.R

object Utils {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext // Store the application context
    }

    fun getFlagDrawable(countryCode: String): Int {
        // Convert country code to lowercase to match the drawable resource naming convention
        val resourceName = countryCode.lowercase()

        // Get the resource ID for the drawable based on the country code
        return when {
            resourceExists(resourceName, appContext) -> {
                // Return the drawable resource ID
                appContext.resources.getIdentifier(resourceName, "drawable", appContext.packageName)
            }
            else -> {
                // Return a default flag drawable if not found (optional)
                R.drawable.ls // Replace with your default flag drawable resource
            }
        }
    }

    // Helper function to check if the resource exists
    private fun resourceExists(resourceName: String, context: Context): Boolean {
        return try {
            val resId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            resId != 0 // Return true if the resource ID is valid
        } catch (e: Exception) {
            false // Return false if any exception occurs
        }
    }

    fun saveTargetTimeInStorage(context: Context,targetTimeInMillis: Long) {
        val sharedPreferences = context.getSharedPreferences("ChallengePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong("target_time", targetTimeInMillis)
        editor.apply()
    }

    fun getTargetTimeFromStorage(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences("ChallengePrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("target_time", -1L)
    }

}