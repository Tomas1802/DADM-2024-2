package co.edu.unal.tictactoe.services

import android.content.Context
import java.util.UUID

class PlayerIdManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "UserPreferences"
        private const val KEY_USER_ID = "UserId"
    }

    fun getOrCreateUserId(): String {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString(KEY_USER_ID, null)

        if (userId == null) {
            // Generate a new UUID
            userId = UUID.randomUUID().toString()

            // Save it to SharedPreferences
            sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
        }

        return userId
    }
}