package com.aaloke.feetracker

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    val allStudents = MutableLiveData<List<Student>>()
    val attendanceForDate = MutableLiveData<List<Attendance>>()

    init {
        val appDao = AppDatabase.getDatabase(application).appDao()
        repository = AppRepository(appDao)
    }

    fun fetchStudentsByClass(className: String) = viewModelScope.launch {
        // Fetch students for the chosen class
        val students = repository.getStudentsFiltered(className, "").first()
        allStudents.postValue(students)
    }

    fun fetchAttendanceForDate(studentIds: List<Int>, date: Date) = viewModelScope.launch {
        // Fetch existing attendance records for those students on that date
        val attendance = repository.getAttendanceForStudentsOnDate(studentIds, date).first()
        attendanceForDate.postValue(attendance)
    }

    fun saveAttendance(attendanceItems: List<AttendanceItem>, date: Date) = viewModelScope.launch {
        // Convert our UI models to database models
        val attendanceRecords = attendanceItems.map {
            Attendance(
                studentId = it.student.id,
                date = date,
                status = it.status
            )
        }
        repository.insertOrUpdateAttendance(attendanceRecords)
    }
}