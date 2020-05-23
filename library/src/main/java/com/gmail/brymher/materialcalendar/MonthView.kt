package com.gmail.brymher.materialcalendar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate


/**
 * Display a month of [DayView]s and
 * seven [WeekDayView]s.
 */

class MonthView : CalendarPagerView {

    override val rows: Int get() = if (showWeekDays) DEFAULT_MAX_WEEKS + DAY_NAMES_ROW else DEFAULT_MAX_WEEKS

    constructor(
        view: MaterialCalendarView,
        month: CalendarDay?,
        firstDayOfWeek: DayOfWeek,
        showWeekDays: Boolean
    ) : super(
        view,
        month,
        firstDayOfWeek,
        showWeekDays
    )

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleRes
    )


    override fun buildDayViews(
        dayViews: MutableCollection<DayView>?,
        calendar: LocalDate?
    ) {
        var temp = calendar
        for (r in 0 until DEFAULT_MAX_WEEKS) {
            for (i in 0 until DEFAULT_DAYS_IN_WEEK) {
                addDayView(dayViews?.toMutableList(), temp)
                temp = temp?.plusDays(1)
            }
        }
    }

    val month: CalendarDay?
        get() = firstViewDay

    override fun isDayEnabled(day: CalendarDay?): Boolean {
        return day?.month == firstViewDay?.month
    }

}
