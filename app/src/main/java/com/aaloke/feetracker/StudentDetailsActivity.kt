package com.aaloke.feetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class StudentDetailsActivity : AppCompatActivity() {

    private var student: Student? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_details)

        // --- These are the input fields from your layout ---
        val nameEditText = findViewById<TextInputEditText>(R.id.nameEditText)
        val classNameEditText = findViewById<TextInputEditText>(R.id.classNameEditText)
        val feeAmountEditText = findViewById<TextInputEditText>(R.id.feeAmountEditText)
        // **HERE IS THE NEW PHONE NUMBER FIELD**
        val phoneNumberEditText = findViewById<TextInputEditText>(R.id.phoneNumberEditText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // This gets the student data if you are editing an existing student
        student = intent.getSerializableExtra("STUDENT_EXTRA") as? Student

        // This fills the input fields with the student's data
        student?.let {
            nameEditText.setText(it.name)
            classNameEditText.setText(it.className)
            feeAmountEditText.setText(it.feeAmount.toString())
            // **HERE WE SET THE PHONE NUMBER TEXT**
            phoneNumberEditText.setText(it.phoneNumber)
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val className = classNameEditText.text.toString()
            val feeAmount = feeAmountEditText.text.toString().toDoubleOrNull()
            // **HERE WE GET THE TEXT FROM THE PHONE NUMBER FIELD**
            val phoneNumber = phoneNumberEditText.text.toString()

            // This checks that the required fields are not empty
            if (name.isNotEmpty() && className.isNotEmpty() && feeAmount != null) {
                // This creates the student object to be saved
                val studentToSave = student?.copy(
                    name = name,
                    className = className,
                    feeAmount = feeAmount,
                    // **HERE WE ADD THE PHONE NUMBER TO THE OBJECT**
                    phoneNumber = phoneNumber
                ) ?: Student(
                    name = name,
                    className = className,
                    feeAmount = feeAmount,
                    // **AND HERE AS WELL**
                    phoneNumber = phoneNumber
                )

                // This sends the saved student back to the main list
                val resultIntent = Intent()
                resultIntent.putExtra("STUDENT_RESULT", studentToSave)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}