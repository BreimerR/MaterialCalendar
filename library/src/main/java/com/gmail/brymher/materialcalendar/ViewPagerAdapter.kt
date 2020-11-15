package com.gmail.brymher.materialcalendar

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.viewpager.widget.PagerAdapter
import com.gmail.brymher.materialcalendar.MaterialCalendarView.ShowOtherDates
import com.gmail.brymher.materialcalendar.format.DayFormatter
import com.gmail.brymher.materialcalendar.format.TitleFormatter
import com.gmail.brymher.materialcalendar.format.WeekDayFormatter
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.ArrayList

@ExperimentalStdlibApi
abstract class ViewPagerAdapter<V : CalendarPagerView>(
    protected val mcv: MaterialCalendarView,
    protected var today: CalendarDay? = CalendarDay.today()
) : PagerAdapter() {

    /** TODO
     * parent container already stores the
     * list of children and doing this is a bit repetitive
     * */
    protected val currentViews: ArrayDeque<V> = ArrayDeque()

    @DrawableRes
    var textColor: Int = DayView.DEFAULT_TEXT_COLOR

    var titleFormatter: TitleFormatter? = TitleFormatter.DEFAULT
        set(value) {
            if (value == null) field = TitleFormatter.DEFAULT

            field = value
        }

    // should be selection color
    private var color: Int? = null

    var selectionColor
        get() = color
        set(value) {
            color = value
            currentViews.forEach { it.setSelectionColor(value!!) }
        }

    var dateTextAppearance: Int = 0
        set(value) {
            if (value == 0) {
                return
            }

            field = value

            currentViews.forEach { it.setDateTextAppearance(value) }
        }

    var weekDayTextAppearance: Int = 0
        set(value) {
            if (value == 0) return

            field = value

            applyToCurrentViews { setWeekDayTextAppearance(value) }
        }

    @ShowOtherDates
    private var showOtherDates = MaterialCalendarView.SHOW_DEFAULTS
        set(value) {
            field = value

            applyToCurrentViews { setShowOtherDates(field) }

        }

    fun applyToCurrentViews(action: V.() -> Unit) {
        currentViews.forEach {
            it.apply(action)
        }
    }

    protected var minDate: CalendarDay? = null
    protected var maxDate: CalendarDay? = null
    protected var rangeIndex: DateRangeIndex? = null
    protected var selectedDates: MutableList<CalendarDay> = ArrayList()
        get() = Collections.unmodifiableList(field)

    private var weekDayFormatter = WeekDayFormatter.DEFAULT
    var dayFormatter: DayFormatter = DayFormatter.DEFAULT
        set(value) {
            dayFormatterContentDescription =
                if (dayFormatterContentDescription == dayFormatter) value else dayFormatterContentDescription
            field = value

            applyToCurrentViews { setDayFormatter(value) }
        }

    var dayFormatterContentDescription: DayFormatter = dayFormatter
        set(value) {
            field = value
            applyToCurrentViews { setDayFormatterContentDescription(value) }
        }
    protected var decorators = mutableListOf<DayViewDecorator>()
        set(value) {
            field = value
            invalidateDecorators()
        }

    protected var decoratorResults: MutableList<DecoratorResult>? = null
    var selectionEnabled = true
        set(value) {
            field = value

            currentViews.forEach {
                it.setSelectionEnabled(value)
            }
        }
    var showWeekDays = true

    init {
        currentViews.iterator();
    }


    fun invalidateDecorators() {
        decoratorResults = mutableListOf()
        for (decorator in decorators) {
            val facade = DayViewFacade()

            if (facade.isDecorated) {
                decoratorResults!!.add(
                    DecoratorResult(
                        decorator.apply { decorate(facade) },
                        facade
                    )
                )
            }
        }

        currentViews.forEach {
            it.setDayViewDecorators(decoratorResults)
        }

    }

    override fun getCount(): Int = rangeIndex?.count ?: 0

    override fun getPageTitle(position: Int): CharSequence? = titleFormatter!!.format(getItem(position))

    open fun migrateStateAndReturn(newAdapter: ViewPagerAdapter<*>): ViewPagerAdapter<*>? {
        newAdapter.titleFormatter = titleFormatter!!
        newAdapter.color = color
        newAdapter.dateTextAppearance = dateTextAppearance!!
        newAdapter.weekDayTextAppearance = weekDayTextAppearance!!
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
        if (day == null) return count / 2



        minDate?.let {
            if (day.isBefore(it)) return 0
        }


        maxDate?.let {
            if (day.isAfter(it)) return count - 1
        }

        return rangeIndex?.indexOf(day) ?: 0

    }

    /**
     * This is a single month/ week view
     * */
    protected abstract fun createView(position: Int): V

    protected abstract fun indexOf(view: V): Int?

    protected abstract fun isInstanceOfView(obj: Any): Boolean

    protected abstract fun createRangeIndex(min: CalendarDay, max: CalendarDay): DateRangeIndex

    override fun getItemPosition(obj: Any): Int? {

        if (!(isInstanceOfView(obj))) return POSITION_NONE

        (obj as CalendarPagerView).firstViewDay ?: return POSITION_NONE

        val index = indexOf(obj as V)

        if (index!= null && index < 0) return POSITION_NONE

        return index

    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any = createView(position).apply {
        contentDescription = mcv.calendarContentDescription

        alpha = 0f

        setSelectionEnabled(selectionEnabled)
        setWeekDayFormatter(weekDayFormatter)
        setDayFormatterContentDescription(dayFormatterContentDescription)
        color?.let {
            setSelectionColor(it)
        }

        dateTextAppearance?.let {
            setDateTextAppearance(it)
        }

        weekDayTextAppearance?.let { setWeekDayTextAppearance(it) }

        setShowOtherDates(showOtherDates)
        setMinimumDate(minDate)
        setMaximumDate(maxDate)
        setSelectedDates(selectedDates)

        container.addView(this)
        currentViews.add(this)

        setDayViewDecorators(decoratorResults)

    }


    open fun isShowWeekDays(): Boolean = showWeekDays

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val pagerView = `object` as V
        // TODO remove dependency on currentViews
        currentViews.remove(pagerView)
        container.removeView(pagerView)
    }


    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`


    /**
     * Updates date ranges and redraws the ui by calling invalidate
     * */
    fun setRangeDates(min: CalendarDay?, max: CalendarDay?) {
        minDate = min
        maxDate = max
        var m = min
        var mx = max

        applyToCurrentViews {
            setMinimumDate(min)
            setMaximumDate(max)
        }

        today?.let {

            if (m == null) {
                m = CalendarDay.from(it.year - 200, it.getMonth(), it.getDay())
            }


            if (mx == null) {
                mx = CalendarDay.from(it.year + 200, it.getMonth(), it.getDay())
            }
        }

        rangeIndex = createRangeIndex(m!!, mx!!)

        notifyDataSetChanged()

        invalidateDecorators()

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
     * @see ViewPagerAdapter#selectRange(CalendarDay, CalendarDay)
     */
    fun setSelectedDate(day: CalendarDay, selected: Boolean) {
        if (selected) {
            addSelectedDate(day)
        } else {
            removeSelectedDate(day)
        }
    }

    fun addSelectedDate(day: CalendarDay) {
        if (!selectedDates.contains(day)) {
            selectedDates.add(day)
            invalidateSelectedDates()
        }
    }

    fun removeSelectedDate(day: CalendarDay) {
        if (selectedDates.contains(day)) {
            selectedDates.add(day)
            invalidateSelectedDates()
        }
    }

    protected fun clearSelectedDates() {
        selectedDates.clear()
    }

    /**
     * Clear the previous selection, select the range of days from first to last, and finally
     * invalidate. First day should be before last day, otherwise the selection won't happen.
     *
     * @param first The first day of the range.
     * @param last  The last day in the range.
     * @see ViewPagerAdapter#setSelectedDate(CalendarDay, boolean)
     */
    fun selectRange(first: CalendarDay, last: CalendarDay) {
        clearSelectedDates()
        var temp = LocalDate.of(first.year, first.getMonth(), first.getDay())

        val end = last.date

        while (temp.isBefore(end) || temp.equals(end)) {
            CalendarDay.from(temp)?.let {
                selectedDates.add(it)
            }

            temp = temp.plusDays(1)
        }

        invalidateSelectedDates()
    }

    private fun invalidateSelectedDates() {
        validateSelectedDates()

        applyToCurrentViews { setSelectedDates(selectedDates) }

    }

    private fun validateSelectedDates() {

        var i = 0
        while (i < selectedDates.size) {
            val date = selectedDates[i]

            if ((minDate != null && minDate!!.isAfter(date)) || (maxDate != null && maxDate!!.isBefore(date))) {
                selectedDates.removeAt(i)
                mcv.onDateUnselected(date)
                i -= 1
            }
        }

    }

    protected fun getItem(position: Int): CalendarDay? = rangeIndex?.getItem(position)

}