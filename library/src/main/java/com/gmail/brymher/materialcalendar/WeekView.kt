package com.gmail.brymher.materialcalendar

import android.annotation.SuppressLint
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate


/**
 * Display a week of {@linkplain DayView}s and
 * seven {@linkplain WeekDayView}s.
 */

/**
 * Display a week of [DayView]s and
 * seven [WeekDayView]s.
 */
/**
 * TODO
 * This should not be a viewPager
 * as weeks do not change days change
 */
@SuppressLint("ViewConstructor")
class WeekView(
    view: MaterialCalendarView,
    firstViewDay: CalendarDay?,
    firstDayOfWeek: DayOfWeek,
    showWeekDays: Boolean
) : CalendarPagerView(
    view,
    firstViewDay,
    firstDayOfWeek,
    showWeekDays
) {
    override fun buildDayViews(
        dayViews: MutableCollection<DayView>?,
        calendar: LocalDate?
    ) {
        var temp = calendar
        for (i in 0 until DEFAULT_DAYS_IN_WEEK) {
            addDayView(dayViews, temp)
            temp = temp?.plusDays(1)
        }
    }

    override fun isDayEnabled(day: CalendarDay?): Boolean {
        return true
    }

    override val rows: Int
        get() = if (showWeekDays) DAY_NAMES_ROW + 1 else 1

}
