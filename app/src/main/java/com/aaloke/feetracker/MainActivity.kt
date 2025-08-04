package com.aaloke.feetracker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var classSpinner: Spinner
    private lateinit var sendSmsButton: Button

    // --- Activity Result Launchers ---
    private val addStudentResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getSerializableExtra("STUDENT_RESULT")?.let { student ->
                studentViewModel.insertStudent(student as Student)
            }
        }
    }

    private val requestSmsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            sendSmsToPendingStudents()
        } else {
            Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        // --- Find Views ---
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        tabLayout = findViewById(R.id.tabs)
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_student)
        classSpinner = findViewById(R.id.class_filter_spinner)
        sendSmsButton = findViewById(R.id.send_sms_button)

        // --- Setup UI Components ---
        setupTabs(viewPager, tabLayout)
        setupFab(fab)
        setupClassSpinner(classSpinner)
        setupSmsButton(sendSmsButton)

        // Lock or unlock features based on premium status
        updateUiForPremiumStatus()
    }

    private fun updateUiForPremiumStatus() {
        val isPremium = PremiumManager.isPremiumUser(this) || PremiumManager.isTemporarilyUnlocked

        // Enable/disable the spinner and SMS button
        classSpinner.isEnabled = isPremium
        sendSmsButton.isEnabled = isPremium

        // Visually grey them out if disabled
        classSpinner.alpha = if (isPremium) 1.0f else 0.5f
        sendSmsButton.alpha = if (isPremium) 1.0f else 0.5f
    }

    private fun showPremiumDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Premium Feature")
            .setMessage("This feature is locked. Unlock all features by watching an ad or buying premium.")
            .setPositiveButton("Buy Premium ($0.99)") { dialog, _ ->
                // --- THIS IS A SIMULATION ---
                // In a real app, you would start the Google Play Billing flow here.
                PremiumManager.setPremiumUser(this, true)
                updateUiForPremiumStatus()
                Toast.makeText(this, "Thank you for purchasing premium!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Watch Ad") { dialog, _ ->
                // --- THIS IS A SIMULATION ---
                // In a real app, you would show a Rewarded Ad here.
                PremiumManager.isTemporarilyUnlocked = true
                updateUiForPremiumStatus()
                Toast.makeText(this, "Features unlocked for this session!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTabs(viewPager: ViewPager, tabs: TabLayout) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ClassFragment(), "Class")
        adapter.addFragment(PendingFragment(), "Pending")
        adapter.addFragment(ReceivedFragment(), "Received")
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)

        // Add a listener to intercept clicks on locked tabs
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isPremium = PremiumManager.isPremiumUser(this@MainActivity) || PremiumManager.isTemporarilyUnlocked
                // If user clicks "Pending" (position 1) or "Received" (position 2) without being premium
                if (!isPremium && (tab?.position == 1 || tab?.position == 2)) {
                    // Go back to the "Class" tab
                    viewPager.setCurrentItem(0, true)
                    showPremiumDialog()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupFab(fab: FloatingActionButton) {
        fab.setOnClickListener {
            val intent = Intent(this, StudentDetailsActivity::class.java)
            addStudentResultLauncher.launch(intent)
        }
    }

    private fun setupClassSpinner(spinner: Spinner) {
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        studentViewModel.distinctClassNames.observe(this) { classNames ->
            val spinnerList = mutableListOf("All")
            spinnerList.addAll(classNames)
            spinnerAdapter.clear()
            spinnerAdapter.addAll(spinnerList)
            spinnerAdapter.notifyDataSetChanged()
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val isPremium = PremiumManager.isPremiumUser(this@MainActivity) || PremiumManager.isTemporarilyUnlocked
                if (!isPremium && position > 0) {
                    // If not premium, reset to "All" and show dialog
                    spinner.setSelection(0)
                    showPremiumDialog()
                } else {
                    studentViewModel.setClassNameFilter(parent.getItemAtPosition(position) as String)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSmsButton(button: Button) {
        button.setOnClickListener {
            val isPremium = PremiumManager.isPremiumUser(this) || PremiumManager.isTemporarilyUnlocked
            if (!isPremium) {
                showPremiumDialog()
                return@setOnClickListener
            }

            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED -> {
                    sendSmsToPendingStudents()
                }
                else -> {
                    requestSmsPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
                }
            }
        }
    }

    private fun sendSmsToPendingStudents() {
        // We only want to observe this once, to avoid sending multiple SMS messages
        studentViewModel.pendingStudents.observe(this) { pendingStudents ->
            // Once we get the list, immediately remove the observer
            studentViewModel.pendingStudents.removeObservers(this)

            if (pendingStudents.isNullOrEmpty()) {
                Toast.makeText(this, "No students with pending fees.", Toast.LENGTH_SHORT).show()
                return@observe
            }

            val smsManager: SmsManager = this.getSystemService(SmsManager::class.java)
            val currentMonth = Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG, resources.configuration.locales[0])
            var messagesSent = 0

            pendingStudents.forEach { student ->
                if (!student.phoneNumber.isNullOrBlank()) {
                    val message = "Dear ${student.name}, your fees for the month of $currentMonth are pending."
                    smsManager.sendTextMessage(student.phoneNumber, null, message, null, null)
                    messagesSent++
                }
            }
            Toast.makeText(this, "$messagesSent fee reminders sent.", Toast.LENGTH_LONG).show()
        }
    }
}