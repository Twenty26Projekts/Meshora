package com.bitchat.android.onboarding

import android.content.Context

/** Persists whether the first-run welcome screen has been completed. */
object WelcomePreferenceManager {
    private const val PREFS_NAME = "bitchat_settings"
    private const val KEY_WELCOME_COMPLETE = "welcome_complete_v1"

    fun isComplete(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_WELCOME_COMPLETE, false)

    fun markComplete(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_WELCOME_COMPLETE, true)
            .apply()
    }
}
