package com.gmail.brymher.materialcalendar


import org.threeten.bp.Period



class MonthViewPagerAdapter(mcv: MaterialCalendarView, today: CalendarDay? = null) :
    ViewPagerAdapter<MonthView>(mcv, today) {

    override fun createView(position: Int): MonthView =
        MonthView(
            mcv,
            getItem(position),
            mcv.firstDayOfWeek,
            showWeekDays
        )

    override fun indexOf(view: MonthView): Int? =
        rangeIndex?.indexOf(view.month)

    override fun isInstanceOfView(obj: Any): Boolean =
        obj is MonthView

    override fun createRangeIndex(min: CalendarDay, max: CalendarDay): DateRangeIndex =
        Monthly(min, max)


    class Monthly(val min: CalendarDay, val max: CalendarDay) : DateRangeIndex {

        override val count: Int = indexOf(max) + 1

        override fun indexOf(day: CalendarDay?): Int =
            day?.let {
                Period
                    .between(min.date, it.date.withDayOfMonth(1))
                    .toTotalMonths().toInt()
            } ?: -1

        override fun getItem(position: Int): CalendarDay? =
            CalendarDay.from(min.date.plusMonths(position.toLong()))

    }
}