package com.gmail.brymher.materialcalendar.format;

import com.gmail.brymher.materialcalendar.CalendarDay
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import java.text.SimpleDateFormat


/**
 * Supply labels for a given day. Default implementation is to format using a [SimpleDateFormat]
 */
abstract class DayFormatter {
    /**
     * Format a given day into a string
     *
     * @param day the day
     * @return a label for the day
     */
    abstract fun format(day: CalendarDay): String

    companion object {
        /**
         * Default format for displaying the day.
         */
        const val DEFAULT_FORMAT = "d"

        /**
         * Default implementation used by [MaterialCalendarView]
         */

        val DEFAULT: DayFormatter = DateFormatDayFormatter()
    }
}

