package com.gmail.brymher.materialcalendar

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.gmail.brymher.materialcalendar.format.DayFormatter
import com.gmail.brymher.materialcalendar.format.TitleFormatter
import com.gmail.brymher.materialcalendar.format.WeekDayFormatter
import org.threeten.bp.LocalDate
import java.util.*


/**
 * Pager adapter backing the calendar view
 */
abstract class CalendarPagerAdapter<V : CalendarPagerView?> constructor(
    protected val mcv: MaterialCalendarView
) : PagerAdapter() {

    private val currentViews: ArrayDeque<V> = ArrayDeque()
    val today: CalendarDay? = CalendarDay.today
    var titleFormatter: TitleFormatter? = TitleFormatter.DEFAULT
        set(value) {
            field = value ?: TitleFormatter.DEFAULT
        }

    var color: Int = R.color.mcv_text_date_light
    private var dateTextAppearance: Int? = null
    private var weekDayTextAppearance: Int? = null

    fun getTextAppearance(): Int? = dateTextAppearance

    @MaterialCalendarView.ShowOtherDates
    private var showOtherDates = MaterialCalendarView.SHOW_DEFAULTS
    private var minDate: CalendarDay? = null
    private var maxDate: CalendarDay? = null

    var rangeIndex: DateRangeIndex? = null
        private set

    private var selectedDates: MutableList<CalendarDay?> = ArrayList()

    var weekDayFormatter: WeekDayFormatter = WeekDayFormatter.DEFAULT
        set(value) {
            field = value
            for (pagerView in currentViews) {
                pagerView?.weekDayFormatter = value
            }
        }
    private var dayFormatter = DayFormatter.DEFAULT
    private var dayFormatterContentDescription = dayFormatter
    var decorators: List<DayViewDecorator> = ArrayList()
        set(value) {
            field = value
            invalidateDecorators()
        }
    private var decoratorResults: MutableList<DecoratorResult>? = null
    private var selectionEnabled = true
    var showWeekDays = false

    fun invalidateDecorators() {
        decoratorResults = ArrayList()
        for (decorator in decorators) {
            val facade = DayViewFacade()
            decorator.decorate(facade)
            if (facade.isDecorated) {
                decoratorResults?.add(
                    DecoratorResult(
                        decorator,
                        facade
                    )
                )
            }
        }
        for (pagerView in currentViews) {
            pagerView?.setDayViewDecorators(decoratorResults)
        }
    }

    override fun getCount(): Int {
        return rangeIndex!!.count
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titleFormatter?.format(getItem(position))
    }

    fun migrateStateAndReturn(newAdapter: CalendarPagerAdapter<*>): CalendarPagerAdapter<*> {
        newAdapter.titleFormatter = titleFormatter
        newAdapter.color = color
        newAdapter.dateTextAppearance = dateTextAppearance
        newAdapter.weekDayTextAppearance = weekDayTextAppearance
        newAdapter.showOtherDates = showOtherDates
        newAdapter.minDate = minDate
        newAdapter.maxDate = maxDate
        newAdapter.selectedDates = selectedDates
        newAdapter.weekDayFormatter = weekDayFormatter
        newAdapter.dayFormatter = dayFormatter
        newAdapter.dayFormatterContentDescription = dayFormatterContentDescription
        newAdapter.decorators = decorators
        newAdapter.decoratorResults = decoratorResults
        newAdapter.selectionEnabled = selectionEnabled
        return newAdapter
    }

    fun getIndexForDay(day: CalendarDay?): Int {
        if (day == null) {
            return count / 2
        }

        minDate?.let {
            if (day.isBefore(it)) {
                return 0
            }
        }
        return if (maxDate != null && day.isAfter(maxDate!!)) {
            count - 1
        } else rangeIndex!!.indexOf(day)
    }

    protected abstract fun createView(position: Int): V
    protected abstract fun indexOf(view: V): Int
    protected abstract fun isInstanceOfView(`object`: Any?): Boolean
    protected abstract fun createRangeIndex(min: CalendarDay?, max: CalendarDay?): DateRangeIndex?

    override fun getItemPosition(`object`: Any): Int {
        return if (!isInstanceOfView(`object`)) {
            POSITION_NONE
        } else {
            val pagerView = `object` as CalendarPagerView

            val firstViewDay = pagerView.firstViewDay ?: return POSITION_NONE

            @Suppress("UNCHECKED_CAST")
            val index = indexOf(`object` as V)

            if (index < 0) POSITION_NONE
            else index
        }

    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val pagerView = createView(position)
        pagerView!!.contentDescription = mcv.calendarContentDescription
        pagerView.alpha = 0f
        pagerView.setSelectionEnabled(selectionEnabled)
        pagerView.weekDayFormatter = weekDayFormatter
        pagerView.dayFormatter = dayFormatter
        pagerView.dayFormatterContentDescription = dayFormatterContentDescription
        pagerView.selectionColor = color

        if (dateTextAppearance != null) {
            pagerView.setDateTextAppearance(dateTextAppearance!!)
        }
        if (weekDayTextAppearance != null) {
            pagerView.setWeekDayTextAppearance(weekDayTextAppearance!!)
        }
        pagerView.showOtherDates = showOtherDates
        pagerView.setMinimumDate(minDate)
        pagerView.setMaximumDate(maxDate)
        pagerView.setSelectedDates(selectedDates)
        container.addView(pagerView)
        currentViews.add(pagerView)
        pagerView.setDayViewDecorators(decoratorResults)
        return pagerView
    }

    fun setSelectionEnabled(enabled: Boolean) {
        selectionEnabled = enabled
        for (pagerView in currentViews) {
            pagerView!!.setSelectionEnabled(selectionEnabled)
        }
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        @Suppress("UNCHECKED_CAST")
        val pagerView = `object` as V
        currentViews.remove(pagerView)
        container.removeView(pagerView)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }


    var selectionColor: Int
        get() = color
        set(value) {
            color = value
            for (pagerView in currentViews) {
                pagerView?.selectionColor = value
            }
        }

    fun setDateTextAppearance(taId: Int) {
        if (taId == 0) {
            return
        }
        dateTextAppearance = taId
        for (pagerView in currentViews) {
            pagerView!!.setDateTextAppearance(taId)
        }
    }

    fun setShowOtherDates(@MaterialCalendarView.ShowOtherDates showFlags: Int) {
        showOtherDates = showFlags
        for (pagerView in currentViews) {
            pagerView!!.showOtherDates = showFlags
        }
    }


    fun setDayFormatter(formatter: DayFormatter) {
        dayFormatterContentDescription =
            if (dayFormatterContentDescription === dayFormatter) formatter else dayFormatterContentDescription
        dayFormatter = formatter
        for (pagerView in currentViews) {
            pagerView!!.dayFormatter = formatter
        }
    }

    fun setDayFormatterContentDescription(formatter: DayFormatter) {
        dayFormatterContentDescription = formatter
        for (pagerView in currentViews) {
            pagerView?.dayFormatterContentDescription = formatter
        }
    }

    @MaterialCalendarView.ShowOtherDates
    fun getShowOtherDates(): Int {
        return showOtherDates
    }

    fun setWeekDayTextAppearance(taId: Int) {
        if (taId == 0) {
            return
        }
        weekDayTextAppearance = taId
        for (pagerView in currentViews) {
            pagerView!!.setWeekDayTextAppearance(taId)
        }
    }

    fun setRangeDates(
        min: CalendarDay?,
        max: CalendarDay?
    ) {
        var min = min
        var max = max
        minDate = min
        maxDate = max
        for (pagerView in currentViews) {
            pagerView?.setMinimumDate(min)
            pagerView?.setMaximumDate(max)
        }
        if (min == null) {
            min = CalendarDay.from(
                (today?.year ?: 1970) - 200,
                today?.month,
                today?.day
            )
        }
        if (max == null) {
            max = CalendarDay.from(
                (today?.year ?: 1970) + 200,
                today?.month,
                today?.day
            )
        }
        rangeIndex = createRangeIndex(min, max)
        notifyDataSetChanged()
        invalidateSelectedDates()
    }

    fun clearSelections() {
        selectedDates.clear()
        invalidateSelectedDates()
    }

    /**
     * Select or un-select a day.
     *
     * @param day      Day to select or un-select
     * @param selected Whether to select or un-select the day from the list.
     * @see CalendarPagerAdapter.selectRange
     */
    fun setSelectedDate(
        day: CalendarDay?,
        selected: Boolean
    ) {
        if (selected) {
            if (!selectedDates.contains(day)) {
                selectedDates.add(day)
                invalidateSelectedDates()
            }
        } else {
            if (selectedDates.contains(day)) {
                selectedDates.remove(day)
                invalidateSelectedDates()
            }
        }
    }

    /**
     * Clear the previous selection, select the range of days from first to last, and finally
     * invalidate. First day should be before last day, otherwise the selection won't happen.
     *
     * @param first The first day of the range.
     * @param last  The last day in the range.
     * @see CalendarPagerAdapter.setSelectedDate
     */
    fun selectRange(
        first: CalendarDay,
        last: CalendarDay
    ) {
        selectedDates.clear()

        // Copy to start from the first day and increment
        var temp =
            LocalDate.of(first.year, first.month, first.day)

        // for comparison
        val end = last.date
        while (temp.isBefore(end) || temp == end) {
            selectedDates.add(CalendarDay.from(temp))
            temp = temp.plusDays(1)
        }
        invalidateSelectedDates()
    }

    private fun invalidateSelectedDates() {
        validateSelectedDates()
        for (pagerView in currentViews) {
            pagerView?.setSelectedDates(selectedDates)
        }
    }

    private fun validateSelectedDates() {
        var i = 0
        while (i < selectedDates.size) {
            val date = selectedDates[i]
            if (minDate != null && minDate!!.isAfter(date!!) || (maxDate != null
                        && maxDate!!.isBefore(date!!))
            ) {
                selectedDates.removeAt(i)
                mcv.onDateUnselected(date)
                i -= 1
            }
            i++
        }
    }

    fun getItem(position: Int): CalendarDay? {
        return rangeIndex?.getItem(position)
    }

    fun getSelectedDates(): MutableList<CalendarDay> {
        return Collections.unmodifiableList(selectedDates)
    }

    fun setSelectedDates(vararg days: CalendarDay) {
        selectedDates.clear()
        for (day in days) selectedDates.add(day)
    }

    fun getDateTextAppearance(): Int {
        return (if (dateTextAppearance == null) 0 else dateTextAppearance)!!
    }

    fun getWeekDayTextAppearance(): Int {
        return if (weekDayTextAppearance == null) 0 else weekDayTextAppearance!!
    }

    init {
        currentViews.iterator()
        setRangeDates(null, null)
    }
}
