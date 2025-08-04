package com.aaloke.feetracker

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class SettingsActivity : AppCompatActivity() {

    private lateinit var smsTemplateEditText: TextInputEditText
    private lateinit var saveTemplateButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set the title for the activity
        title = "Settings"

        smsTemplateEditText = findViewById(R.id.sms_template_edittext)
        saveTemplateButton = findViewById(R.id.save_template_button)

        // Load the currently saved template and display it
        loadSmsTemplate()

        saveTemplateButton.setOnClickListener {
            saveSmsTemplate()
        }
    }

    private fun loadSmsTemplate() {
        val currentTemplate = SettingsManager.getSmsTemplate(this)
        smsTemplateEditText.setText(currentTemplate)
    }

    private fun saveSmsTemplate() {
        val newTemplate = smsTemplateEditText.text.toString().trim()
        if (newTemplate.isNotEmpty()) {
            SettingsManager.setSmsTemplate(this, newTemplate)
            Toast.makeText(this, "Template saved successfully!", Toast.LENGTH_SHORT).show()
            finish() // Close the settings screen after saving
        } else {
            Toast.makeText(this, "Template cannot be empty.", Toast.LENGTH_SHORT).show()
        }
    }
}