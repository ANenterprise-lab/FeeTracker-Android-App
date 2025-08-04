package com.aaloke.feetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ReportsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports_container) // We'll create this next

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ReportsFragment())
                .commit()
        }
    }
}