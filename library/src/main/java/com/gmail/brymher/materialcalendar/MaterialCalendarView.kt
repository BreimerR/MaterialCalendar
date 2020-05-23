package com.gmail.brymher.materialcalendar;

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.gmail.brymher.materialcalendar.variables.lazyVar
import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import com.gmail.brymher.materialcalendar.CalendarDay.Companion.today
import com.gmail.brymher.materialcalendar.format.*
import com.gmail.brymher.materialcalendar.format.DayFormatter.Companion.DEFAULT
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt


/**
 *
 *
 * This class is a calendar widget for displaying and selecting dates.
 * The range of dates supported by this calendar is configurable.
 * A user can select a date by taping on it and can page the calendar to a desired date.
 *
 *
 *
 * By default, the range of dates shown is from 200 years in the past to 200 years in the future.
 * This can be extended or shortened by configuring the minimum and maximum dates.
 *
 *
 *
 * When selecting a date out of range, or when the range changes so the selection becomes outside,
 * The date closest to the previous selection will become selected. This will also trigger the
 * [OnDateSelectedListener]
 *
 *
 *
 * **Note:** if this view's size isn't divisible by 7,
 * the contents will be centered inside such that the days in the calendar are equally square.
 * For example, 600px isn't divisible by 7, so a tile size of 85 is choosen, making the calendar
 * 595px wide. The extra 5px are distributed left and right to get to 600px.
 *
 */
class MaterialCalendarView : ViewGroup {

    var showTopBar = true

    var defaultVisibleWeeksCount = 6

    private val defaultTileSize
        get() = dpToPx(DEFAULT_TILE_SIZE_DP)

    /**
     * @return the height of tiles in pixels
     */
    var tileHeight = INVALID_TILE_DIMENSION
        /**
         * Set the height of each tile that makes up the calendar.
         *
         * @param height the new height for each tile in pixels
         */
        set(height: Int) {
            if (height > INVALID_TILE_DIMENSION) {
                field = height
                requestLayout()
            }
        }


