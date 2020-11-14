package com.gmail.brymher.materialcalendar

enum class Mode(
    /**
     * Number of visible weeks per calendar mode.
     * */
    val visibleWeeksCount: Int
) {
    /**
     * Month Mode to display entire month per page
     * <p>
     *     This value should be dependant on the month being represented
     *     to avoid covering un required space
     * </p>
     * */
    MONTHS(6),

    /**
     * Number of weeks that will be shown in weeks mode.
     * */
    WEEKS(1)
    
    

}

