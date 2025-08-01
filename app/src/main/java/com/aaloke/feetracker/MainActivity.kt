package com.aaloke.feetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var studentAdapter: StudentAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var credential: GoogleAccountCredential

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInResult(result.data)
        } else {
            Toast.makeText(this, "Google Sign-In was cancelled.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupRecyclerView()
        setupGoogleSignIn()

        lifecycleScope.launch {
            db.appDao().getAllStudents().collect { students -> studentAdapter.updateData(students) }
        }

        findViewById<Button>(R.id.syncButton).setOnClickListener {
            syncDataToSheet()
        }
        // ... (other listeners)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope("https://www.googleapis.com/auth/spreadsheets"))
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun handleSignInResult(data: Intent?) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
            .addOnSuccessListener { googleAccount ->
                credential = GoogleAccountCredential.usingOAuth2(this, listOf("https://www.googleapis.com/auth/spreadsheets"))
                credential.selectedAccount = googleAccount.account
                Toast.makeText(this, "Sign-In successful. Syncing...", Toast.LENGTH_SHORT).show()
                syncDataToSheet()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun syncDataToSheet() {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedInAccount == null) {
            googleSignInLauncher.launch(mGoogleSignInClient.signInIntent)
            return
        }

        if (!::credential.isInitialized) {
            credential = GoogleAccountCredential.usingOAuth2(this, listOf("https://www.googleapis.com/auth/spreadsheets"))
            credential.selectedAccount = lastSignedInAccount.account
        }

        Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sheetsService = Sheets.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential).setApplicationName("FeeTracker").build()
                val spreadsheetId = findOrCreateSpreadsheet(sheetsService) ?: throw Exception("Could not create spreadsheet")

                val students = db.appDao().getAllStudents().first()
                val fees = db.appDao().getAllFees().first()

                // Prepare data with correct types
                val studentHeader = listOf<Any>("Student ID", "Name", "Default Fee")
                val studentRows = students.map { listOf<Any>(it.id, it.name, it.defaultFeeAmount) }
                val studentData = listOf(studentHeader) + studentRows

                val feeHeader = listOf<Any>("Fee ID", "Student ID", "Month", "Year", "Amount", "Paid", "Payment Date")
                val feeRows = fees.map {
                    val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(it.paymentDate))
                    listOf<Any>(it.id, it.studentId, it.month, it.year, it.amount, it.isPaid.toString(), formattedDate)
                }
                val feeData = listOf(feeHeader) + feeRows

                // Clear existing data and write new data
                val clearRequest = BatchUpdateSpreadsheetRequest().setRequests(listOf(
                    Request().setUpdateSheetProperties(UpdateSheetPropertiesRequest().setProperties(SheetProperties().setSheetId(0).setTitle("Students")).setFields("title")),
                    Request().setUpdateSheetProperties(UpdateSheetPropertiesRequest().setProperties(SheetProperties().setSheetId(1).setTitle("Fees")).setFields("title"))
                ))
                sheetsService.spreadsheets().batchUpdate(spreadsheetId, clearRequest).execute()

                val writeData = mutableListOf<ValueRange>()
                writeData.add(ValueRange().setRange("Students!A1").setValues(studentData.map { it.map { cell -> cell } }))
                writeData.add(ValueRange().setRange("Fees!A1").setValues(feeData.map { it.map { cell -> cell } }))

                val body = BatchUpdateValuesRequest().setValueInputOption("USER_ENTERED").setData(writeData)
                sheetsService.spreadsheets().values().batchUpdate(spreadsheetId, body).execute()

                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Sync successful!", Toast.LENGTH_LONG).show() }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Sync Error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun findOrCreateSpreadsheet(service: Sheets): String? {
        val spreadsheet = Spreadsheet().setProperties(SpreadsheetProperties().setTitle("FeeTracker Backup"))
            .setSheets(listOf(
                Sheet().setProperties(SheetProperties().setSheetId(0).setTitle("Students")),
                Sheet().setProperties(SheetProperties().setSheetId(1).setTitle("Fees"))
            ))
        return service.spreadsheets().create(spreadsheet).execute().spreadsheetId
    }

    private fun setupRecyclerView() {
        // ... (This function remains unchanged)
        studentAdapter = StudentAdapter(emptyList()) { student ->
            val intent = Intent(this, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student.id)
            }
            startActivity(intent)
        }
        val recyclerView = findViewById<RecyclerView>(R.id.studentsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = studentAdapter
    }
    private fun addSampleStudents() {
        // ... (This function remains unchanged)
    }
}