package com.gmail.brymher.materialcalendar

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.gmail.brymher.materialcalendar.format.DayFormatter
import com.gmail.brymher.materialcalendar.format.WeekDayFormatter
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.lang.NullPointerException
import java.util.*


abstract class CalendarPagerView : ViewGroup, View.OnClickListener, OnLongClickListener {


    lateinit var mcv: MaterialCalendarView
    var firstViewDay: CalendarDay? = null
    var firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
    var showWeekDays: Boolean = true

    constructor(
        mcv: MaterialCalendarView,
        firstViewDay: CalendarDay?,
        firstDayOfWeek: DayOfWeek,
        showWeekDays: Boolean
    ) : super(mcv.context) {
        this.mcv = mcv
        this.firstDayOfWeek = firstDayOfWeek
        this.firstViewDay = firstViewDay
        this.showWeekDays = showWeekDays
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleRes
    )


    private val weekDayViews = ArrayList<WeekDayView>()
    private val decoratorResults = ArrayList<DecoratorResult>()

    @MaterialCalendarView.ShowOtherDates
    var showOtherDates = MaterialCalendarView.SHOW_DEFAULTS
        set(value) {
            field = value
            updateUi()
        }

    private var minDate: CalendarDay? = null
    private var maxDate: CalendarDay? = null
    private val dayViews: MutableCollection<DayView> = mutableListOf()

