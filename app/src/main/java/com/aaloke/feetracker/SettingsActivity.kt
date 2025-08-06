package com.aaloke.feetracker

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    private lateinit var institutionNameEditText: TextInputEditText
    private lateinit var smsTemplateEditText: TextInputEditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = "App Settings"

        institutionNameEditText = findViewById(R.id.institution_name_edittext)
        smsTemplateEditText = findViewById(R.id.sms_template_edittext)
        saveButton = findViewById(R.id.save_settings_button)

        loadSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        // Load and display the current institution name
        val currentName = SettingsManager.getInstitutionName(this)
        institutionNameEditText.setText(currentName)

        // Load and display the current SMS template
        val currentTemplate = SettingsManager.getSmsTemplate(this)
        smsTemplateEditText.setText(currentTemplate)
    }

    private fun saveSettings() {
        val newName = institutionNameEditText.text.toString().trim()
        val newTemplate = smsTemplateEditText.text.toString().trim()

        if (newName.isNotEmpty() && newTemplate.isNotEmpty()) {
            SettingsManager.setInstitutionName(this, newName)
            SettingsManager.setSmsTemplate(this, newTemplate)
            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
            finish() // Close the settings screen after saving
        } else {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }
}