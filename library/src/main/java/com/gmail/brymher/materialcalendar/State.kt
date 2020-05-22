package com.gmail.brymher.materialcalendar

import org.threeten.bp.DayOfWeek


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

}