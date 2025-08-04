package com.aaloke.feetracker

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREFS_NAME = "settings_prefs"
    private const val SMS_TEMPLATE_KEY = "sms_template"

    // This is the default message if the user hasn't set one
    private const val DEFAULT_SMS_TEMPLATE = "Dear {student_name}, your fees for the month of {month} are pending."

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSmsTemplate(context: Context): String {
        return getPrefs(context).getString(SMS_TEMPLATE_KEY, DEFAULT_SMS_TEMPLATE) ?: DEFAULT_SMS_TEMPLATE
    }

    fun setSmsTemplate(context: Context, template: String) {
        getPrefs(context).edit().putString(SMS_TEMPLATE_KEY, template).apply()
    }
}