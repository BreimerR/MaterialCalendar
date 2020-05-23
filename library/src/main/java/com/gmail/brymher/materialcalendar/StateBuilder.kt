package com.gmail.brymher.materialcalendar

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*


class StateBuilder {

    var calendarMode: CalendarMode
    var firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
        private set
    var cacheCurrentPosition = false
    var minDate: CalendarDay? = null
    var maxDate: CalendarDay? = null
    var showWeekDays = false
        private set

    constructor() {
        calendarMode = CalendarMode.MONTHS
        firstDayOfWeek = LocalDate.now().with(
            WeekFields.of(Locale.getDefault()).dayOfWeek(),
            1
        ).dayOfWeek
    }

    constructor(state: State) {
        calendarMode = state.calendarMode
        firstDayOfWeek = state.firstDayOfWeek
        minDate = state.minDate
        maxDate = state.maxDate
        cacheCurrentPosition = state.cacheCurrentPosition
        showWeekDays = state.showWeekDays
    }

    /**
     * Sets the first day of the week.
     *
     *
     * Uses the [DayOfWeek] day constants.
     *
     * @param day The first day of the week as a [DayOfWeek] enum.
     * @see Calendar
     */
    fun setFirstDayOfWeek(day: DayOfWeek): StateBuilder {
        firstDayOfWeek = day
        return this
    }

    /**
     * Set calendar display mode. The default mode is Months.
     * When switching between modes will select todays date, or the selected date,
     * if selection mode is single.
     *
     * @param mode - calendar mode
     */
    fun setCalendarDisplayMode(mode: CalendarMode): StateBuilder {
        calendarMode = mode
        return this
    }

    /**
     * @param date set the minimum selectable date, null for no minimum
     */
    fun setMinimumDate(date: LocalDate?): StateBuilder {
        setMinimumDate(CalendarDay.from(date))
        return this
    }

    /**
     * @param calendar set the minimum selectable date, null for no minimum
     */
    fun setMinimumDate(calendar: CalendarDay?): StateBuilder {
        minDate = calendar
        return this
    }

    /**
     * @param date set the maximum selectable date, null for no maximum
     */
    fun setMaximumDate(date: LocalDate?): StateBuilder {
        setMaximumDate(CalendarDay.from(date))
        return this
    }

    /**
     * @param calendar set the maximum selectable date, null for no maximum
     */
    fun setMaximumDate(calendar: CalendarDay?): StateBuilder {
        maxDate = calendar
        return this
    }

    /**
     * @param showWeekDays true to show week days names
     */
    fun setShowWeekDays(showWeekDays: Boolean): StateBuilder {
        this.showWeekDays = showWeekDays
        return this
    }

    /**
     * Use this method to enable saving the current position when switching
     * between week and month mode. By default, the calendar update to the latest selected date
     * or the current date. When set to true, the view will used the month that the calendar is
     * currently on.
     *
     * @param cacheCurrentPosition Set to true to cache the current position, false otherwise.
     */
    fun isCacheCalendarPositionEnabled(cacheCurrentPosition: Boolean): StateBuilder {
        this.cacheCurrentPosition = cacheCurrentPosition
        return this
    }

    fun commit(calendarView: MaterialCalendarView) {
        calendarView.commit(State(this))
    }
}