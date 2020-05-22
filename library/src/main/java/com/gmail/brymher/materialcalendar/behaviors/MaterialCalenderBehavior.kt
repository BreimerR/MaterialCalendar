package com.gmail.brymher.materialcalendar.behaviors

import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.CalendarMode
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import java.util.*

class MaterialCalenderBehavior : ViewOffsetBehavior<MaterialCalendarView>() {

    val TAG = "MaterialCalender"

    private var calendarMode = CalendarMode.MONTHS

    private val weekOfMonth get() = Calendar.getInstance()[Calendar.WEEK_OF_MONTH]

    var calendarLineHeight: Int = 0
    var weekCalendarHeight = 0
    var monthCalendarHeight = 0
    var listMaxOffset = 0


    private fun setMonthMode(calendarView: MaterialCalendarView) {
        if (calendarMode != CalendarMode.WEEKS) {
            return
        }
        calendarView.setMode(CalendarMode.MONTHS)
        setTopAndBottomOffset(-calendarLineHeight * (weekOfMonth - 1))
        calendarMode = CalendarMode.MONTHS
    }


    private fun setWeekMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.WEEKS) {
            return
        }
        calendarView.setMode(CalendarMode.WEEKS)

        setTopAndBottomOffset(0)
        calendarMode = CalendarMode.WEEKS

    }
}