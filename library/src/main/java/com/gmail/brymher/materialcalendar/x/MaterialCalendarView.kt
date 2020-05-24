/*
package com.gmail.brymher.materialcalendar.x

import android.util.Log
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.*
import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import com.gmail.brymher.materialcalendar.format.*
import com.gmail.brymher.materialcalendar.variables.nullableSingleInit
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.*
import org.threeten.bp.temporal.WeekFields
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.reflect.KProperty

class MaterialCalendarView : ViewGroup {

    var TAG = "MaterialCalendarV"

    */
/**
     * [IntDef] annotation for selection mode.
     *
     * @see .setSelectionMode
     * @see .getSelectionMode
     *//*

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        SELECTION_MODE_NONE,
        SELECTION_MODE_SINGLE,
        SELECTION_MODE_MULTIPLE,
        SELECTION_MODE_RANGE
    )
    annotation class SelectionMode {}

    */
/**
     * [IntDef] annotation for showOtherDates.
     *
     * @see .setShowOtherDates
     * @see .getShowOtherDates
     *//*

    @SuppressLint("UniqueConstants")
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        flag = true,
        value = [SHOW_NONE, SHOW_ALL, SHOW_DEFAULTS, SHOW_OUT_OF_RANGE, SHOW_OTHER_MONTHS, SHOW_DECORATED_DISABLED]
    )
    annotation class ShowOtherDates {}

    var calendarMode: CalendarMode? = State.CALENDAR_MODE

    // this is wrong month should be month and date should be date
    var currentMonth by nullableSingleInit {
        CalendarDay.today
    }

    var currentDate
        get() = adapter?.getItem(pager.currentItem);
        */
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
         *//*

        set(day) {
            setCurrentDate(day, true)
        }


    var adapter: CalendarPagerAdapter<*>? = null

    var showWeekDays = true

    val weekCountBasedOnMode: Int
        get() {
            var weekCount = calendarMode?.visibleWeeksCount ?: 6
            val isInMonthsMode = calendarMode == CalendarMode.MONTHS
            if (isInMonthsMode && dynamicHeightEnabled) {

                adapter?.let {
                    val cal = it.getItem(pager.currentItem)?.date
                    val tempLastDay = cal?.withDayOfMonth(cal.lengthOfMonth())
                    weekCount =
                        tempLastDay?.get(WeekFields.of(firstDayOfWeek, 1).weekOfMonth()) ?: 6
                }

            }
            return if (showWeekDays) weekCount + DAY_NAMES_ROW else weekCount
        }
    var firstDayOfWeek = DayOfWeek.SUNDAY

    var tileSize by Size(40)

    var tileWidth by Size(40)

    var tileHeight: Int by Size(40)

    var calendarModeIndex = DEFAULT_CALENDAR_MODE_INDEX

    var firstDayOfWeekInt = DEFAULT_FIRST_DAY_WEEK_INT

    var selectionColor = getThemeAccentColor(context)

    var weekDayFormatter = WeekDayFormatter.DEFAULT
        set(value) {
            field = value
            adapter?.weekDayFormatter = value
        }

    */
/**
     * Set a formatter for day labels.
     *
     * @param formatter the new formatter, null for default
     *//*

    var dayFormatter: DayFormatter = DayFormatter.DEFAULT
        set(value) {
            adapter?.setDayFormatter(value)
        }

    var titleFormatter: MonthArrayTitleFormatter? = null
        set(value) {
            field = value
            titleChanger.titleFormatter = value
            adapter?.titleFormatter = value
            updateUi()
        }

    private var monthListener: OnMonthChangedListener? = null

    private var onDateLongClickListener: OnDateLongClickListener? = null

    private val onDateSelectedListener: OnDateSelectedListener? = null

    private var rangeListener: OnRangeSelectedListener? = null

    var calendarContentDescription: CharSequence? = null

    */
/**
     * Change the title animation orientation to have a different look and feel.
     *
     * @param orientation [MaterialCalendarView.VERTICAL] or [                    ][MaterialCalendarView.HORIZONTAL]
     *//*

    var titleAnimationOrientation = VERTICAL
        set(orientation) {
            field = orientation
            titleChanger.orientation = orientation
        }

    */
/**
     * Enable or disable the ability to swipe between months.
     *
     * @param pagingEnabled pass false to disable paging, true to enable (default)
     *//*

    var pagingEnabled
        get() = pager.isPagingEnabled
        set(value) {
            pager.isPagingEnabled = value
            updateUi()
        }

    private val dayViewDecorators = mutableListOf<DayViewDecorator>()

    */
