package com.gmail.brymher.materialcalendar

import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields


@ExperimentalStdlibApi
class WeekViewPagerAdapter(mcv: MaterialCalendarView, today: CalendarDay? = null) :
    ViewPagerAdapter<WeekView>(mcv, today) {

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

    override fun createRangeIndex(min: CalendarDay, max: CalendarDay): DateRangeIndex =
        Weekly(min, max, mcv.firstDayOfWeek)

    class Weekly(val min: CalendarDay, val max: CalendarDay, val firstDayOfWeek: DayOfWeek?) :
        DateRangeIndex {

        /**
         * Number of weeks to show
         * */
        override var count: Int = 0

        init {
            count = indexOf(max) + 1
        }


        override fun indexOf(day: CalendarDay?): Int {
            val weekFields = WeekFields.of(firstDayOfWeek, 1)
            val temp = day?.date?.with(weekFields.dayOfWeek(), 1L)
            return temp?.let {
                ChronoUnit.WEEKS.between(min.date, it).toInt()
            } ?: 0
        }

        override fun getItem(position: Int): CalendarDay? {
            return CalendarDay.from(min.date.plusWeeks(position.toLong()))
        }


        /**
         * Getting the first day of a week for a specific date based on a specific week day as first
         * day.
         */
        fun getFirstDayOfWeek(day: CalendarDay): CalendarDay? {
            val temp = day.date.with(WeekFields.of(firstDayOfWeek, 1).dayOfWeek(), 1L)
            return from(temp)
        }
    }


}