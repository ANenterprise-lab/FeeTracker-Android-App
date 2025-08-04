package com.aaloke.feetracker

import android.content.Context
import android.content.SharedPreferences

object PremiumManager {

    private const val PREFS_NAME = "premium_prefs"
    private const val IS_PREMIUM_KEY = "is_premium"

    // This keeps track of the temporary unlock from watching an ad.
    var isTemporarilyUnlocked = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Checks if the user has bought premium (permanent unlock).
    fun isPremiumUser(context: Context): Boolean {
        return getPrefs(context).getBoolean(IS_PREMIUM_KEY, false)
    }

    // Call this when the user successfully buys the premium version.
    fun setPremiumUser(context: Context, isPremium: Boolean) {
        getPrefs(context).edit().putBoolean(IS_PREMIUM_KEY, isPremium).apply()
    }
}