    /**
     * TODO
     * make weeks not a view pager as it does not change
     */
    private fun buildWeekDays(calendar: LocalDate?) {
        calendar?.let {
            var local = it
            for (i in 0 until DEFAULT_DAYS_IN_WEEK) {
                val weekDayView =
                    WeekDayView(context, local.dayOfWeek)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    weekDayView.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                }
                weekDayViews.add(weekDayView)
                addView(weekDayView)
                local = local.plusDays(1)
            }
        }

    }

    protected fun addDayView(
        dayViews: MutableCollection<DayView>?,
        temp: LocalDate?
    ) {
        val day =
            CalendarDay.from(temp)
        val dayView =
            DayView(context, day)
        dayView.setOnClickListener(this)
        dayView.setOnLongClickListener(this)
        dayViews?.add(dayView)
        addView(dayView, LayoutParams())
    }

    protected fun resetAndGetWorkingCalendar(): LocalDate? {

        return if (!isInEditMode) {
            val now = LocalDate.now()

            val fDayOfWeek = WeekFields.of(firstDayOfWeek, 1).dayOfWeek()

            val temp = firstViewDay?.date?.with(fDayOfWeek, 1)
            val dow = temp?.dayOfWeek?.value
            var delta: Int? = firstDayOfWeek.let { d ->
                dow?.let {
                    d.value - it
                }
            }
            delta?.let {
                //If the delta is positive, we want to remove a week
                val removeRow = if (MaterialCalendarView.showOtherMonths(showOtherDates)
                ) delta >= 0 else delta > 0

                if (removeRow) {
                    delta -= DEFAULT_DAYS_IN_WEEK
                }

                temp?.plusDays(delta.toLong())
            }
        } else null
    }

    protected abstract fun buildDayViews(
        dayViews: MutableCollection<DayView>?,
        calendar: LocalDate?
    )

    protected abstract fun isDayEnabled(day: CalendarDay?): Boolean
    fun setDayViewDecorators(results: List<DecoratorResult>?) {
        decoratorResults.clear()
        if (results != null) {
            decoratorResults.addAll(results)
        }
        invalidateDecorators()
    }

    fun setWeekDayTextAppearance(taId: Int) {
        for (weekDayView in weekDayViews) {
            weekDayView.setTextAppearance(context, taId)
        }
    }

    fun setDateTextAppearance(taId: Int) {
        for (dayView in dayViews) {
            dayView.setTextAppearance(context, taId)
        }
    }

    fun setSelectionEnabled(selectionEnabled: Boolean) {
        for (dayView in dayViews) {
            dayView.setOnClickListener(if (selectionEnabled) this else null)
            dayView.isClickable = selectionEnabled
        }
    }

    // this should surely change it's wrong in kotlin's eyes
    var selectionColor: Int
        get() = dayViews.first().selectionColor
        set(value) {
            for (dayView in dayViews) {
                dayView.selectionColor = value
            }
        }

    /*fun setSelectionColor(color: Int) {
        for (dayView in dayViews) {
            dayView.setSelectionColor(color)
        }
    }*/

    var weekDayFormatter: WeekDayFormatter? = null
        set(value) {
            field = value

            for (dayView in weekDayViews) {
                dayView.weekDayFormatter = value
            }
        }


    var dayFormatter: DayFormatter? = null
        set(value) {
            field = value
            for (dayView in dayViews) {
                dayView.setDayFormatter(value)
            }
        }

    var dayFormatterContentDescription: DayFormatter? = null
        set(value) {
            field = value
            for (dayView in dayViews) {
                dayView.setDayFormatterContentDescription(value)
            }
        }


    fun setMinimumDate(minDate: CalendarDay?) {
        this.minDate = minDate
        updateUi()
    }

    fun setMaximumDate(maxDate: CalendarDay?) {
        this.maxDate = maxDate
        updateUi()
    }

    fun setSelectedDates(dates: Collection<CalendarDay?>?) {
        for (dayView in dayViews) {
            val day = dayView.date
            dayView.isChecked = dates != null && dates.contains(day)
        }
        postInvalidate()
    }

    protected fun updateUi() {
        for (dayView in dayViews) {
            val day = dayView.date
            dayView.setupSelection(
                showOtherDates,
                day?.isInRange(minDate, maxDate) ?: false,
                isDayEnabled(day)
            )
        }
        postInvalidate()
    }

    protected fun invalidateDecorators() {
        val facadeAccumulator =
            DayViewFacade()
        for (dayView in dayViews) {
            facadeAccumulator.reset()
            for (result in decoratorResults) {
                if (result.decorator.shouldDecorate(dayView.date)) {
                    result.result.applyTo(facadeAccumulator)
                }
            }
            dayView.applyFacade(facadeAccumulator)
        }
    }

    override fun onClick(v: View) {
        if (v is DayView) {
            mcv.onDateClicked(v)
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (v is DayView) {
            mcv.onDateLongClicked(v)
            return true
        }
        return false
    }
    /*
     * Custom ViewGroup Code
     */
    /**
     * {@inheritDoc}
     */
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams()
    }

    /**
     * {@inheritDoc}
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val specWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val specHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        //We expect to be somewhere inside a MaterialCalendarView, which should measure EXACTLY
        check(!(specHeightMode == MeasureSpec.UNSPECIFIED || specWidthMode == MeasureSpec.UNSPECIFIED)) { "CalendarPagerView should never be left to decide it's size" }

        //The spec width should be a correct multiple
        val measureTileWidth = specWidthSize / DEFAULT_DAYS_IN_WEEK
        val measureTileHeight = specHeightSize / rows

        //Just use the spec sizes
        setMeasuredDimension(specWidthSize, specHeightSize)
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                measureTileWidth,
                MeasureSpec.EXACTLY
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                measureTileHeight,
                MeasureSpec.EXACTLY
            )
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
    }

    /**
     * Return the number of rows to display per page
     */
    protected abstract val rows: Int

    /**
     * {@inheritDoc}
     */
    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val parentWidth = width
        val count = childCount
        val parentLeft = 0
        var childTop = 0
        var childLeft = parentLeft
        var childRight = parentWidth
        for (i in 0 until count) {
            val child = getChildAt(i)
            val width = child.measuredWidth
            val height = child.measuredHeight
            if (LocalUtils.isRTL) {
                child.layout(childRight - width, childTop, childRight, childTop + height)
                childRight -= width
            } else {
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
                childLeft += width
            }

            //We should warp every so many children
            if (i % DEFAULT_DAYS_IN_WEEK == DEFAULT_DAYS_IN_WEEK - 1) {
                childLeft = parentLeft
                childRight = parentWidth
                childTop += height
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams()
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    /**
     * {@inheritDoc}
     */
    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = CalendarPagerView::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = CalendarPagerView::class.java.name
    }

    /**
     * Simple layout params class for MonthView, since every child is the same size
     */
    class LayoutParams
    /**
     * {@inheritDoc}
     */
        :
        MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

    companion object {
        const val DEFAULT_DAYS_IN_WEEK = 7
        const val DEFAULT_MAX_WEEKS = 6
        const val DAY_NAMES_ROW = 1
    }

    init {
        clipChildren = false
        clipToPadding = false
        if (showWeekDays) {
            buildWeekDays(resetAndGetWorkingCalendar())
        }
        @Suppress("LeakingThis")
        buildDayViews(dayViews, resetAndGetWorkingCalendar())
    }
}