    /**
     * [IntDef] annotation for selection mode.
     *
     * @see .setSelectionMode
     * @see .getSelectionMode
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
        SELECTION_MODE_NONE,
        SELECTION_MODE_SINGLE,
        SELECTION_MODE_MULTIPLE,
        SELECTION_MODE_RANGE
    )
    annotation class SelectionMode

    /**
     * [IntDef] annotation for showOtherDates.
     *
     * @see .setShowOtherDates
     * @see .getShowOtherDates
     */
    @SuppressLint("UniqueConstants")
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        flag = true,
        value = [SHOW_NONE, SHOW_ALL, SHOW_DEFAULTS, SHOW_OUT_OF_RANGE, SHOW_OTHER_MONTHS, SHOW_DECORATED_DISABLED]
    )
    annotation class ShowOtherDates

    fun setMode(mode: CalendarMode?) {
        state()!!.edit()
            .setCalendarDisplayMode(mode!!)
            .commit(this)
    }

    val inflater get() = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    // SHOULD BE INSIDE by lazy
    val content = inflater.inflate(R.layout.calendar_view, null, false)

    val title: TextView? get() = content.findViewById(R.id.month_name)
    val titleChanger: TitleChanger = TitleChanger(title)
    private val buttonPast: ImageView? get() = content.findViewById(R.id.previous)
    private val btnNextYear: ImageButton? get() = content.findViewById(R.id.next_year)
    private val btnPrevYear: ImageButton? get() = content.findViewById(R.id.prev_year)
    private val buttonFuture: ImageView? get() = content.findViewById(R.id.next)
    private val pager: CalendarPager by lazy {
        CalendarPager(getContext())
    }
    lateinit var adapter: CalendarPagerAdapter<*>
    var currentMonth: CalendarDay? by lazyVar {
        today
    }

    val topbar: LinearLayout? get() = content.findViewById(R.id.header)

    /**
     * Get the current [CalendarMode] set of the Calendar.
     *
     * @return Whichever mode the calendar is currently in.
     */
    var calendarMode: CalendarMode? by lazyVar {
        CalendarMode.MONTHS
    }

    /**
     * Used for the dynamic calendar height.
     */
    var mDynamicHeightEnabled = false
    val dayViewDecorators = ArrayList<DayViewDecorator>()

    private val onClickListener =
        OnClickListener { v ->
            if (v === buttonFuture) {
                nextDay(true)
            } else if (v === buttonPast) {
                previousDay(true)
            } else if (v === btnNextYear) {
                nextYear(true)
            } else if (v === btnPrevYear) previousYear(true)
        }

    fun updateViewPager(position: Int, smoothScroll: Boolean) {
        pager.setCurrentItem(pager.currentItem + position, smoothScroll)
    }

    fun nextDay(smoothScroll: Boolean) {
        updateViewPager(1, smoothScroll)
    }

    fun previousDay(smoothScroll: Boolean) {
        updateViewPager(-1, smoothScroll)
    }

    private fun nextYear(smoothScroll: Boolean) {
        updateViewPager(yearUpdateDifference, smoothScroll)
    }

    private fun previousYear(smoothScroll: Boolean) {
        updateViewPager(-yearUpdateDifference, smoothScroll)
    }

    private val yearUpdateDifference: Int
        private get() {
            var updateDifference = 12
            if (calendarMode === CalendarMode.WEEKS) updateDifference = 52
            return updateDifference
        }

    private val pageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            titleChanger.previousMonth = currentMonth
            currentMonth = adapter.getItem(position)
            updateUi()
            dispatchOnMonthChanged(currentMonth)
        }

        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }
    }
    private var minDate: CalendarDay? = null
    private var maxDate: CalendarDay? = null
    private var listener: OnDateSelectedListener? = null
    private var longClickListener: OnDateLongClickListener? = null
    private var monthListener: OnMonthChangedListener? = null
    private var rangeListener: OnRangeSelectedListener? = null

    /**
     * Get content description for calendar
     *
     * @return calendar's content description
     */
    var calendarContentDescription: CharSequence? = null
        get() {
            return (if (field != null) field else context.getString(
                R.string.calendar
            ))
        }

    private var accentColor = 0

    /**
     * @return the width of tiles in pixels
     */
    var tileWidth = INVALID_TILE_DIMENSION
        /**
         * Set the width of each tile that makes up the calendar.
         *
         * @param width the new width for each tile in pixels
         */
        set(width: Int) {
            if (width > INVALID_TILE_DIMENSION) {
                field = width
                requestLayout()
            }
        }


    /**
     * Get the current selection mode. The default mode is [.SELECTION_MODE_SINGLE]
     *
     * @return the current selection mode
     * @see .setSelectionMode
     * @see .SELECTION_MODE_NONE
     *
     * @see .SELECTION_MODE_SINGLE
     *
     * @see .SELECTION_MODE_MULTIPLE
     *
     * @see .SELECTION_MODE_RANGE
     */
    @SelectionMode
    var selectionMode = SELECTION_MODE_SINGLE
        /**
         * Change the selection mode of the calendar. The default mode is [ ][.SELECTION_MODE_SINGLE]
         *
         * @param mode the selection mode to change to. This must be one of
         * [.SELECTION_MODE_NONE], [.SELECTION_MODE_SINGLE],
         * [.SELECTION_MODE_RANGE] or [.SELECTION_MODE_MULTIPLE].
         * Unknown values will act as [.SELECTION_MODE_SINGLE]
         * @see .getSelectionMode
         * @see .SELECTION_MODE_NONE
         *
         * @see .SELECTION_MODE_SINGLE
         *
         * @see .SELECTION_MODE_MULTIPLE
         *
         * @see .SELECTION_MODE_RANGE
         */
        set(@SelectionMode mode: Int) {
            @SelectionMode val oldMode = field
            field = mode
            when (mode) {
                SELECTION_MODE_RANGE -> {
                    field = SELECTION_MODE_RANGE
                    clearSelection()
                    TODO("Implement {@link MaterialCalendarView$selectionMode}")
                }
                SELECTION_MODE_MULTIPLE -> {
                    field = SELECTION_MODE_MULTIPLE
                    clearSelection()
                    TODO("Implement {@link MaterialCalendarView$selectionMode}")
                }
                SELECTION_MODE_SINGLE -> if (oldMode == SELECTION_MODE_MULTIPLE || oldMode == SELECTION_MODE_RANGE) {
                    //We should only have one selection now, so we should pick one
                    val dates = getSelectedDates()
                    if (dates.isNotEmpty()) {
                        setSelectedDate(selectedDate)
                    }
                }
                SELECTION_MODE_NONE -> {
                    field = SELECTION_MODE_NONE
                    if (oldMode != SELECTION_MODE_NONE) {
                        //No selection! Clear out!
                        clearSelection()
                    }
                }
                else -> {
                    field = SELECTION_MODE_NONE
                    if (oldMode != SELECTION_MODE_NONE) {
                        clearSelection()
                    }
                }
            }
            adapter.setSelectionEnabled(selectionMode != SELECTION_MODE_NONE)
        }
    private var allowClickDaysOutsideCurrentMonth = true

    /**
     * @return The first day of the week as a [Calendar] day constant.
     */
    var firstDayOfWeekInt = -1
        set(value) {
            field = value

            // update first day of week
            if (value in 1..7) firstDayOfWeek = DayOfWeek.of(value)
        }

    var firstDayOfWeek: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
        set(value) {
            field = value
        }

    /**
     * @return true if the week days names are shown
     */
    var isShowWeekDays = false
        private set
    private var state: State? = null

    constructor(context: Context) : super(context) {
        if (isInEditMode) {
            AndroidThreeTen.init(context)
        }
        initializeStyleProperties(context, null)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        if (isInEditMode) {
            AndroidThreeTen.init(context)
        }

        initializeStyleProperties(context, attrs)
    }

    private fun initializeControls() {
        buttonPast?.setOnClickListener(onClickListener)
        buttonFuture?.setOnClickListener(onClickListener)
        btnNextYear?.setOnClickListener(onClickListener)
        btnPrevYear?.setOnClickListener(onClickListener)
    }

    private fun setupChildren() {
        addView(topbar)
        pager.id = R.id.mcv_pager
        pager.offscreenPageLimit = 1

        calendarMode?.let {
            tileHeight =
                if (isShowWeekDays) it.visibleWeeksCount + DAY_NAMES_ROW else it.visibleWeeksCount
        }

        addView(pager, LayoutParams(tileHeight))

    }

    private fun updateUi() {
        if (isInEditMode) {
            titleChanger.previousMonth = currentMonth!!
        }
        titleChanger.change(currentMonth)
        enableView(buttonPast, canGoBack())
        enableView(buttonFuture, canGoForward())
    }


    /**
     * Go to previous month or week without using the button [.buttonPast]. Should only go to
     * previous if [.canGoBack] is true, meaning it's possible to go to the previous month
     * or week.
     */
    fun goToPrevious() {
        if (canGoBack()) {
            pager!!.setCurrentItem(pager.currentItem - 1, true)
        }
    }

    /**
     * Go to next month or week without using the button [.buttonFuture]. Should only go to
     * next if [.canGoForward] is enabled, meaning it's possible to go to the next month or
     * week.
     */
    fun goToNext() {
        if (canGoForward()) {
            pager.setCurrentItem(pager.currentItem + 1, true)
        }
    }

    /**
     * Use [.getTileWidth] or [.getTileHeight] instead. This method is deprecated
     * and will just return the largest of the two sizes.
     *
     * @return tile height or width, whichever is larger
     */
    /**
     * Set the size of each tile that makes up the calendar.
     * Each day is 1 tile, so the widget is 7 tiles wide and 7 or 8 tiles tall
     * depending on the visibility of the [.topbar].
     *
     * @param size the new size for each tile in pixels
     */
    @get:Deprecated("")
    var tileSize: Int
        get() = Math.max(tileHeight, tileWidth)
        set(size) {
            if (size > INVALID_TILE_DIMENSION) {
                tileWidth = size
                tileHeight = size
                requestLayout()
            }
        }

    /**
     * @param tileSizeDp the new size for each tile in dips
     * @see .setTileSize
     */
    fun setTileSizeDp(tileSizeDp: Int) {
        tileSize = dpToPx(tileSizeDp)
    }


    /**
     * @param tileHeightDp the new height for each tile in dips
     * @see .setTileHeight
     */
    fun setTileHeightDp(tileHeightDp: Int) {
        tileHeight = dpToPx(tileHeightDp)
    }


    /**
     * @param tileWidthDp the new width for each tile in dips
     * @see .setTileWidth
     */
    fun setTileWidthDp(tileWidthDp: Int) {
        tileWidth = dpToPx(tileWidthDp)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    /**
     * Whether the pager can page forward, meaning the future month is enabled.
     *
     * @return true if there is a future month that can be shown
     */
    fun canGoForward(): Boolean {
        return (pager?.currentItem ?: 2) < adapter.count - 1
    }

    /**
     * Whether the pager can page backward, meaning the previous month is enabled.
     *
     * @return true if there is a previous month that can be shown
     */
    fun canGoBack(): Boolean {
        return pager.currentItem > 0
    }

    /**
     * Pass all touch events to the pager so scrolling works on the edges of the calendar view.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return pager.dispatchTouchEvent(event)
    }

    /**
     * @return the color used for the selection
     */
    /**
     * @param color The selection color
     */
    var selectionColor: Int
        get() = accentColor
        set(color) {
            var color = color
            if (color == 0) {
                color = if (!isInEditMode) {
                    return
                } else {
                    Color.GRAY
                }
            }
            accentColor = color
            adapter.selectionColor = color
            invalidate()
        }

    /**
     * Set content description for button past
     *
     * @param description String to use as content description
     */
    fun setContentDescriptionArrowPast(description: DayFormatter?) {
        buttonPast?.contentDescription = description.toString()
    }

    /**
     * Set content description for button future
     *
     * @param description String to use as content description
     */
    fun setContentDescriptionArrowFuture(description: CharSequence?) {
        buttonFuture?.contentDescription = description
    }

    /**
     * Set content description for calendar
     *
     * @param description String to use as content description
     */
    fun setContentDescriptionCalendar(description: CharSequence?) {
        calendarContentDescription = description
    }


    /**
     * Set a formatter for day content description.
     *
     * @param formatter the new formatter, null for default
     */
    fun setDayFormatterContentDescription(formatter: DayFormatter?) {
        adapter.setDayFormatterContentDescription(formatter!!)
    }

    /**
     * @return icon used for the left arrow
     */
    var leftArrow: Drawable?
        get() = buttonPast?.drawable
        set(drawable) {
            buttonPast?.setImageDrawable(drawable)
        }

    var leftArrowRes: Int = R.drawable.mcv_action_previous
        set(@DrawableRes resId) {
            field = resId
            buttonPast?.setImageResource(resId)

        }

    /**
     * @param icon the new icon to use for the left paging arrow
     *
     * @Deprecated("Use {@link MaterialCalendarView$leftArrowRes}")
     */
    fun setLeftArrow(@DrawableRes icon: Int) {
        buttonPast?.setImageResource(icon)
    }


    /**
     * @return icon used for the right arrow
     */
    val rightArrow: Drawable?
        get() = buttonFuture?.drawable

    var rightArrowRes: Int = R.drawable.mcv_action_next
        set(@DrawableRes resId) {
            field = resId
            buttonFuture?.setImageResource(resId)
        }

    /**
     * @param resourceId The text appearance resource id.
     */
    fun setHeaderTextAppearance(resourceId: Int) {
        title?.setTextAppearance(context, resourceId)
    }

    /**
     * @param resourceId The text appearance resource id.
     */
    fun setDateTextAppearance(resourceId: Int) {
        adapter.setDateTextAppearance(resourceId)
    }

    /**
     * @param resourceId The text appearance resource id.
     */
    fun setWeekDayTextAppearance(resourceId: Int) {
        adapter.setWeekDayTextAppearance(resourceId)
    }

    /**
     * Get the currently selected date, or null if no selection. Depending on the selection mode,
     * you might get different results.
     *
     *
     * For [.SELECTION_MODE_SINGLE], returns the selected date.
     *
     * For [.SELECTION_MODE_MULTIPLE], returns the last date selected.
     *
     * For [.SELECTION_MODE_RANGE], returns the last date of the range. In most cases, you
     * should probably be using [.getSelectedDates].
     *
     * For [.SELECTION_MODE_NONE], returns null.
     *
     * @return The selected day, or null if no selection. If in multiple selection mode, this
     * will return the last date of the list of selected dates.
     * @see MaterialCalendarView.getSelectedDates
     */
    val selectedDate: CalendarDay?
        get() {
            val dates: List<CalendarDay> = adapter.getSelectedDates()
            return if (dates.isEmpty()) {
                null
            } else {
                dates[dates.size - 1]
            }

        }


    /**
     * Return the list of currently selected dates. Mostly useful for [.SELECTION_MODE_MULTIPLE]
     * and [.SELECTION_MODE_RANGE]. For the other modes, check [.getSelectedDate].
     *
     *
     * For [.SELECTION_MODE_MULTIPLE], returns the list in the order of selection.
     *
     * For [.SELECTION_MODE_RANGE], returns the range of dates ordered chronologically.
     *
     * @return All of the currently selected dates.
     * @see {@link MaterialCalendarView$selectedDate}
     */
    fun getSelectedDates(): List<CalendarDay> {
        return adapter.getSelectedDates()
    }

    /**
     * Clear the currently selected date(s)
     */
    fun clearSelection() {
        val dates = getSelectedDates()
        adapter.clearSelections()
        for (day in dates) {
            dispatchOnDateSelected(day, false)
        }
    }

    /**
     * @param date a Date set to a day to select. Null to clear selection
     */
    fun setSelectedDate(date: LocalDate?) {
        setSelectedDate(from(date))
    }

    /**
     * @param date a Date to set as selected. Null to clear selection
     */
    fun setSelectedDate(date: CalendarDay?) {
        clearSelection()
        if (date != null) {
            setDateSelected(date, true)
        }
    }

    /**
     * @param day      a CalendarDay to change. Passing null does nothing
     * @param selected true if day should be selected, false to deselect
     */
    fun setDateSelected(day: CalendarDay?, selected: Boolean) {
        if (day == null) {
            return
        }
        adapter.setDateSelected(day, selected)
    }

    /**
     * Get the current first day of the month in month mode, or the first visible day of the
     * currently visible week.
     *
     *
     * For example, in week mode, if the week is July 29th, 2018 to August 4th, 2018,
     * this will return July 29th, 2018. If in month mode and the month is august, then this method
     * will return August 1st, 2018.
     *
     * @return The current month or week shown, will be set to first day of the month in month mode,
     * or the first visible day for a week.
     */
    fun getCurrentDate(): CalendarDay? {
        return adapter.getItem(pager.currentItem)
    }

    /**
     * Set the calendar to a specific month or week based on a date.
     *
     *
     * In month mode, the calendar will be set to the corresponding month.
     *
     *
     * In week mode, the calendar will be set to the corresponding week.
     *
     * @param calendar a Calendar set to a day to focus the calendar on. Null will do nothing
     */
    fun setCurrentDate(calendar: LocalDate?) {
        setCurrentDate(from(calendar))
    }

    /**
     * Set the calendar to a specific month or week based on a date.
     *
     *
     * In month mode, the calendar will be set to the corresponding month.
     *
     *
     * In week mode, the calendar will be set to the corresponding week.
     *
     * @param day a CalendarDay to focus the calendar on. Null will do nothing
     */
    fun setCurrentDate(day: CalendarDay?) {
        setCurrentDate(day, true)
    }

    /**
     * Set the calendar to a specific month or week based on a date.
     *
     *
     * In month mode, the calendar will be set to the corresponding month.
     *
     *
     * In week mode, the calendar will be set to the corresponding week.
     *
     * @param day             a CalendarDay to focus the calendar on. Null will do nothing
     * @param useSmoothScroll use smooth scroll when changing months.
     */
    fun setCurrentDate(day: CalendarDay?, useSmoothScroll: Boolean) {
        if (day == null) {
            return
        }

        if (!::adapter.isInitialized) {
            val index = adapter.getIndexForDay(day)
            pager.setCurrentItem(index, useSmoothScroll)
        }

        updateUi()
    }

    /**
     * @return the minimum selectable date for the calendar, if any
     */
    fun getMinimumDate(): CalendarDay? {
        return minDate
    }

    /**
     * @return the maximum selectable date for the calendar, if any
     */
    fun getMaximumDate(): CalendarDay? {
        return maxDate
    }

    /**
     * The default value is [.SHOW_DEFAULTS], which currently is just [ ][.SHOW_DECORATED_DISABLED].
     * This means that the default visible days are of the current month, in the min-max range.
     *
     * @param showOtherDates flags for showing non-enabled dates
     * @see .SHOW_ALL
     *
     * @see .SHOW_NONE
     *
     * @see .SHOW_DEFAULTS
     *
     * @see .SHOW_OTHER_MONTHS
     *
     * @see .SHOW_OUT_OF_RANGE
     *
     * @see .SHOW_DECORATED_DISABLED
     */
    fun setShowOtherDates(@ShowOtherDates showOtherDates: Int) {
        adapter.setShowOtherDates(showOtherDates)
    }

    /**
     * Allow the user to click on dates from other months that are not out of range. Go to next or
     * previous month if a day outside the current month is clicked. The day still need to be
     * enabled to be selected.
     * Default value is true. Should be used with [.SHOW_OTHER_MONTHS].
     *
     * @param enabled True to allow the user to click on a day outside current month displayed
     */
    fun setAllowClickDaysOutsideCurrentMonth(enabled: Boolean) {
        allowClickDaysOutsideCurrentMonth = enabled
    }

    fun setTextColor(color: Int) {}

    /**
     * Set a formatter for weekday labels.
     *
     * @param formatter the new formatter, null for default
     */
    fun setWeekDayFormatter(formatter: WeekDayFormatter?) {
        adapter.weekDayFormatter = formatter ?: WeekDayFormatter.DEFAULT
    }

    /**
     * Set a formatter for day labels.
     *
     * @param formatter the new formatter, null for default
     */
    fun setDayFormatter(formatter: DayFormatter?) {
        adapter.setDayFormatter(formatter ?: DEFAULT)
    }

    /**
     * Set a [WeekDayFormatter]
     * with the provided week day labels
     *
     * @param weekDayLabels Labels to use for the days of the week
     * @see ArrayWeekDayFormatter
     *
     * @see .setWeekDayFormatter
     */
    fun setWeekDayLabels(weekDayLabels: Array<CharSequence?>?) {
        setWeekDayFormatter(ArrayWeekDayFormatter(weekDayLabels))
    }

    /**
     * Set a [WeekDayFormatter]
     * with the provided week day labels
     *
     * @param arrayRes String array resource of week day labels
     * @see ArrayWeekDayFormatter
     *
     * @see .setWeekDayFormatter
     */
    fun setWeekDayLabels(@ArrayRes arrayRes: Int) {
        setWeekDayLabels(resources.getTextArray(arrayRes))
    }

    /**
     * @return int of flags used for showing non-enabled dates
     * @see .SHOW_ALL
     *
     * @see .SHOW_NONE
     *
     * @see .SHOW_DEFAULTS
     *
     * @see .SHOW_OTHER_MONTHS
     *
     * @see .SHOW_OUT_OF_RANGE
     *
     * @see .SHOW_DECORATED_DISABLED
     */
    @ShowOtherDates
    fun getShowOtherDates(): Int {
        return adapter.getShowOtherDates()
    }

    /**
     * @return true if allow click on days outside current month displayed
     */
    fun allowClickDaysOutsideCurrentMonth(): Boolean {
        return allowClickDaysOutsideCurrentMonth
    }

    /**
     * Set a custom formatter for the month/year title
     *
     * @param titleFormatter new formatter to use, null to use default formatter
     */
    fun setTitleFormatter(titleFormatter: TitleFormatter?) {
        titleChanger.titleFormatter = titleFormatter
        adapter.titleFormatter = titleFormatter
        updateUi()
    }

    /**
     * Set a [TitleFormatter]
     * using the provided month labels
     *
     * @param monthLabels month labels to use
     * @see MonthArrayTitleFormatter
     *
     * @see .setTitleFormatter
     */
    fun setTitleMonths(monthLabels: Array<CharSequence?>?) {
        setTitleFormatter(MonthArrayTitleFormatter(monthLabels))
    }

    /**
     * Set a [TitleFormatter]
     * using the provided month labels
     *
     * @param arrayRes String array resource of month labels to use
     * @see MonthArrayTitleFormatter
     *
     * @see .setTitleFormatter
     */
    fun setTitleMonths(@ArrayRes arrayRes: Int) {
        setTitleMonths(resources.getTextArray(arrayRes))
    }

    /**
     * Change the title animation orientation to have a different look and feel.
     *
     * @param orientation [MaterialCalendarView.VERTICAL] or [                    ][MaterialCalendarView.HORIZONTAL]
     */
    fun setTitleAnimationOrientation(orientation: Int) {
        titleChanger.orientation = orientation
    }

    /**
     * Get the orientation of the animation of the title.
     *
     * @return Title animation orientation [MaterialCalendarView.VERTICAL] or [ ][MaterialCalendarView.HORIZONTAL]
     */
    fun getTitleAnimationOrientation(): Int {
        return titleChanger.orientation
    }

    /**
     * Sets the visibility [.topbar], which contains
     * the previous month button [.buttonPast], next month button [.buttonFuture],
     * and the month title [.title].
     *
     * @param visible Boolean indicating if the topbar is visible
     */


    var topbarVisible: Boolean
        /**
         * @return true if the topbar is visible
         */
        get(): Boolean {
            return topbar?.visibility == View.VISIBLE
        }
        set(visibility) {
            topbar?.visibility = if (visibility) View.VISIBLE else View.GONE
            requestLayout()
        }

    override fun onSaveInstanceState(): Parcelable? {
        val ss =
            SavedState(super.onSaveInstanceState())
        ss.showOtherDates = getShowOtherDates()
        ss.allowClickDaysOutsideCurrentMonth = allowClickDaysOutsideCurrentMonth()
        ss.minDate = getMinimumDate()
        ss.maxDate = getMaximumDate()
        ss.selectedDates = getSelectedDates()
        ss.selectionMode = selectionMode
        ss.topbarVisible = topbarVisible
        ss.dynamicHeightEnabled = mDynamicHeightEnabled
        ss.currentMonth = currentMonth
        ss.cacheCurrentPosition = state!!.cacheCurrentPosition
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        state()!!.edit()
            .setMinimumDate(ss.minDate)
            .setMaximumDate(ss.maxDate)
            .isCacheCalendarPositionEnabled(ss.cacheCurrentPosition)
            .commit(this)
        setShowOtherDates(ss.showOtherDates)
        setAllowClickDaysOutsideCurrentMonth(ss.allowClickDaysOutsideCurrentMonth)
        clearSelection()
        for (calendarDay in ss.selectedDates) {
            setDateSelected(calendarDay, true)
        }
        topbarVisible = ss.topbarVisible
        selectionMode = ss.selectionMode
        setDynamicHeightEnabled(ss.dynamicHeightEnabled)
        setCurrentDate(ss.currentMonth)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    private fun setRangeDates(min: CalendarDay?, max: CalendarDay?) {
        val c = currentMonth
        adapter.setRangeDates(min, max)
        currentMonth = c
        if (min != null) {
            currentMonth = if (min.isAfter(currentMonth!!)) min else currentMonth
        }
        val position = adapter.getIndexForDay(c)
        pager.setCurrentItem(position, false)
        updateUi()
    }

    class SavedState : BaseSavedState {
        var showOtherDates = SHOW_DEFAULTS
        var allowClickDaysOutsideCurrentMonth = true
        var minDate: CalendarDay? = null
        var maxDate: CalendarDay? = null
        var selectedDates: List<CalendarDay> = ArrayList()
        var topbarVisible = true
        var selectionMode = SELECTION_MODE_SINGLE
        var dynamicHeightEnabled = false
        var currentMonth: CalendarDay? = null
        var cacheCurrentPosition = false

        // TODO not sure about this functionality
        internal constructor(superState: Parcelable?) : super(superState) {}

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(showOtherDates)
            out.writeByte((if (allowClickDaysOutsideCurrentMonth) 1 else 0).toByte())
            out.writeParcelable(minDate, 0)
            out.writeParcelable(maxDate, 0)
            out.writeTypedList(selectedDates)
            out.writeInt(if (topbarVisible) 1 else 0)
            out.writeInt(selectionMode)
            out.writeInt(if (dynamicHeightEnabled) 1 else 0)
            out.writeParcelable(currentMonth, 0)
            out.writeByte((if (cacheCurrentPosition) 1 else 0).toByte())
        }

        private constructor(parcel: Parcel) : super(parcel) {
            showOtherDates = parcel.readInt()
            allowClickDaysOutsideCurrentMonth = parcel.readByte().toInt() != 0
            val loader = CalendarDay::class.java.classLoader
            minDate = parcel.readParcelable(loader)
            maxDate = parcel.readParcelable(loader)
            parcel.readTypedList(selectedDates, CalendarDay.CREATOR)
            topbarVisible = parcel.readInt() == 1
            selectionMode = parcel.readInt()
            dynamicHeightEnabled = parcel.readInt() == 1
            currentMonth = parcel.readParcelable(loader)
            cacheCurrentPosition = parcel.readByte().toInt() != 0

        }

        companion object {
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState? = SavedState(parcel)

                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)

            }
        }
    }

    /**
     * By default, the calendar will take up all the space needed to show any month (6 rows).
     * By enabling dynamic height, the view will change height dependant on the visible month.
     *
     *
     * This means months that only need 5 or 4 rows to show the entire month will only take up
     * that many rows, and will grow and shrink as necessary.
     *
     * @param useDynamicHeight true to have the view different heights based on the visible month
     */
    fun setDynamicHeightEnabled(useDynamicHeight: Boolean) {
        mDynamicHeightEnabled = useDynamicHeight
    }

    /**
     * @return the dynamic height state - true if enabled.
     */
    fun isDynamicHeightEnabled(): Boolean {
        return mDynamicHeightEnabled
    }

    /**
     * Add a collection of day decorators
     *
     * @param decorators decorators to add
     */
    fun addDecorators(decorators: MutableList<DayViewDecorator?>?) {

        decorators?.let {

            for (i in it) {
                i?.let {
                    dayViewDecorators.add(i)
                }

            }

            adapter.decorators = dayViewDecorators
        }


    }

    /**
     * Add several day decorators
     *
     * @param decorators decorators to add
     */
    fun addDecorators(vararg decorators: DayViewDecorator?) {
        addDecorators(mutableListOf<DayViewDecorator?>(*decorators))
    }

    /**
     * Add a day decorator
     *
     * @param decorator decorator to add
     */
    fun addDecorator(decorator: DayViewDecorator?) {
        if (decorator == null) {
            return
        }
        dayViewDecorators.add(decorator)
        adapter.decorators = dayViewDecorators
    }

    /**
     * Remove all decorators
     */
    fun removeDecorators() {
        dayViewDecorators.clear()
        adapter.decorators = dayViewDecorators
    }

    /**
     * Remove a specific decorator instance. Same rules as [List.remove]
     *
     * @param decorator decorator to remove
     */
    fun removeDecorator(decorator: DayViewDecorator) {
        dayViewDecorators.remove(decorator)
        adapter.decorators = dayViewDecorators
    }

    /**
     * Invalidate decorators after one has changed internally. That is, if a decorator mutates, you
     * should call this method to update the widget.
     */
    fun invalidateDecorators() {
        adapter.invalidateDecorators()
    }
    /*
     * Listener/Callback Code
     */
    /**
     * Sets the listener to be notified upon selected date changes.
     *
     * @param listener thing to be notified
     */
    fun setOnDateChangedListener(listener: OnDateSelectedListener?) {
        this.listener = listener
    }

    /**
     * Sets the listener to be notified upon long clicks on dates.
     *
     * @param longClickListener thing to be notified
     */
    fun setOnDateLongClickListener(longClickListener: OnDateLongClickListener?) {
        this.longClickListener = longClickListener
    }

    /**
     * Sets the listener to be notified upon month changes.
     *
     * @param listener thing to be notified
     */
    fun setOnMonthChangedListener(listener: OnMonthChangedListener?) {
        monthListener = listener
    }

    /**
     * Sets the listener to be notified upon a range has been selected.
     *
     * @param listener thing to be notified
     */
    fun setOnRangeSelectedListener(listener: OnRangeSelectedListener?) {
        rangeListener = listener
    }

    /**
     * Add listener to the title or null to remove it.
     *
     * @param listener Listener to be notified.
     */
    fun setOnTitleClickListener(listener: OnClickListener?) {
        title?.setOnClickListener(listener)
    }

    /**
     * Dispatch date change events to a listener, if set
     *
     * @param day      the day that was selected
     * @param selected true if the day is now currently selected, false otherwise
     */
    protected fun dispatchOnDateSelected(day: CalendarDay?, selected: Boolean) {
        if (listener != null) {
            listener!!.onDateSelected(this@MaterialCalendarView, day!!, selected)
        }
    }

    /**
     * Dispatch a range of days to a range listener, if set, ordered chronologically.
     *
     * @param days Enclosing days ordered from first to last day.
     */
    protected fun dispatchOnRangeSelected(days: List<CalendarDay?>) {
        if (rangeListener != null) {
            rangeListener!!.onRangeSelected(this@MaterialCalendarView, days)
        }
    }

    /**
     * Dispatch date change events to a listener, if set
     *
     * @param day first day of the new month
     */
    protected fun dispatchOnMonthChanged(day: CalendarDay?) {
        if (monthListener != null) {
            monthListener!!.onMonthChanged(this@MaterialCalendarView, day)
        }
    }

    /**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it.
     * This method will always process the click to the selected date.
     *
     * @param date        date of the day that was clicked
     * @param nowSelected true if the date is now selected, false otherwise
     */
    protected fun onDateClicked(date: CalendarDay, nowSelected: Boolean) {
        when (selectionMode) {
            SELECTION_MODE_MULTIPLE -> {
                adapter.setDateSelected(date, nowSelected)
                dispatchOnDateSelected(date, nowSelected)
            }
            SELECTION_MODE_RANGE -> {
                val currentSelection: List<CalendarDay> =
                    adapter.getSelectedDates()
                if (currentSelection.isEmpty()) {
                    // Selecting the first date of a range
                    adapter.setDateSelected(date, nowSelected)
                    dispatchOnDateSelected(date, nowSelected)
                } else if (currentSelection.size == 1) {
                    // Selecting the second date of a range
                    val firstDaySelected = currentSelection[0]
                    if (firstDaySelected.equals(date)) {
                        // Right now, we are not supporting a range of one day, so we are removing the day instead.
                        adapter.setDateSelected(date, nowSelected)
                        dispatchOnDateSelected(date, nowSelected)
                    } else if (firstDaySelected.isAfter(date)) {
                        // Selecting a range, dispatching in reverse order...
                        adapter.selectRange(date, firstDaySelected)
                        dispatchOnRangeSelected(adapter.getSelectedDates())
                    } else {
                        // Selecting a range, dispatching in order...
                        adapter.selectRange(firstDaySelected, date)
                        dispatchOnRangeSelected(adapter.getSelectedDates())
                    }
                } else {
                    // Clearing selection and making a selection of the new date.
                    adapter.clearSelections()
                    adapter.setDateSelected(date, nowSelected)
                    dispatchOnDateSelected(date, nowSelected)
                }
            }
            SELECTION_MODE_SINGLE -> {
                adapter.clearSelections()
                adapter.setDateSelected(date, true)
                dispatchOnDateSelected(date, true)
            }
            else -> {
                adapter.clearSelections()
                adapter.setDateSelected(date, true)
                dispatchOnDateSelected(date, true)
            }
        }
    }

    /**
     * Select a fresh range of date including first day and last day.
     *
     * @param firstDay first day of the range to select
     * @param lastDay  last day of the range to select
     */
    fun selectRange(firstDay: CalendarDay?, lastDay: CalendarDay?) {
        if (firstDay == null || lastDay == null) {
            return
        } else if (firstDay.isAfter(lastDay)) {
            adapter.selectRange(lastDay, firstDay)
            dispatchOnRangeSelected(adapter.getSelectedDates())
        } else {
            adapter.selectRange(firstDay, lastDay)
            dispatchOnRangeSelected(adapter.getSelectedDates())
        }
    }

    /**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it
     */
    fun onDateClicked(dayView: DayView) {
        val currentDate = getCurrentDate()
        val selectedDate = dayView.date
        val currentMonth = currentDate!!.month
        val selectedMonth = selectedDate!!.month
        if (calendarMode === CalendarMode.MONTHS && allowClickDaysOutsideCurrentMonth
            && currentMonth != selectedMonth
        ) {
            if (currentDate.isAfter(selectedDate)) {
                goToPrevious()
            } else if (currentDate.isBefore(selectedDate)) {
                goToNext()
            }
        }
        onDateClicked(dayView.date!!, !dayView.isChecked)
    }

    /**
     * Call by [CalendarPagerView] to indicate that a day was long clicked and we should handle
     * it
     */
    fun onDateLongClicked(dayView: DayView) {
        if (longClickListener != null) {
            longClickListener!!.onDateLongClick(this@MaterialCalendarView, dayView.date!!)
        }
    }

    /**
     * Called by the adapter for cases when changes in state result in dates being unselected
     *
     * @param date date that should be de-selected
     */
    fun onDateUnselected(date: CalendarDay?) {
        dispatchOnDateSelected(date, false)
    }
    /*
     * Custom ViewGroup Code
     */
    /**
     * {@inheritDoc}
     */
    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(1)
    }

    /**
     * {@inheritDoc}
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val specWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val specHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        //We need to disregard padding for a while. This will be added back later
        val desiredWidth = specWidthSize - paddingLeft - paddingRight
        val desiredHeight = specHeightSize - paddingTop - paddingBottom
        val weekCount = getWeekCountBasedOnMode()
        val viewTileHeight = if (topbarVisible) weekCount + 1 else weekCount

        //Calculate independent tile sizes for later
        val desiredTileWidth = desiredWidth / DEFAULT_DAYS_IN_WEEK
        val desiredTileHeight = desiredHeight / viewTileHeight
        var measureTileSize = -1
        var measureTileWidth = -1
        var measureTileHeight = -1

        if (tileWidth != INVALID_TILE_DIMENSION || tileHeight != INVALID_TILE_DIMENSION) {
            measureTileWidth = if (tileWidth > 0) {
                //We have a tileWidth set, we should use that
                tileWidth
            } else {
                desiredTileWidth
            }
            measureTileHeight = if (tileHeight > 0) {
                //We have a tileHeight set, we should use that
                tileHeight
            } else {
                desiredTileHeight
            }
        } else if (specWidthMode == MeasureSpec.EXACTLY || specWidthMode == MeasureSpec.AT_MOST) {
            measureTileSize = if (specHeightMode == MeasureSpec.EXACTLY) {
                //Pick the smaller of the two explicit sizes
                Math.min(desiredTileWidth, desiredTileHeight)
            } else {
                //Be the width size the user wants
                desiredTileWidth
            }
        } else if (specHeightMode == MeasureSpec.EXACTLY || specHeightMode == MeasureSpec.AT_MOST) {
            //Be the height size the user wants
            measureTileSize = desiredTileHeight
        }
        if (measureTileSize > 0) {
            //Use measureTileSize if set
            measureTileHeight = measureTileSize
            measureTileWidth = measureTileSize
        } else if (measureTileSize <= 0) {
            if (measureTileWidth <= 0) {
                //Set width to default if no value were set
                measureTileWidth = dpToPx(DEFAULT_TILE_SIZE_DP)
            }
            if (measureTileHeight <= 0) {
                //Set height to default if no value were set
                measureTileHeight = dpToPx(DEFAULT_TILE_SIZE_DP)
            }
        }

        //Calculate our size based off our measured tile size
        var measuredWidth =
            measureTileWidth * DEFAULT_DAYS_IN_WEEK
        var measuredHeight = measureTileHeight * viewTileHeight

        //Put padding back in from when we took it away
        measuredWidth += paddingLeft + paddingRight
        measuredHeight += paddingTop + paddingBottom

        //Contract fulfilled, setting out measurements
        setMeasuredDimension( //We clamp inline because we want to use un-clamped versions on the children
            clampSize(measuredWidth, widthMeasureSpec),
            clampSize(measuredHeight, heightMeasureSpec)
        )
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            val p =
                child.layoutParams as LayoutParams
            @SuppressLint("Range") val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                DEFAULT_DAYS_IN_WEEK * measureTileWidth,
                MeasureSpec.EXACTLY
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                p.height * measureTileHeight,
                MeasureSpec.EXACTLY
            )
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
    }

    private fun getWeekCountBasedOnMode(): Int {
        var weekCount: Int? = calendarMode?.visibleWeeksCount

        val isInMonthsMode = calendarMode == CalendarMode.MONTHS


        if (isInMonthsMode && mDynamicHeightEnabled) {
            adapter?.let {
                pager?.let {
                    val cal: LocalDate? = adapter.getItem(pager.currentItem)?.date
                    val tempLastDay = cal?.withDayOfMonth(cal.lengthOfMonth())

                    weekCount = tempLastDay?.get(WeekFields.of(firstDayOfWeek, 1)?.weekOfMonth())
                }
            }

        }


        val res = weekCount ?: 6

        return if (isShowWeekDays) res + DAY_NAMES_ROW else res
    }

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
        val count = childCount
        val parentLeft = paddingLeft
        val parentWidth = right - left - parentLeft - paddingRight
        var childTop = paddingTop
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility == View.GONE) {
                continue
            }
            val width = child.measuredWidth
            val height = child.measuredHeight
            val delta = (parentWidth - width) / 2
            val childLeft = parentLeft + delta
            child.layout(childLeft, childTop, childLeft + width, childTop + height)
            childTop += height
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return LayoutParams(1)
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
        return LayoutParams(1)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = MaterialCalendarView::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = MaterialCalendarView::class.java.name
    }

    /**
     * Simple layout params for MaterialCalendarView. The only variation for layout is height.
     */
    class LayoutParams
    /**
     * Create a layout that matches parent width, and is X number of tiles high
     *
     * @param tileHeight view height in number of tiles
     */
        (tileHeight: Int) :
        MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileHeight)

    /**
     * Enable or disable the ability to swipe between months.
     *
     * @param pagingEnabled pass false to disable paging, true to enable (default)
     */
    fun setPagingEnabled(pagingEnabled: Boolean) {
        pager!!.isPagingEnabled = pagingEnabled
        updateUi()
    }

    /**
     * @return true if swiping months is enabled, false if disabled. Default is true.
     */
    fun isPagingEnabled(): Boolean {
        return pager!!.isPagingEnabled
    }

    /**
     * Preserve the current parameters of the Material Calendar View.
     */
    fun state(): State? {
        return state
    }

    /**
     * Initialize the parameters from scratch.
     */
    fun newState(): StateBuilder {
        return StateBuilder()
    }

    fun commit(state: State) {
        // Use the calendarDayToShow to determine which date to focus on for the case of switching between month and week views
        var calendarDayToShow: CalendarDay? = null
        if (adapter != null && state.cacheCurrentPosition) {
            calendarDayToShow = adapter.getItem(pager!!.currentItem)
            if (calendarMode !== state.calendarMode) {
                val currentlySelectedDate = selectedDate
                if (calendarMode === CalendarMode.MONTHS && currentlySelectedDate != null) {
                    // Going from months to weeks
                    val lastVisibleCalendar = calendarDayToShow!!.date
                    val lastVisibleCalendarDay =
                        from(lastVisibleCalendar.plusDays(1))
                    if (currentlySelectedDate == calendarDayToShow ||
                        currentlySelectedDate.isAfter(calendarDayToShow) && currentlySelectedDate.isBefore(
                            lastVisibleCalendarDay!!
                        )
                    ) {
                        // Currently selected date is within view, so center on that
                        calendarDayToShow = currentlySelectedDate
                    }
                } else if (calendarMode === CalendarMode.WEEKS) {
                    // Going from weeks to months
                    val lastVisibleCalendar = calendarDayToShow!!.date
                    val lastVisibleCalendarDay =
                        from(lastVisibleCalendar.plusDays(6))
                    calendarDayToShow = if (currentlySelectedDate != null &&
                        (currentlySelectedDate == calendarDayToShow || currentlySelectedDate == lastVisibleCalendarDay ||
                                (currentlySelectedDate.isAfter(calendarDayToShow)
                                        && currentlySelectedDate.isBefore(lastVisibleCalendarDay!!)))
                    ) {
                        // Currently selected date is within view, so center on that
                        currentlySelectedDate
                    } else {
                        lastVisibleCalendarDay
                    }
                }
            }
        }
        this.state = state
        saveStateParameters(state)

        recreateAdapter()

        adapter.isShowWeekDays = isShowWeekDays
        pager?.adapter = adapter

        setRangeDates(minDate, maxDate)


        // Reset height params after mode change
        val tileHeight =
            if (isShowWeekDays) (calendarMode?.visibleWeeksCount
                ?: defaultVisibleWeeksCount) + DAY_NAMES_ROW else calendarMode?.visibleWeeksCount
                ?: defaultVisibleWeeksCount

        pager?.layoutParams = LayoutParams(tileHeight)

        setCurrentDate(
            if (selectionMode == SELECTION_MODE_SINGLE && adapter.getSelectedDates()
                    .isNotEmpty()
            ) adapter.getSelectedDates()[0] else today
        )
        if (calendarDayToShow != null) {
            pager?.currentItem = adapter.getIndexForDay(calendarDayToShow)
        }
        invalidateDecorators()
        updateUi()
    }

    private fun saveStateParameters(state: State) {
        // Save states parameters
        calendarMode = state.calendarMode
        firstDayOfWeek = state.firstDayOfWeek
        minDate = state.minDate
        maxDate = state.maxDate
        isShowWeekDays = state.showWeekDays
    }

    private fun recreateAdapter() {

        // Recreate adapter
        val newAdapter: CalendarPagerAdapter<*> = when (calendarMode) {
            CalendarMode.MONTHS -> MonthPagerAdapter(this)
            CalendarMode.WEEKS -> WeekPagerAdapter(this)
            else -> MonthPagerAdapter(this)
        }

        adapter = if (!::adapter.isInitialized) {
            newAdapter
        } else {
            adapter.migrateStateAndReturn(newAdapter)
        }
    }

    companion object {
        const val INVALID_TILE_DIMENSION = -10

        /**
         * Selection mode that disallows all selection.
         * When changing to this mode, current selection will be cleared.
         */
        const val SELECTION_MODE_NONE = 0

        /**
         * Selection mode that allows one selected date at one time. This is the default mode.
         * When switching from [.SELECTION_MODE_MULTIPLE], this will select the same date
         * as from [.getSelectedDate], which should be the last selected date
         */
        const val SELECTION_MODE_SINGLE = 1

        /**
         * Selection mode which allows more than one selected date at one time.
         */
        const val SELECTION_MODE_MULTIPLE = 2

        /**
         * Selection mode which allows selection of a range between two dates
         */
        const val SELECTION_MODE_RANGE = 3

        /**
         * Do not show any non-enabled dates
         */
        const val SHOW_NONE = 0

        /**
         * Show dates from the proceeding and successive months, in a disabled state.
         * This flag also enables the [.SHOW_OUT_OF_RANGE] flag to prevent odd blank areas.
         */
        const val SHOW_OTHER_MONTHS = 1

        /**
         * Show dates that are outside of the min-max range.
         * This will only show days from the current month unless [.SHOW_OTHER_MONTHS] is enabled.
         */
        const val SHOW_OUT_OF_RANGE = 1 shl 1

        /**
         * Show days that are individually disabled with decorators.
         * This will only show dates in the current month and inside the minimum and maximum date range.
         */
        const val SHOW_DECORATED_DISABLED = 1 shl 2

        /**
         * The default flags for showing non-enabled dates. Currently only shows [ ][.SHOW_DECORATED_DISABLED]
         */
        const val SHOW_DEFAULTS = SHOW_DECORATED_DISABLED

        /**
         * Show all the days
         */
        const val SHOW_ALL =
            SHOW_OTHER_MONTHS or SHOW_OUT_OF_RANGE or SHOW_DECORATED_DISABLED

        /**
         * Use this orientation to animate the title vertically
         */
        const val VERTICAL = 0

        /**
         * Use this orientation to animate the title horizontally
         */
        const val HORIZONTAL = 1

        /**
         * Default tile size in DIPs. This is used in cases where there is no tile size specificed and the
         * view is set to [WRAP_CONTENT][ViewGroup.LayoutParams.WRAP_CONTENT]
         */
        const val DEFAULT_TILE_SIZE_DP = 44
        private const val DEFAULT_DAYS_IN_WEEK = 7
        private const val DEFAULT_MAX_WEEKS = 6
        private const val DAY_NAMES_ROW = 1
        private fun getThemeAccentColor(context: Context): Int {
            val colorAttr: Int
            colorAttr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                android.R.attr.colorAccent
            } else {
                //Get colorAccent defined for AppCompat
                context.resources
                    .getIdentifier("colorAccent", "attr", context.packageName)
            }
            val outValue = TypedValue()
            context.theme.resolveAttribute(colorAttr, outValue, true)
            return outValue.data
        }
        /*
     * Show Other Dates Utils
     */
        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the other months flag is set
         */
        fun showOtherMonths(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_OTHER_MONTHS != 0
        }

        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the out of range flag is set
         */
        fun showOutOfRange(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_OUT_OF_RANGE != 0
        }

        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the decorated disabled flag is set
         */
        fun showDecoratedDisabled(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_DECORATED_DISABLED != 0
        }

        /**
         * Clamp the size to the measure spec.
         *
         * @param size Size we want to be
         * @param spec Measure spec to clamp against
         * @return the appropriate size to pass to [View.setMeasuredDimension]
         */
        private fun clampSize(size: Int, spec: Int): Int {
            val specMode = MeasureSpec.getMode(spec)
            val specSize = MeasureSpec.getSize(spec)
            return when (specMode) {
                MeasureSpec.EXACTLY -> {
                    specSize
                }
                MeasureSpec.AT_MOST -> {
                    Math.min(size, specSize)
                }
                MeasureSpec.UNSPECIFIED -> {
                    size
                }
                else -> {
                    size
                }
            }
        }

        /**
         * Used for enabling or disabling views, while also changing the alpha.
         *
         * @param view   The view to enable or disable.
         * @param enable Whether to enable or disable the view.
         */
        private fun enableView(view: View?, enable: Boolean) {
            view?.isEnabled = enable
            view?.alpha = if (enable) 1f else 0.1f
        }
    }

    init {
        if (isInEditMode) {
            AndroidThreeTen.init(context)
        }
        compatActions()

        recreateAdapter()

        initializeControls()

        initializePager()

        // Adapter is created while parsing the TypedArray attrs, so setup has to happen after
        setupChildren()

        currentMonth = today
        setCurrentDate(currentMonth)

        removeView(pager)

        val monthView = MonthView(this, currentMonth, firstDayOfWeek, true)

        monthView.selectionColor = selectionColor
        monthView.setDateTextAppearance(adapter.getDateTextAppearance())
        monthView.setWeekDayTextAppearance(adapter.getWeekDayTextAppearance())
        monthView.showOtherDates = getShowOtherDates()
        addView(
            monthView,
            LayoutParams(
                calendarMode?.visibleWeeksCount ?: defaultVisibleWeeksCount + DAY_NAMES_ROW
            )
        )

    }

    private fun initializeStyleProperties(
        context: Context,
        attrs: AttributeSet?
    ) {
        val a = context.theme
            .obtainStyledAttributes(attrs, R.styleable.MaterialCalendarView, 0, 0)
        try {
            val calendarModeIndex = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_calendarMode,
                0
            )

            firstDayOfWeekInt = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_firstDayOfWeek,
                -1
            )

            titleChanger.orientation = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_titleAnimationOrientation,
                VERTICAL
            )

            isShowWeekDays = a.getBoolean(R.styleable.MaterialCalendarView_mcv_showWeekDays, true)

            newState()
                .setFirstDayOfWeek(firstDayOfWeek)
                .setCalendarDisplayMode(CalendarMode.values()[calendarModeIndex])
                .setShowWeekDays(isShowWeekDays)
                .commit(this)

            selectionMode = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_selectionMode,
                SELECTION_MODE_SINGLE
            )

            tileSize = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileSize,
                INVALID_TILE_DIMENSION
            )

            tileWidth = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileWidth,
                INVALID_TILE_DIMENSION
            )

            tileHeight = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileHeight,
                INVALID_TILE_DIMENSION
            )

            leftArrowRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_leftArrow,
                R.drawable.mcv_action_previous
            )

            rightArrowRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_rightArrow,
                R.drawable.mcv_action_next
            )

            selectionColor = a.getColor(
                R.styleable.MaterialCalendarView_mcv_selectionColor,
                getThemeAccentColor(context)
            )
            var array =
                a.getTextArray(R.styleable.MaterialCalendarView_mcv_weekDayLabels)
            if (array != null) {
                setWeekDayFormatter(ArrayWeekDayFormatter(array))
            }
            array = a.getTextArray(R.styleable.MaterialCalendarView_mcv_monthLabels)
            if (array != null) {
                setTitleFormatter(MonthArrayTitleFormatter(array))
            }

            setHeaderTextAppearance(
                a.getResourceId(
                    R.styleable.MaterialCalendarView_mcv_headerTextAppearance,
                    R.style.TextAppearance_MaterialCalendarWidget_Header
                )
            )
            setWeekDayTextAppearance(
                a.getResourceId(
                    R.styleable.MaterialCalendarView_mcv_weekDayTextAppearance,
                    R.style.TextAppearance_MaterialCalendarWidget_WeekDay
                )
            )
            setDateTextAppearance(
                a.getResourceId(
                    R.styleable.MaterialCalendarView_mcv_dateTextAppearance,
                    R.style.TextAppearance_MaterialCalendarWidget_Date
                )
            )
            setShowOtherDates(
                a.getInteger(
                    R.styleable.MaterialCalendarView_mcv_showOtherDates,
                    SHOW_DEFAULTS
                )
            )
            setAllowClickDaysOutsideCurrentMonth(
                a.getBoolean(
                    R.styleable.MaterialCalendarView_mcv_allowClickDaysOutsideCurrentMonth,
                    true
                )
            )
            setTextColor(
                a.getResourceId(
                    R.styleable.MaterialCalendarView_dateTextColor,
                    R.color.mcv_text_date_light
                )
            )

            showTopBar = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_showTopBar,
                true
            )

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            a.recycle()
        }
    }

    private fun initializePager() {

        pager.addOnPageChangeListener(pageChangeListener)
        pager.setPageTransformer(false) { page, position ->
            var pos = position
            pos = sqrt(1 - abs(pos).toDouble()).toFloat()
            page.alpha = pos
        }
    }

    private fun compatActions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //If we're on good Android versions, turn off clipping for cool effects
            clipToPadding = false
            clipChildren = false
        } else {
            //Old Android does not like _not_ clipping view pagers, we need to clip
            clipChildren = true
            clipToPadding = true
        }
    }
}