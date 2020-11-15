package com.gmail.brymher.materialcalendar

import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields


class WeekViewPagerAdapter(mcv: MaterialCalendarView, today: CalendarDay? = CalendarDay.today) :
    ViewPagerAdapter<WeekView>(mcv, today) {

    init {
        currentViews.iterator();
        setRangeDates(null, null)
    }

    /**
     * Creates the main view for a week
     * */
    override fun createView(position: Int): WeekView =
        WeekView(
            mcv,
            getItem(position),
            mcv.firstDayOfWeek,
            showWeekDays
        )

    override fun indexOf(view: WeekView): Int? = rangeIndex?.indexOf(view.firstViewDay)

    override fun isInstanceOfView(obj: Any): Boolean = obj is WeekView

    override fun createRangeIndex(min: CalendarDay?, max: CalendarDay?): DateRangeIndex =
        Weekly(min, max, mcv.firstDayOfWeek)

    class Weekly(minCalendarDay: CalendarDay?, max: CalendarDay?, val firstDayOfWeek: DayOfWeek?) :
        DateRangeIndex {

        private val min = getFirstDayOfWeek(minCalendarDay)

        /**
         * Number of weeks to show
         * */
        override var count: Int = 0

        init {
            count = indexOf(max) + 1
        }


        override fun indexOf(day: CalendarDay?): Int =
            day?.date?.with(WeekFields.of(firstDayOfWeek, 1).dayOfWeek(), 1L)?.let {
                min?.let { m ->
                    ChronoUnit.WEEKS.between(m.date, it).toInt()
                }

            } ?: POSITION_UNCHANGED


        override fun getItem(position: Int): CalendarDay? = min?.let {
            from(min.date.plusWeeks(position.toLong()))
        }


        /**
         * Getting the first day of a week for a specific date based on a specific week day as first
         * day.
         */
        fun getFirstDayOfWeek(day: CalendarDay?): CalendarDay? = day?.let {
            from(it.date.with(WeekFields.of(firstDayOfWeek, 1).dayOfWeek(), 1L))
        }
    }


}