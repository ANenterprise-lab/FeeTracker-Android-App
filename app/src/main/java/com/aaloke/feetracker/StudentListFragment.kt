package com.aaloke.feetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StudentListFragment : Fragment() {

    private lateinit var studentViewModel: StudentViewModel // This line had the error
    private lateinit var studentAdapter: StudentAdapter

    private var listType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            listType = it.getString("LIST_TYPE")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.studentRecyclerView)

        studentAdapter = StudentAdapter(
            onPayFeeClicked = { student ->
                studentViewModel.updateStudent(student.copy(feeStatus = "Paid"))
            },
            onDeleteClicked = { student ->
                studentViewModel.deleteStudent(student)
            }
        )
        recyclerView.adapter = studentAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // This line also had the error
        studentViewModel = ViewModelProvider(this).get(StudentViewModel::class.java)

        // Observe the correct list based on the argument
        when (listType) {
            "Pending" -> {
                studentViewModel.pendingStudents.observe(viewLifecycleOwner) { students ->
                    studentAdapter.submitList(students)
                }
            }
            "Paid" -> {
                studentViewModel.paidStudents.observe(viewLifecycleOwner) { students ->
                    studentAdapter.submitList(students)
                }
            }
            else -> { // "Class" tab
                studentViewModel.allStudents.observe(viewLifecycleOwner) { students ->
                    studentAdapter.submitList(students)
                }
            }
        }
        return view
    }

    companion object {
        fun newInstance(listType: String?): StudentListFragment {
            val fragment = StudentListFragment()
            val args = Bundle()
            args.putString("LIST_TYPE", listType)
            fragment.arguments = args
            return fragment
        }
    }
}