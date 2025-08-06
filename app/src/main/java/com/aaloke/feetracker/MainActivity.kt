package com.aaloke.feetracker

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var studentViewModel: StudentViewModel
    private lateinit var adManager: AdManager
    private lateinit var fabMain: FloatingActionButton
    private lateinit var fabAddStudent: ExtendedFloatingActionButton
    private lateinit var fabAttendance: ExtendedFloatingActionButton
    private lateinit var fabFilter: ExtendedFloatingActionButton
    private var isFabMenuOpen = false

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
        fabMain = findViewById(R.id.fab_main)
        fabAddStudent = findViewById(R.id.fab_add_student)
        fabAttendance = findViewById(R.id.fab_attendance)
        fabFilter = findViewById(R.id.fab_filter)

        setupTabs(viewPager, tabLayout)
        setupFabMenu()
        val manageBatchesButton: Button = findViewById(R.id.manage_batches_button)
        manageBatchesButton.setOnClickListener {
            val intent = Intent(this, BatchesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupFabMenu() {
        fabMain.setOnClickListener {
            if (isFabMenuOpen) closeFabMenu() else openFabMenu()
        }
        fabAddStudent.setOnClickListener {
            addStudentResultLauncher.launch(Intent(this, StudentDetailsActivity::class.java))
            closeFabMenu()
        }
        fabAttendance.setOnClickListener {
            startActivity(Intent(this, AttendanceActivity::class.java))
            closeFabMenu()
        }
        fabFilter.setOnClickListener {
            showFilterDialog()
            closeFabMenu()
        }
        val manageBatchesButton: Button = findViewById(R.id.manage_batches_button)
        manageBatchesButton.setOnClickListener {
            val intent = Intent(this, BatchesActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openFabMenu() {
        isFabMenuOpen = true
        fabMain.setImageResource(R.drawable.ic_close)
        fabAddStudent.show()
        fabAttendance.show()
        fabFilter.show()
        fabAddStudent.animate().translationY(-convertDpToPx(70f))
        fabAttendance.animate().translationY(-convertDpToPx(140f))
        fabFilter.animate().translationY(-convertDpToPx(210f))
    }

    private fun closeFabMenu() {
        isFabMenuOpen = false
        fabMain.setImageResource(R.drawable.ic_add)
        fabAddStudent.animate().translationY(0f).withEndAction { fabAddStudent.hide() }
        fabAttendance.animate().translationY(0f).withEndAction { fabAttendance.hide() }
        fabFilter.animate().translationY(0f).withEndAction { fabFilter.hide() }
    }

    private fun convertDpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null)
        val classAutocomplete = dialogView.findViewById<AutoCompleteTextView>(R.id.class_filter_autocomplete)
        val searchEditText = dialogView.findViewById<TextInputEditText>(R.id.search_filter_edittext)

        val classAdapter = ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line)
        classAutocomplete.setAdapter(classAdapter)

        studentViewModel.distinctClassNames.observe(this) { classNames ->
            val classList = mutableListOf("All")
            classList.addAll(classNames)
            classAdapter.clear()
            classAdapter.addAll(classList)
        }

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setNegativeButton("Reset") { _, _ ->
                studentViewModel.setClassNameFilter("All")
                studentViewModel.setSearchQuery("")
                Toast.makeText(this, "Filters Reset", Toast.LENGTH_SHORT).show()
            }
            .setPositiveButton("Apply") { _, _ ->
                val selectedClass = classAutocomplete.text.toString()
                val searchQuery = searchEditText.text.toString()
                studentViewModel.setClassNameFilter(if (selectedClass.isEmpty()) "All" else selectedClass)
                studentViewModel.setSearchQuery(searchQuery)
                Toast.makeText(this, "Filters Applied", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    // --- (All other functions remain the same) ---
    private val addStudentResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getSerializableExtra("STUDENT_RESULT")?.let { student -> studentViewModel.insertStudent(student as Student) }
        }
    }
    private val requestSmsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) sendSmsToPendingStudents() else Toast.makeText(this, "SMS permission denied.", Toast.LENGTH_SHORT).show()
    }
    private val createDocumentLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            lifecycleScope.launch {
                val jsonData = BackupManager.createBackupJson(this@MainActivity)
                BackupManager.writeBackupToFile(this@MainActivity, it, jsonData)
                Toast.makeText(this@MainActivity, "Backup created!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            lifecycleScope.launch {
                BackupManager.restoreBackupFromJson(this@MainActivity, it)
                Toast.makeText(this@MainActivity, "Restore complete! Restarting...", Toast.LENGTH_LONG).show()
                val intent = Intent(this@MainActivity, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reports -> { startActivity(Intent(this, ReportsActivity::class.java)); true }
            R.id.action_export -> {
                studentViewModel.allStudents.value?.let { students -> ExportManager.exportToCsv(this, students, "student_report.csv") }
                true
            }
            R.id.action_expenses -> { startActivity(Intent(this, ExpensesActivity::class.java)); true }
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
            R.id.action_backup -> {
                val fileName = "FeeTrackerBackup_${System.currentTimeMillis()}.json"
                createDocumentLauncher.launch(fileName)
                true
            }
            R.id.action_restore -> {
                openDocumentLauncher.launch(arrayOf("application/json"))
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
        adapter.addFragment(ClassFragment(), "Students")
        adapter.addFragment(PendingFragment(), "Pending")
        adapter.addFragment(ReceivedFragment(), "Received")
        viewPager.adapter = adapter
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        // ... (premium tab logic)
    }
    private fun updateUiForPremiumStatus() {
        // ... (premium UI logic)
    }
    private fun showPremiumDialog() {
        // ... (premium dialog logic)
    }
    private fun sendSmsToPendingStudents() {
        // ... (sms logic)
    }
}