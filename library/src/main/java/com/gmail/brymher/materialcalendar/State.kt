package com.gmail.brymher.materialcalendar

import android.annotation.SuppressLint
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*


class State(builder: StateBuilder) {


    val calendarMode: CalendarMode = builder.calendarMode
    val firstDayOfWeek: DayOfWeek = builder.firstDayOfWeek
    val minDate: CalendarDay? = builder.minDate
    val maxDate: CalendarDay? = builder.maxDate
    val cacheCurrentPosition: Boolean by lazy {
        builder.cacheCurrentPosition
    }
    val showWeekDays: Boolean = builder.showWeekDays

    /**
     * Modify parameters from current state.
     */
    fun edit(): StateBuilder {
        return StateBuilder(this)
    }

    companion object {
        @JvmStatic
        val CALENDAR_MODE = CalendarMode.MONTHS

        @SuppressLint("ConstantLocale")
        @JvmStatic
        val FIRST_DAY_WEEK = LocalDate.now().with(
            WeekFields.of(Locale.getDefault()).dayOfWeek(),
            1
        ).dayOfWeek
    }

}