/**
     * By default, the calendar will take up all the space needed to show any month (6 rows).
     * By enabling dynamic height, the view will change height dependant on the visible month.
     *
     *
     * This means months that only need 5 or 4 rows to show the entire month will only take up
     * that many rows, and will grow and shrink as necessary.
     *
     * @param useDynamicHeight true to have the view different heights based on the visible month
     *//*

    var dynamicHeightEnabled = false


    private var minDate: CalendarDay? = null
    private var maxDate: CalendarDay? = null

    var state: State? = null

    var headerTextAppearance = R.style.TextAppearance_MaterialCalendarWidget_Header
        set(resId) {
            field = resId
            title?.setTextAppearanceCompat(resId)
        }
    var showTopBar = true

    var weekDayTextAppearance = R.style.TextAppearance_MaterialCalendarWidget_WeekDay
        set(resId) {
            field = resId
            adapter?.setWeekDayTextAppearance(resId);
        }

    var dateTextAppearance = R.style.TextAppearance_MaterialCalendarWidget_Date
        set(resId) {
            field = resId
            adapter?.setDateTextAppearance(resId);
        }


    var showOtherDates = SHOW_DEFAULTS
        set(value) {
            field = value
            adapter?.setShowOtherDates(value);
        }

    val canGoBack: Boolean
        get() = pager.currentItem > 0;

    val canGoForward: Boolean
        get() = pager.currentItem < ((adapter?.count ?: 0) - 1)


    var allowClickDaysOutsideCurrentMonth = true

    var textColor = R.color.mcv_text_date_dark

    var arrowLeftRes: Int = R.drawable.mcv_action_previous
        set(@DrawableRes resId) {
            field = resId
            buttonPast?.setImageResource(resId)
        }

    var arrowRightRes = R.drawable.mcv_action_next
        set(@DrawableRes resId) {
            field = resId
            buttonFuture?.setImageResource(resId)
        }

    var leftArrow: Drawable?
        get() = buttonPast?.drawable
        */
