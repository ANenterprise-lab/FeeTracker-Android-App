package com.aaloke.feetracker

interface StudentClickListener {
    fun onPayFeeClicked(student: Student)
    fun onDeleteClicked(student: Student)
    fun onItemClicked(student: Student)
    fun onWhatsAppClicked(student: Student)
    fun onSelectionModeStarted()
    fun onSelectionModeEnded()
    fun onItemSelected(count: Int)
}