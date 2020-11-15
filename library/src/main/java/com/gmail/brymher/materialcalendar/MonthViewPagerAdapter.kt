package com.gmail.brymher.materialcalendar


import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import org.threeten.bp.Period


class MonthViewPagerAdapter(mcv: MaterialCalendarView, today: CalendarDay? = CalendarDay.today) :
    ViewPagerAdapter<MonthView>(mcv, today) {

    init {
        currentViews.iterator();
        setRangeDates(null, null)
    }

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

    override fun createRangeIndex(min: CalendarDay?, max: CalendarDay?): DateRangeIndex =
        Monthly(min, max)


    class Monthly(minCalendarDay: CalendarDay?, private val max: CalendarDay?) : DateRangeIndex {

        override val count: Int = indexOf(max) + 1

        private var min: CalendarDay? = minCalendarDay?.let {
            from(it.year, it.getMonth(), 1)
        }

        override fun indexOf(day: CalendarDay?): Int = day?.let {
            min?.let { m ->
                Period
                    .between(m.date, it.date.withDayOfMonth(1))
                    .toTotalMonths().toInt()
            }

        } ?: 0

        override fun getItem(position: Int): CalendarDay? = min?.let {
            from(it.date.plusMonths(position.toLong()))
        }

    }
}