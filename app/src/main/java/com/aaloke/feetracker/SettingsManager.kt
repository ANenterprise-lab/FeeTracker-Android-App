package com.aaloke.feetracker

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {

    private const val PREFS_NAME = "settings_prefs"
    private const val SMS_TEMPLATE_KEY = "sms_template"
    private const val INSTITUTION_NAME_KEY = "institution_name"
    private const val DEFAULT_INSTITUTION_NAME = "Your Institution Name"
    private const val DEFAULT_SMS_TEMPLATE = "Dear {student_name}, your fees for {month} are pending."

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSmsTemplate(context: Context): String {
        return getPrefs(context).getString(SMS_TEMPLATE_KEY, DEFAULT_SMS_TEMPLATE) ?: DEFAULT_SMS_TEMPLATE
    }

    fun setSmsTemplate(context: Context, template: String) {
        getPrefs(context).edit().putString(SMS_TEMPLATE_KEY, template).apply()
    }

    fun getInstitutionName(context: Context): String {
        return getPrefs(context).getString(INSTITUTION_NAME_KEY, DEFAULT_INSTITUTION_NAME) ?: DEFAULT_INSTITUTION_NAME
    }

    fun setInstitutionName(context: Context, name: String) {
        getPrefs(context).edit().putString(INSTITUTION_NAME_KEY, name).apply()
    }
}