/**
         * @param icon the new icon to use for the left paging arrow
         *//*

        set(drawable) {
            buttonPast?.setImageDrawable(drawable)
        }

    var rightArrow: Drawable? = null
        set(drawable) {
            buttonFuture?.setImageDrawable(drawable)
        }


    val stateBuilder
        get() = StateBuilder()

    private val yearUpdateDifference: Int
        get() {
            var updateDifference = 12

            if (calendarMode === CalendarMode.WEEKS) updateDifference = 52

            return updateDifference
        }

    var selectedDates
        get() = adapter?.getSelectedDates()
        set(dates) {
            // TODO adapter?.setSelectedDates(*dates)
        }

    private var topBarVisible
        get() = topbar?.visibility == View.VISIBLE
        set(value) {
            topbar?.visibility = if (value) View.VISIBLE else View.GONE
        }

    */
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
     *//*

    var selectedDate: CalendarDay?
        get() {
            return adapter?.getSelectedDates()?.let { dates ->
                return if (dates.isEmpty()) {
                    null
                } else {
                    dates[dates.size - 1]
                }
            }
        }
        set(value) {
            clearSelection()
            if (value != null) {
                setSelectedDate(value, true)
            }
        }

    private var selectionMode = SELECTION_MODE_SINGLE
        set(@SelectionMode mode) {
            @MaterialCalendarView.SelectionMode val oldMode = selectionMode
            field = mode
            when (mode) {
                SELECTION_MODE_RANGE -> clearSelection()
                SELECTION_MODE_MULTIPLE -> {
                }
                SELECTION_MODE_SINGLE -> if (oldMode == SELECTION_MODE_MULTIPLE || oldMode == SELECTION_MODE_RANGE) {
                    //We should only have one selection now, so we should pick one
                    selectedDates?.let { dates ->
                        if (dates.isNotEmpty()) {
                            // DO NOT CHANGE THIS LOOKS REDUNDANT BUT IS OKAY
                            selectedDate = selectedDate
                        }
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

            adapter?.setSelectionEnabled(selectionMode != SELECTION_MODE_NONE)
        }

    val inflater get() = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val content: View = inflater.inflate(R.layout.calendar_view, null, false)

    val title by nullableSingleInit {
        content.findViewById<TextView>(R.id.month_name)
    }

    val buttonPast by nullableSingleInit {
        content.findViewById<ImageView>(R.id.previous)
    }

    val buttonFuture by nullableSingleInit {
        content.findViewById<ImageView>(R.id.next)
    }

    val btnNextYear by nullableSingleInit {
        content.findViewById<ImageButton>(R.id.next_year)
    }

    val btnPrevYear by nullableSingleInit {
        content.findViewById<ImageButton>(R.id.prev_year)
    }

    val topbar by nullableSingleInit {
        content.findViewById<LinearLayout>(R.id.header)
    }

    init {
        Log.d(TAG, "{@link MaterialCalendarView\$init}")
    }

    // TODO this might be nullified on fragment close and open restore id not okay for lazyProperties
    val pager: CalendarPager by lazy {
        CalendarPager(context)
    }

    val titleChanger: TitleChanger by lazy {
        TitleChanger(title)
    }

    private val onClickListener = OnClickListener { v ->
        if (v === buttonFuture) {
            nextDay(true)
        } else if (v === buttonPast) {
            previousDay(true)
        } else if (v === btnNextYear) {
            nextYear(true)
        } else if (v === btnPrevYear) previousYear(true)
    }

    private val pageChangeListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            titleChanger.previousMonth = currentMonth
            currentMonth = adapter?.getItem(position)
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

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }

    constructor (context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context, attrs, defStyleAttr)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor (
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initialize(context, attrs, defStyleAttr, defStyleRes)
    }

    fun initialize(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
    ) {
        Log.d(TAG, "{@link MaterialCalendarView\$init}")
        if (isInEditMode) {
            AndroidThreeTen.init(context)
        }

        initCompat()

        attrs?.let {
            initStyleAttributes(context, attrs)
        }

        stateBuilder
            .setFirstDayOfWeek(firstDayOfWeek)
            .setCalendarDisplayMode(CalendarMode.values()[calendarModeIndex])
            .setShowWeekDays(showWeekDays)
            .commit(this)

        initEventListeners()
        initPager()
        // Adapter is created while parsing the TypedArray attrs, so setup has to happen after
        setupChildren()
        currentDate = currentMonth

        if (isInEditMode) {
            removeView(pager)

            val monthView = MonthView(this, currentMonth, firstDayOfWeek, true)
            monthView.selectionColor = selectionColor
            monthView.setDateTextAppearance(adapter?.getDateTextAppearance())
            monthView.setWeekDayTextAppearance(adapter?.getWeekDayTextAppearance())
            monthView.showOtherDates = showOtherDates

            calendarMode?.let {
                addView(
                    monthView,
                    LayoutParams(it.visibleWeeksCount + DAY_NAMES_ROW)
                )
            }

        }

    }


    fun initCompat() {
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

    fun initStyleAttributes(context: Context, attrs: AttributeSet) {

        val a = context.theme
            .obtainStyledAttributes(attrs, R.styleable.MaterialCalendarView, 0, 0)
        try {
            calendarModeIndex = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_calendarMode,
                calendarModeIndex
            )

            firstDayOfWeekInt = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_firstDayOfWeek,
                firstDayOfWeekInt
            )

            titleChanger.orientation = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_titleAnimationOrientation,
                MaterialCalendarView.VERTICAL
            )

            firstDayOfWeek = if (firstDayOfWeekInt in 1..7) DayOfWeek.of(firstDayOfWeekInt)
            else WeekFields.of(Locale.getDefault()).firstDayOfWeek

            showWeekDays = a.getBoolean(R.styleable.MaterialCalendarView_mcv_showWeekDays, true)

            selectionMode = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_selectionMode,
                selectionMode
            )

            tileSize = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileSize,
                tileSize
            )

            tileWidth = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileWidth,
                tileWidth
            )

            tileHeight = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileHeight,
                tileHeight
            )

            arrowLeftRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_leftArrow,
                arrowLeftRes
            )

            arrowRightRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_rightArrow,
                arrowRightRes
            )

            selectionColor = a.getColor(
                R.styleable.MaterialCalendarView_mcv_selectionColor,
                selectionColor
            )


            a.getTextArray(R.styleable.MaterialCalendarView_mcv_weekDayLabels)?.let {
                weekDayFormatter = ArrayWeekDayFormatter(it)
            }

            a.getTextArray(R.styleable.MaterialCalendarView_mcv_monthLabels)?.let {
                titleFormatter = MonthArrayTitleFormatter(it)
            }

            headerTextAppearance = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_headerTextAppearance,
                headerTextAppearance
            )

            weekDayTextAppearance = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_weekDayTextAppearance,
                weekDayTextAppearance
            )

            dateTextAppearance = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_dateTextAppearance,
                dateTextAppearance
            )

            showOtherDates = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_showOtherDates,
                showOtherDates
            )

            allowClickDaysOutsideCurrentMonth = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_allowClickDaysOutsideCurrentMonth,
                allowClickDaysOutsideCurrentMonth
            )

            textColor = a.getResourceId(
                R.styleable.MaterialCalendarView_dateTextColor,
                textColor
            )

            showTopBar = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_showTopBar,
                showTopBar
            )

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            a.recycle()
        }
    }

    fun initEventListeners() {
        buttonPast?.setOnClickListener(onClickListener)
        buttonFuture?.setOnClickListener(onClickListener)
        btnNextYear?.setOnClickListener(onClickListener)
        btnPrevYear?.setOnClickListener(onClickListener)
    }

    fun initPager() {
        pager.addOnPageChangeListener(pageChangeListener)
        pager.setPageTransformer(
            false
        ) { page, position ->
            var pos = position
            pos =
                sqrt(1 - abs(pos).toDouble()).toFloat()
            page.alpha = pos
        }
    }


    private fun setupChildren() {
        addView(topbar)
        pager.id = R.id.mcv_pager
        pager.offscreenPageLimit = 1

        calendarMode?.let {
            tileHeight =
                if (showWeekDays) it.visibleWeeksCount + DAY_NAMES_ROW else it.visibleWeeksCount

            addView(pager, LayoutParams(tileHeight))
        }

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


    fun recreateAdapter() {

        // Recreate adapter
        val newAdapter: CalendarPagerAdapter<*> = when (calendarMode) {
            // TODO do not change this this
            CalendarMode.MONTHS -> MonthPagerAdapter(this)
            CalendarMode.WEEKS -> WeekPagerAdapter(this)
            else -> throw IllegalArgumentException("Provided display mode which is not yet implemented")
        }





        adapter = if (adapter == null) newAdapter
        else adapter?.migrateStateAndReturn(newAdapter)

        adapter?.showWeekDays = showWeekDays
    }

    private fun updateUi() {
        titleChanger.change(currentMonth)
        enableView(buttonPast, canGoBack)
        enableView(buttonFuture, canGoForward)
    }

    */
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
     *//*

    fun setCurrentDate(calendar: LocalDate?) {
        setCurrentDate(from(calendar))
    }

    */
/**
     * Clear the currently selected date(s)
     *//*

    fun clearSelection() {

        selectedDates?.let { dates ->

            adapter?.clearSelections()

            for (day in dates) {
                dispatchOnDateSelected(day, false)
            }
        }

    }

    */
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
     *//*

    fun setCurrentDate(day: CalendarDay?, useSmoothScroll: Boolean = true) {
        if (day != null) {
            adapter?.let {
                pager.setCurrentItem(it.getIndexForDay(day), useSmoothScroll)
                updateUi()
            }
        }
    }

    */
/**
     * @param date a Date set to a day to select. Null to clear selection
     *//*

    fun setSelectedDate(date: LocalDate?) {
        selectedDate = from(date)
    }

    */
/**
     * @param day      a CalendarDay to change. Passing null does nothing
     * @param selected true if day should be selected, false to deselect
     *//*

    fun setSelectedDate(day: CalendarDay?, selected: Boolean) {
        day?.let {
            adapter?.setSelectedDate(it, selected)
        }

    }

    */
/**
     * Dispatch date change events to a listener, if set
     *
     * @param day first day of the new month
     *//*

    protected fun dispatchOnMonthChanged(day: CalendarDay?) {
        monthListener?.onMonthChanged(this, day)
    }

    */
/**
     * Call by [CalendarPagerView] to indicate that a day was long clicked and we should handle
     * it
     *//*

    fun onDateLongClicked(dayView: DayView) {
        dayView.date?.let {
            onDateLongClickListener?.onDateLongClick(this, it)
        }

    }

    */
/**
     * Called by the adapter for cases when changes in state result in dates being unselected
     *
     * @param date date that should be de-selected
     *//*

    fun onDateUnselected(date: CalendarDay?) {
        date?.let {
            dispatchOnDateSelected(date, false)
        }
    }

    */
/**
     * Dispatch date change events to a listener, if set
     *
     * @param day      the day that was selected
     * @param selected true if the day is now currently selected, false otherwise
     *//*

    protected fun dispatchOnDateSelected(day: CalendarDay, selected: Boolean) {
        onDateSelectedListener?.onDateSelected(this, day, selected)
    }

    */
/**
     * Dispatch a range of days to a range listener, if set, ordered chronologically.
     *
     * @param days Enclosing days ordered from first to last day.
     *//*

    protected fun dispatchOnRangeSelected(days: List<CalendarDay?>?) {
        days?.let {
            rangeListener?.onRangeSelected(this, days)
        }
    }


    */
/**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it.
     * This method will always process the click to the selected date.
     *
     * @param date        date of the day that was clicked
     * @param nowSelected true if the date is now selected, false otherwise
     *//*

    protected fun onDateClicked(date: CalendarDay, nowSelected: Boolean) {
        when (selectionMode) {
            SELECTION_MODE_MULTIPLE -> {
                adapter?.setSelectedDate(date, nowSelected)
                dispatchOnDateSelected(date, nowSelected)
            }
            SELECTION_MODE_RANGE -> {
                val currentSelection = selectedDates
                when {
                    currentSelection?.isEmpty() ?: false -> {
                        // Selecting the first date of a range
                        adapter?.setSelectedDate(date, nowSelected)
                        dispatchOnDateSelected(date, nowSelected)
                    }
                    currentSelection?.size == 1 -> {
                        // Selecting the second date of a range
                        val firstDaySelected = currentSelection[0]
                        when {
                            firstDaySelected == date -> {
                                // Right now, we are not supporting a range of one day, so we are removing the day instead.
                                adapter?.setSelectedDate(date, nowSelected)
                                dispatchOnDateSelected(date, nowSelected)
                            }
                            firstDaySelected.isAfter(date) -> {
                                // Selecting a range, dispatching in reverse order...
                                adapter?.selectRange(date, firstDaySelected)
                                dispatchOnRangeSelected(adapter?.getSelectedDates())
                            }
                            else -> {
                                // Selecting a range, dispatching in order...
                                adapter?.selectRange(firstDaySelected, date)
                                dispatchOnRangeSelected(adapter?.getSelectedDates())
                            }
                        }
                    }
                    else -> {
                        // Clearing selection and making a selection of the new date.
                        adapter?.clearSelections()
                        adapter?.setSelectedDate(date, nowSelected)
                        dispatchOnDateSelected(date, nowSelected)
                    }
                }
            }
            SELECTION_MODE_SINGLE -> {
                adapter?.clearSelections()
                adapter?.setSelectedDate(date, true)
                dispatchOnDateSelected(date, true)
            }
            else -> {
                adapter?.clearSelections()
                adapter?.setSelectedDate(date, true)
                dispatchOnDateSelected(date, true)
            }
        }
    }


    */
/**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it
     *//*

    fun onDateClicked(dayView: DayView) {
        val selectedDate = dayView.date
        val currentMonth = currentDate?.month
        val selectedMonth = selectedDate?.month
        if (calendarMode === CalendarMode.MONTHS && allowClickDaysOutsideCurrentMonth
            && currentMonth != selectedMonth
        ) {
            selectedDate?.let { date ->
                if (currentDate?.isAfter(date) == true) {
                    goToPrevious()
                } else if (currentDate?.isBefore(date) == true) {
                    goToNext()
                }
            }

        }
        dayView.date?.let { date ->
            onDateClicked(date, !dayView.isChecked)
        }
    }


    */
/**
     * Go to previous month or week without using the button [.buttonPast]. Should only go to
     * previous if [.canGoBack] is true, meaning it's possible to go to the previous month
     * or week.
     *//*

    fun goToPrevious() {
        if (canGoBack) {
            pager.setCurrentItem(pager.currentItem - 1, true)
        }
    }

    */
/**
     * Go to next month or week without using the button [.buttonFuture]. Should only go to
     * next if [.canGoForward] is enabled, meaning it's possible to go to the next month or
     * week.
     *//*

    fun goToNext() {
        if (canGoForward) {
            pager.setCurrentItem(pager.currentItem + 1, true)
        }
    }

    private fun getThemeAccentColor(context: Context): Int {
        val colorAttr: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.attr.colorAccent
        } else {
            //Get colorAccent defined for AppCompat
            context.resources.getIdentifier("colorAccent", "attr", context.packageName)
        }
        val outValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, outValue, true)
        return outValue.data
    }


    */
/**
     * {@inheritDoc}
     *//*

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
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

    */
/**
     * {@inheritDoc}
     *//*

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(1)
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    */
/**
     * {@inheritDoc}
     *//*

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is MaterialCalendarView.LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): LayoutParams? {
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

    fun commit(state: State) {
        // Use the calendarDayToShow to determine which date to focus on for the case of switching between month and week views
        var calendarDayToShow: CalendarDay? = null
        if (adapter != null && state.cacheCurrentPosition) {
            calendarDayToShow = adapter?.getItem(pager.currentItem)
            if (calendarMode !== state.calendarMode) {
                val currentlySelectedDate: CalendarDay? = selectedDate

                if (calendarMode === CalendarMode.MONTHS && currentlySelectedDate != null) {
                    // Going from months to weeks
                    val lastVisibleCalendar = calendarDayToShow?.date
                    val lastVisibleCalendarDay = from(lastVisibleCalendar?.plusDays(1))
                    if (currentlySelectedDate == calendarDayToShow ||
                        lastVisibleCalendarDay != null && calendarDayToShow != null && currentlySelectedDate.isAfter(
                            calendarDayToShow
                        ) && currentlySelectedDate.isBefore(
                            lastVisibleCalendarDay
                        )
                    ) {
                        // Currently selected date is within view, so center on that
                        calendarDayToShow = currentlySelectedDate
                    }
                } else if (calendarMode === CalendarMode.WEEKS) {
                    // Going from weeks to months
                    val lastVisibleCalendar = calendarDayToShow?.date
                    val lastVisibleCalendarDay =
                        from(lastVisibleCalendar?.plusDays(6))
                    calendarDayToShow = if (
                        currentlySelectedDate != null &&
                        (
                                currentlySelectedDate == calendarDayToShow ||
                                        currentlySelectedDate == lastVisibleCalendarDay ||
                                        (
                                                calendarDayToShow != null &&
                                                        currentlySelectedDate.isAfter(
                                                            calendarDayToShow
                                                        ) &&
                                                        lastVisibleCalendarDay != null &&
                                                        currentlySelectedDate.isBefore(
                                                            lastVisibleCalendarDay
                                                        )
                                                )
                                )
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
        // Save states parameters
        calendarMode = state.calendarMode
        firstDayOfWeek = state.firstDayOfWeek
        minDate = state.minDate
        maxDate = state.maxDate
        showWeekDays = state.showWeekDays

        recreateAdapter()

        pager.adapter = adapter
        setRangeDates(minDate, maxDate)

        // Reset height params after mode change
        calendarMode?.let {
            tileHeight =
                if (showWeekDays) it.visibleWeeksCount + DAY_NAMES_ROW else it.visibleWeeksCount
        }

        pager.layoutParams = LayoutParams(tileHeight)

        var cDate: CalendarDay? = CalendarDay.today

        selectedDates?.let { sDates ->

            if (selectionMode == SELECTION_MODE_SINGLE && sDates.isNotEmpty()) cDate = sDates[0]

        }

        setCurrentDate(cDate)

        calendarDayToShow?.let {
            adapter?.getIndexForDay(calendarDayToShow)?.let { i ->
                pager.currentItem = i
            }

        }

        invalidateDecorators()
        updateUi()
    }

    private fun setRangeDates(min: CalendarDay?, max: CalendarDay?) {
        val c = currentMonth

        adapter?.setRangeDates(min, max)

        currentMonth = c

        currentMonth?.let {
            min?.let { m ->
                currentMonth = if (m.isAfter(it)) m else it
            }
        }

        adapter?.let {
            val position = it.getIndexForDay(c)
            pager.setCurrentItem(position, false)
        }

        updateUi()
    }

    // TODO did not copy this section
    fun setMode(mode: CalendarMode) {
        state?.let {
            it.edit()
                .setCalendarDisplayMode(mode)
                .commit(this)
        }
    }

    */
/**
     * Remove a specific decorator instance. Same rules as [List.remove]
     *
     * @param decorator decorator to remove
     *//*

    fun removeDecorator(decorator: DayViewDecorator?) {
        dayViewDecorators.remove(decorator)
        adapter?.decorators = dayViewDecorators
    }

    */
/**
     * Invalidate decorators after one has changed internally. That is, if a decorator mutates, you
     * should call this method to update the widget.
     *//*

    fun invalidateDecorators() = adapter?.invalidateDecorators()

    */
/**
     * Add a collection of day decorators
     *
     * @param decorators decorators to add
     *//*

    fun addDecorators(decorators: Collection<DayViewDecorator?>?) {

        decorators?.let {
            for (d in decorators) {
                d?.let {
                    dayViewDecorators.add(d)
                }

            }

            adapter?.decorators = dayViewDecorators
        }

    }


    */
/**
     * Add several day decorators
     *
     * @param decorators decorators to add
     *//*

    fun addDecorators(vararg decorators: DayViewDecorator?) {
        addDecorators(mutableListOf(*decorators))
    }

    */
/**
     * Add a day decorator
     *
     * @param decorator decorator to add
     *//*

    fun addDecorator(decorator: DayViewDecorator?) {

        decorator?.let {
            dayViewDecorators.add(decorator)

            adapter?.decorators = dayViewDecorators
        }

    }

    */
/**
     * Remove all decorators
     *//*

    fun removeDecorators() {
        dayViewDecorators.clear()
        adapter?.decorators = dayViewDecorators
    }

    */
/**
     * Sets the listener to be notified upon long clicks on dates.
     *
     * @param longClickListener thing to be notified
     *//*

    fun setOnDateLongClickListener(longClickListener: OnDateLongClickListener) {
        onDateLongClickListener = longClickListener
    }


    */
/**
     * Sets the listener to be notified upon month changes.
     *
     * @param listener thing to be notified
     *//*

    fun setOnMonthChangedListener(listener: OnMonthChangedListener?) {
        monthListener = listener
    }

    */
/**
     * Sets the listener to be notified upon a range has been selected.
     *
     * @param listener thing to be notified
     *//*

    fun setOnRangeSelectedListener(listener: OnRangeSelectedListener?) {
        rangeListener = listener
    }

    */
/**
     * Add listener to the title or null to remove it.
     *
     * @param listener Listener to be notified.
     *//*

    fun setOnTitleClickListener(listener: OnClickListener?) {
        title?.setOnClickListener(listener)
    }


    */
/**
     * {@inheritDoc}
     *//*

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specWidthSize = MeasureSpec.getSize(widthMeasureSpec)
        val specWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val specHeightSize = MeasureSpec.getSize(heightMeasureSpec)
        val specHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        //We need to disregard padding for a while. This will be added back later
        val desiredWidth = specWidthSize - paddingLeft - paddingRight
        val desiredHeight = specHeightSize - paddingTop - paddingBottom
        val weekCount: Int = weekCountBasedOnMode
        val viewTileHeight = if (topBarVisible) weekCount + 1 else weekCount

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
            } else desiredTileWidth

            measureTileHeight = if (tileHeight > 0) {
                //We have a tileHeight set, we should use that
                tileHeight
            } else desiredTileHeight

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
        var measuredWidth = measureTileWidth * DEFAULT_DAYS_IN_WEEK
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
            val p = child.layoutParams as LayoutParams
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

    */
/**
     * Clamp the size to the measure spec.
     *
     * @param size Size we want to be
     * @param spec Measure spec to clamp against
     * @return the appropriate size to pass to [View.setMeasuredDimension]
     *//*

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

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }


    */
/**
     * Set a [TitleFormatter]
     * using the provided month labels
     *
     * @param monthLabels month labels to use
     * @see MonthArrayTitleFormatter
     *
     * @see .setTitleFormatter
     *//*

    fun setTitleMonths(monthLabels: Array<CharSequence?>?) {
        titleFormatter = MonthArrayTitleFormatter(monthLabels)
    }

    */
/**
     * Set a [TitleFormatter]
     * using the provided month labels
     *
     * @param arrayRes String array resource of month labels to use
     * @see MonthArrayTitleFormatter
     *
     * @see .setTitleFormatter
     *//*

    fun setTitleMonths(@ArrayRes arrayRes: Int) {
        setTitleMonths(resources.getTextArray(arrayRes))
    }

    */
/**
     * Set a [WeekDayFormatter]
     * with the provided week day labels
     *
     * @param weekDayLabels Labels to use for the days of the week
     * @see ArrayWeekDayFormatter
     *
     * @see .setWeekDayFormatter
     *//*

    fun setWeekDayLabels(weekDayLabels: Array<CharSequence?>?) {
        weekDayFormatter = ArrayWeekDayFormatter(weekDayLabels)
    }

    */
/**
     * Set a [WeekDayFormatter]
     * with the provided week day labels
     *
     * @param arrayRes String array resource of week day labels
     * @see ArrayWeekDayFormatter
     *
     * @see .setWeekDayFormatter
     *//*

    fun setWeekDayLabels(@ArrayRes arrayRes: Int) {
        setWeekDayLabels(resources.getTextArray(arrayRes))
    }

    companion object {

        @JvmStatic
        val INVALID_TILE_DIMENSION = -10

        */
/**
         * Selection mode that disallows all selection.
         * When changing to this mode, current selection will be cleared.
         *//*

        const val SELECTION_MODE_NONE = 0

        */
/**
         * Selection mode that allows one selected date at one time. This is the default mode.
         * When switching from [.SELECTION_MODE_MULTIPLE], this will select the same date
         * as from [.getSelectedDate], which should be the last selected date
         *//*

        const val SELECTION_MODE_SINGLE = 1

        */
/**
         * Selection mode which allows more than one selected date at one time.
         *//*

        const val SELECTION_MODE_MULTIPLE = 2

        */
/**
         * Selection mode which allows selection of a range between two dates
         *//*

        const val SELECTION_MODE_RANGE = 3


        */
/**
         * Do not show any non-enabled dates
         *//*

        const val SHOW_NONE = 0


        */
/**
         * Show dates from the proceeding and successive months, in a disabled state.
         * This flag also enables the [.SHOW_OUT_OF_RANGE] flag to prevent odd blank areas.
         *//*

        const val SHOW_OTHER_MONTHS = 1

        */
/**
         * Show dates that are outside of the min-max range.
         * This will only show days from the current month unless [.SHOW_OTHER_MONTHS] is enabled.
         *//*

        const val SHOW_OUT_OF_RANGE = 1 shl 1

        */
/**
         * Show days that are individually disabled with decorators.
         * This will only show dates in the current month and inside the minimum and maximum date range.
         *//*

        const val SHOW_DECORATED_DISABLED = 1 shl 2

        */
/**
         * The default flags for showing non-enabled dates. Currently only shows [ ][.SHOW_DECORATED_DISABLED]
         *//*

        const val SHOW_DEFAULTS = SHOW_DECORATED_DISABLED

        */
/**
         * Show all the days
         *//*

        const val SHOW_ALL = SHOW_OTHER_MONTHS or SHOW_OUT_OF_RANGE or SHOW_DECORATED_DISABLED


        */
/**
         * Use this orientation to animate the title vertically
         *//*

        @JvmStatic
        val VERTICAL = 0


        */
/**
         * Use this orientation to animate the title horizontally
         *//*

        @JvmStatic
        val HORIZONTAL = 1

        */
/**
         * Default tile size in DIPs. This is used in cases where there is no tile size specificed and the
         * view is set to [WRAP_CONTENT][ViewGroup.LayoutParams.WRAP_CONTENT]
         *//*

        @JvmStatic
        val DEFAULT_TILE_SIZE_DP = 44

        @JvmStatic
        val DEFAULT_DAYS_IN_WEEK = 7

        @JvmStatic
        val DEFAULT_MAX_WEEKS = 6

        @JvmStatic
        val DAY_NAMES_ROW = 1

        @JvmStatic
        val DEFAULT_CALENDAR_MODE_INDEX = 0

        @JvmStatic
        val DEFAULT_FIRST_DAY_WEEK_INT = -1

        @JvmStatic
        private fun enableView(view: View?, enable: Boolean) {
            view?.isEnabled = enable
            view?.alpha = if (enable) 1f else 0.1f
        }

        */
/*
     * Show Other Dates Utils
     *//*

        */
/**
         * @param showOtherDates int flag for show other dates
         * @return true if the other months flag is set
         *//*

        fun showOtherMonths(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_OTHER_MONTHS != 0
        }

        */
/**
         * @param showOtherDates int flag for show other dates
         * @return true if the out of range flag is set
         *//*

        fun showOutOfRange(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_OUT_OF_RANGE != 0
        }

        */
/**
         * @param showOtherDates int flag for show other dates
         * @return true if the decorated disabled flag is set
         *//*

        fun showDecoratedDisabled(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_DECORATED_DISABLED != 0
        }

    }

    */
/**
     * Simple layout params for MaterialCalendarView. The only variation for layout is height.
     *//*

    */
/**
     * Create a layout that matches parent width, and is X number of tiles high
     *
     * @param tileHeight view height in number of tiles
     *//*

    class LayoutParams(tileHeight: Int) :
        MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileHeight)

    class Size(var value: Int) {


        operator fun setValue(calendarView: Any, property: KProperty<*>, newValue: Int) {
            if (value > INVALID_TILE_DIMENSION) {
                value = newValue
            }
        }

        operator fun getValue(calendarView: MaterialCalendarView, property: KProperty<*>): Int {
            return value
        }


    }

    // counter productive
    fun getMinimumDate() = minDate
    fun getMaximumDate() = maxDate

    override fun onSaveInstanceState(): Parcelable? {
        val ss = SavedState(super.onSaveInstanceState())
        ss.showOtherDates = showOtherDates
        ss.allowClickDaysOutsideCurrentMonth = allowClickDaysOutsideCurrentMonth
        ss.minDate = getMinimumDate()
        ss.maxDate = getMaximumDate()
        selectedDates?.let {
            ss.selectedDates = it
        }
        ss.selectionMode = selectionMode
        ss.topbarVisible = topBarVisible
        ss.dynamicHeightEnabled = dynamicHeightEnabled
        ss.currentMonth = currentMonth
        ss.cacheCurrentPosition = state?.cacheCurrentPosition ?: false
        return ss
    }


    override fun onRestoreInstanceState(parcel: Parcelable) {
        val ss = parcel as SavedState
        super.onRestoreInstanceState(ss.superState)
        state?.apply {
            edit()
                .setMinimumDate(ss.minDate)
                .setMaximumDate(ss.maxDate)
                .isCacheCalendarPositionEnabled(ss.cacheCurrentPosition)
                .commit(this@MaterialCalendarView)
        }
        showOtherDates = ss.showOtherDates
        allowClickDaysOutsideCurrentMonth = ss.allowClickDaysOutsideCurrentMonth
        clearSelection()

        for (calendarDay in ss.selectedDates) {
            setSelectedDate(calendarDay, true)
        }
        topBarVisible = ss.topbarVisible
        selectionMode = ss.selectionMode
        dynamicHeightEnabled = ss.dynamicHeightEnabled
        currentDate = ss.currentMonth
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>) {
        dispatchThawSelfOnly(container)
    }


}*/
