package com.gmail.brymher.materialcalendar


/**
 * The callback used to indicate a date has been long clicked.
 */
interface OnDateLongClickListener {
    /**
     * Called when a user long clicks on a day.
     * There is no logic to prevent multiple calls for the same date and state.
     *
     * @param widget the view associated with this listener
     * @param date the date that was long clicked.
     */
    fun onDateLongClick(
        widget: MaterialCalendarView,
        date: CalendarDay
    )
}
