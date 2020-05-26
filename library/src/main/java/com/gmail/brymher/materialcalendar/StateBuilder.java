package com.gmail.brymher.materialcalendar;

import androidx.annotation.Nullable;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.WeekFields;

import java.util.*;

public class StateBuilder {
    public CalendarMode calendarMode;
    public DayOfWeek firstDayOfWeek;
    public boolean cacheCurrentPosition = false;
    public CalendarDay minDate = null;
    public CalendarDay maxDate = null;
    public boolean showWeekDays;

    public StateBuilder() {
        calendarMode = CalendarMode.MONTHS;
        firstDayOfWeek =
                LocalDate.now().with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1).getDayOfWeek();
    }

    StateBuilder(final State state) {
        calendarMode = state.calendarMode;
        firstDayOfWeek = state.firstDayOfWeek;
        minDate = state.minDate;
        maxDate = state.maxDate;
        cacheCurrentPosition = state.cacheCurrentPosition;
        showWeekDays = state.showWeekDays;
    }

    /**
     * Sets the first day of the week.
     * <p>
     * Uses the {@link DayOfWeek} day constants.
     *
     * @param day The first day of the week as a {@link DayOfWeek} enum.
     * @see java.util.Calendar
     */
    public StateBuilder setFirstDayOfWeek(DayOfWeek day) {
        this.firstDayOfWeek = day;
        return this;
    }

    /**
     * Set calendar display mode. The default mode is Months.
     * When switching between modes will select todays date, or the selected date,
     * if selection mode is single.
     *
     * @param mode - calendar mode
     */
    public StateBuilder setCalendarDisplayMode(CalendarMode mode) {
        this.calendarMode = mode;
        return this;
    }

    /**
     * @param date set the minimum selectable date, null for no minimum
     */
    public StateBuilder setMinimumDate(@Nullable LocalDate date) {
        setMinimumDate(CalendarDay.from(date));
        return this;
    }

    /**
     * @param calendar set the minimum selectable date, null for no minimum
     */
    public StateBuilder setMinimumDate(@Nullable CalendarDay calendar) {
        minDate = calendar;
        return this;
    }

    /**
     * @param date set the maximum selectable date, null for no maximum
     */
    public StateBuilder setMaximumDate(@Nullable LocalDate date) {
        setMaximumDate(CalendarDay.from(date));
        return this;
    }

    /**
     * @param calendar set the maximum selectable date, null for no maximum
     */
    public StateBuilder setMaximumDate(@Nullable CalendarDay calendar) {
        maxDate = calendar;
        return this;
    }

    /**
     * @param showWeekDays true to show week days names
     */
    public StateBuilder setShowWeekDays(boolean showWeekDays) {
        this.showWeekDays = showWeekDays;
        return this;
    }

    /**
     * Use this method to enable saving the current position when switching
     * between week and month mode. By default, the calendar update to the latest selected date
     * or the current date. When set to true, the view will used the month that the calendar is
     * currently on.
     *
     * @param cacheCurrentPosition Set to true to cache the current position, false otherwise.
     */
    public StateBuilder isCacheCalendarPositionEnabled(final boolean cacheCurrentPosition) {
        this.cacheCurrentPosition = cacheCurrentPosition;
        return this;
    }

    public void commit(MaterialCalendarView calendar) {
        calendar.commit(new State(this));
    }
}