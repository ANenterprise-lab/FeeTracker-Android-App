package com.aaloke.feetracker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var classSpinner: Spinner
    private lateinit var sendSmsButton: Button
    private lateinit var adManager: AdManager

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

    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val jsonData = BackupManager.createBackupJson(this@MainActivity)
                BackupManager.writeBackupToFile(this@MainActivity, it, jsonData)
                Toast.makeText(this@MainActivity, "Backup created successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                BackupManager.restoreBackupFromJson(this@MainActivity, it)
                Toast.makeText(this@MainActivity, "Restore completed successfully! Restarting app.", Toast.LENGTH_LONG).show()
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        MobileAds.initialize(this) {}
        adManager = AdManager(this)
        studentViewModel = ViewModelProvider(this)[StudentViewModel::class.java]

        val viewPager = findViewById<ViewPager2>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        val fab = findViewById<FloatingActionButton>(R.id.fab_add_student)
        classSpinner = findViewById(R.id.class_filter_spinner)
        sendSmsButton = findViewById(R.id.send_sms_button)
        val searchAutoComplete = findViewById<AutoCompleteTextView>(R.id.search_auto_complete)

        setupTabs(viewPager, tabLayout)
        setupFab(fab)
        setupClassSpinner()
        setupSmsButton()
        setupSearchView(searchAutoComplete)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                // ... (existing code)
                true
            }
            R.id.action_reports -> {
                // Launch the new ReportsActivity
                startActivity(Intent(this, ReportsActivity::class.java))
                true
            }
            R.id.action_backup -> {
                // ... (existing code)
                true
            }
            R.id.action_restore -> {
                // ... (existing code)
                true
            }
            // THIS IS THE NEW PART
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        updateUiForPremiumStatus()
    }

    private fun setupTabs(viewPager: ViewPager2, tabs: TabLayout) {
        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(DashboardFragment(), "Dashboard")
        adapter.addFragment(ClassFragment(), "Class")
        adapter.addFragment(PendingFragment(), "Pending")
        adapter.addFragment(ReceivedFragment(), "Received")
        viewPager.adapter = adapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val isPremium = PremiumManager.isPremiumUser(this@MainActivity) || PremiumManager.isTemporarilyUnlocked
                if (!isPremium && (tab?.position == 2 || tab?.position == 3)) {
                    viewPager.setCurrentItem(1, false)
                    showPremiumDialog()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchView(searchAutoComplete: AutoCompleteTextView) {
        val searchAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        searchAutoComplete.setAdapter(searchAdapter)
        studentViewModel.allStudentNames.observe(this) { names ->
            searchAdapter.clear()
            searchAdapter.addAll(names)
        }
        searchAutoComplete.doOnTextChanged { text, _, _, _ ->
            studentViewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupFab(fab: FloatingActionButton) {
        fab.setOnClickListener {
            val intent = Intent(this, StudentDetailsActivity::class.java)
            addStudentResultLauncher.launch(intent)
        }
    }

    private fun setupClassSpinner() {
        val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item)
        classSpinner.adapter = spinnerAdapter
        studentViewModel.allStudentNames.observe(this) { names ->
            val spinnerList = mutableListOf("All")
            spinnerList.addAll(names.distinct())
            spinnerAdapter.clear()
            spinnerAdapter.addAll(spinnerList)
        }
        classSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val isPremium = PremiumManager.isPremiumUser(this@MainActivity) || PremiumManager.isTemporarilyUnlocked
                if (!isPremium && position > 0) {
                    classSpinner.setSelection(0)
                    showPremiumDialog()
                } else {
                    studentViewModel.setClassNameFilter(parent.getItemAtPosition(position) as String)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupSmsButton() {
        sendSmsButton.setOnClickListener {
            val isPremium = PremiumManager.isPremiumUser(this) || PremiumManager.isTemporarilyUnlocked
            if (!isPremium) {
                showPremiumDialog()
                return@setOnClickListener
            }
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED -> sendSmsToPendingStudents()
                else -> requestSmsPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
            }
        }
    }

    private fun sendSmsToPendingStudents() {
        lifecycleScope.launch {
            val pendingStudents = studentViewModel.getPendingStudentsList()
            if (pendingStudents.isEmpty()) {
                Toast.makeText(this@MainActivity, "No students with pending fees.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val smsManager: SmsManager = getSystemService(SmsManager::class.java)
            val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
            val currentMonth = monthFormat.format(Calendar.getInstance().time)
            var messagesSent = 0

            // Get the custom template from SettingsManager
            val template = SettingsManager.getSmsTemplate(this@MainActivity)

            pendingStudents.forEach { student ->
                if (!student.phoneNumber.isNullOrBlank()) {
                    // Replace placeholders with real data
                    val message = template
                        .replace("{student_name}", student.name, ignoreCase = true)
                        .replace("{month}", currentMonth, ignoreCase = true)

                    smsManager.sendTextMessage(student.phoneNumber, null, message, null, null)
                    messagesSent++
                }
            }
            Toast.makeText(this@MainActivity, "$messagesSent fee reminders sent.", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateUiForPremiumStatus() {
        val isPremium = PremiumManager.isPremiumUser(this) || PremiumManager.isTemporarilyUnlocked
        classSpinner.isEnabled = isPremium
        sendSmsButton.isEnabled = isPremium
        classSpinner.alpha = if (isPremium) 1.0f else 0.5f
        sendSmsButton.alpha = if (isPremium) 1.0f else 0.5f
    }

    private fun showPremiumDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Premium Feature")
            .setMessage("This feature is locked. Unlock all features by watching an ad or buying premium.")
            .setPositiveButton("Buy Premium") { dialog, _ ->
                PremiumManager.setPremiumUser(this, true)
                updateUiForPremiumStatus()
                Toast.makeText(this, "Thank you for purchasing premium!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNeutralButton("Watch Ad") { dialog, _ ->
                adManager.showRewardedAd(this) {
                    PremiumManager.isTemporarilyUnlocked = true
                    updateUiForPremiumStatus()
                    Toast.makeText(this, "Features unlocked for this session!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}