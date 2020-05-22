package com.gmail.brymher.materialcalendar

import org.threeten.bp.DayOfWeek
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.WeekFields


class WeekPagerAdapter(mcv: MaterialCalendarView) : CalendarPagerAdapter<WeekView?>(mcv) {


    override fun createView(position: Int): WeekView {
        return WeekView(
            mcv,
            getItem(position),
            mcv.firstDayOfWeek,
            isShowWeekDays
        )
    }

    protected override fun indexOf(view: WeekView?): Int {
        val week = view?.firstViewDay
        return rangeIndex?.indexOf(week) ?: POSITION_NONE
    }

    override fun isInstanceOfView(`object`: Any?): Boolean {
        return `object` is WeekView
    }

    override fun createRangeIndex(
        min: CalendarDay?,
        max: CalendarDay?
    ): DateRangeIndex {
        return Weekly(min, max, mcv.firstDayOfWeek)
    }

    class Weekly(
        min: CalendarDay?,
        max: CalendarDay?,
        /**
         * First day of the week to base the weeks on.
         */
        private val firstDayOfWeek: DayOfWeek
    ) : DateRangeIndex {
        /**
         * Minimum day of the first week to display.
         */
        private val min: CalendarDay?

        /**
         * Number of weeks to show.
         */
        override var count: Int = 0

        override fun indexOf(day: CalendarDay?): Int {
            val weekFields =
                WeekFields.of(firstDayOfWeek, 1)
            val temp = day?.date?.with(weekFields.dayOfWeek(), 1L)
            return ChronoUnit.WEEKS.between(min!!.date, temp).toInt()
        }

        override fun getItem(position: Int): CalendarDay {
            return CalendarDay.from(min!!.date.plusWeeks(position.toLong()))!!
        }

        /**
         * Getting the first day of a week for a specific date based on a specific week day as first
         * day.
         */
        private fun getFirstDayOfWeek(day: CalendarDay?): CalendarDay? {
            val temp = day?.date?.with(
                WeekFields.of(firstDayOfWeek, 1).dayOfWeek(),
                1L
            )
            return CalendarDay.from(temp)
        }

        init {
            this.min = getFirstDayOfWeek(min)
            count = indexOf(max) + 1
        }
    }

}