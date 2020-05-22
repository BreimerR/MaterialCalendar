package com.gmail.brymher.materialcalendar;

import org.threeten.bp.Period


/**
 * Pager adapter backing the calendar view
 */
class MonthPagerAdapter(mcv: MaterialCalendarView?) : CalendarPagerAdapter<MonthView?>(
    mcv!!
) {


    override fun createView(position: Int): MonthView {

        return MonthView(
            mcv,
            getItem(position)!!,
            mcv.firstDayOfWeek,
            //was showWeekDays
            isShowWeekDays
        )
    }

    protected override fun indexOf(view: MonthView?): Int {
        val month = view?.month
        return rangeIndex!!.indexOf(month)
    }

    override fun isInstanceOfView(`object`: Any?): Boolean {
        return `object` is MonthView
    }

    override fun createRangeIndex(
        min: CalendarDay?,
        max: CalendarDay?
    ): DateRangeIndex? {
        return Monthly(min!!, max!!)
    }

    class Monthly(
        min: CalendarDay,
        max: CalendarDay
    ) :
        DateRangeIndex {
        /**
         * Minimum date with the first month to display.
         */
        private val min: CalendarDay

        /**
         * Number of months to display.
         */
        override val count: Int

        override fun indexOf(day: CalendarDay?): Int {
            return Period
                .between(min.date.withDayOfMonth(1), day!!.date.withDayOfMonth(1))
                .toTotalMonths().toInt()
        }

        override fun getItem(position: Int): CalendarDay? {
            return CalendarDay.from(min.date.plusMonths(position.toLong()))
        }

        init {
            this.min = CalendarDay.from(min.year, min.month, 1)
            count = indexOf(max) + 1
        }
    }
}

