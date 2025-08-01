package com.aaloke.feetracker

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }
    private var feesByDate = mapOf<LocalDate, List<Fee>>()
    private var studentMap = mapOf<Int, Student>()
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        val calendarView = findViewById<CalendarView>(R.id.calendarView)

        lifecycleScope.launch {
            studentMap = db.appDao().getAllStudents().first().associateBy { it.id }
            feesByDate = db.appDao().getAllFees().first().groupBy {
                java.util.Date(it.paymentDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            }
            setupCalendar(calendarView)
        }
    }

    private fun setupCalendar(calendarView: CalendarView) {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = firstDayOfWeekFromLocale()

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarDayText)
            val dotsContainer: LinearLayout = view.findViewById(R.id.dotsContainer)
            lateinit var day: CalendarDay
            init { view.setOnClickListener { if (day.position == DayPosition.MonthDate) { updateSelectedDate(day.date) } } }
        }

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()
                container.dotsContainer.removeAllViews()

                if (data.position == DayPosition.MonthDate) {
                    container.textView.visibility = View.VISIBLE
                    feesByDate[data.date]?.let { fees ->
                        val distinctStudentColors = fees.mapNotNull { studentMap[it.studentId]?.color }.distinct()
                        for (color in distinctStudentColors) {
                            val dot = View(container.view.context)
                            val dotSize = 8
                            val params = LinearLayout.LayoutParams(dotSize, dotSize)
                            params.setMargins(2, 0, 2, 0)
                            dot.layoutParams = params
                            val shape = GradientDrawable()
                            shape.shape = GradientDrawable.OVAL
                            shape.setColor(color)
                            dot.background = shape
                            container.dotsContainer.addView(dot)
                        }
                    }
                } else {
                    container.textView.visibility = View.INVISIBLE
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val textView: TextView = view.findViewById(R.id.calendarHeader)
        }
        calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                container.textView.text = "${data.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${data.yearMonth.year}"
            }
        }
    }

    private fun updateSelectedDate(date: LocalDate) {
        // ... (rest of the function is unchanged)
        val paymentsTextView = findViewById<TextView>(R.id.paymentsTextView)
        val selectedDateTextView = findViewById<TextView>(R.id.selectedDateTextView)
        val payments = feesByDate[date]
        selectedDateTextView.text = "Payments on ${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}, ${date.year}"
        if (payments.isNullOrEmpty()) {
            paymentsTextView.text = "No payments recorded on this day."
        } else {
            paymentsTextView.text = payments.joinToString("\n") { fee ->
                "- ${studentMap[fee.studentId]?.name}: \$${fee.amount}"
            }
        }
    }
}