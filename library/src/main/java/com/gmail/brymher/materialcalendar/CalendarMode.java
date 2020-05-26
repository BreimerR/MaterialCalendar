package com.gmail.brymher.materialcalendar;

/**
 * Different Calendar types available for the pager, which will give the amount of weeks visible.
 */
public enum CalendarMode {

    /**
     * Month Mode to display an entire month per page.
     * <p>
     * this value should be dependant on the month being represented to avoid covering un required space
     */
    MONTHS(6),
    /**
     * Week mode that shows the calendar week by week.
     */
    WEEKS(1);

    /**
     * Number of visible weeks per calendar mode.
     */
    final int visibleWeeksCount;

    CalendarMode(int visibleWeeksCount) {
        this.visibleWeeksCount = visibleWeeksCount;
    }
}
