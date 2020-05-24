package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.gmail.brymher.materialcalendar.CalendarMode
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import java.util.*

class AppBarMaterialCalendarBehavior(context: Context, attributeSet: AttributeSet) :
    Behaviors.AppBarBehavior<MaterialCalendarView>(
        context, attributeSet
    ) {


    var appBarHeight: Int = 0

    var calendarMode: CalendarMode? = CalendarMode.MONTHS
        protected set
    var weekOfMonth = Calendar.getInstance()[Calendar.WEEK_OF_MONTH]
    private var calendarLineHeight = 0
    private var weekCalendarHeight = 0
    private var monthCalendarHeight = 0
    private var listMaxOffset = 0
    private var velocityY = 0f
    private var canAutoScroll = true

    fun setMonthMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.MONTHS) {
            return
        }

        calendarView.state?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit(calendarView)
        }

        setTopAndBottomOffset(-calendarLineHeight * (weekOfMonth - 1))
        calendarMode = CalendarMode.MONTHS
    }

    fun setWeekMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.WEEKS) {
            return
        }

        calendarView.state?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit(calendarView)
        }

        setTopAndBottomOffset(calendarLineHeight)

        calendarMode = CalendarMode.WEEKS
    }

}