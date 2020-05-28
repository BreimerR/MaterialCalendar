package com.gmail.brymher.materialcalendar;

import org.threeten.bp.DayOfWeek;

public class State {
    public final CalendarMode calendarMode;
    public final DayOfWeek firstDayOfWeek;
    public final CalendarDay minDate;
    public final CalendarDay maxDate;
    public final boolean cacheCurrentPosition;
    public boolean showWeekDays = true;
    public int showOtherDates = MaterialCalendarView.SHOW_DEFAULTS;

    State(final StateBuilder builder) {
        calendarMode = builder.calendarMode;
        firstDayOfWeek = builder.firstDayOfWeek;
        minDate = builder.minDate;
        maxDate = builder.maxDate;
        cacheCurrentPosition = builder.cacheCurrentPosition;
        showWeekDays = builder.showWeekDays;
    }

    /**
     * Modify parameters from current state.
     */
    public StateBuilder edit() {
        return new StateBuilder(this);
    }
}
