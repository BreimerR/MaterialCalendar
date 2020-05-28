package com.gmail.brymher.materialcalendar

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
import androidx.viewpager.widget.ViewPager
import com.gmail.brymher.materialcalendar.format.*
import com.gmail.brymher.materialcalendar.utils.lateInit
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.WeekFields
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.util.Log
import androidx.core.graphics.drawable.DrawableCompat
import com.gmail.brymher.materialcalendar.DayView.DEFAULT_TEXT_COLOR

@Suppress("MemberVisibilityCanBePrivate")
open class MaterialCalendarView : ViewGroup {

    private val TAG = this::class.simpleName ?: "MaterialCalendar"

    // variables
    var showTopBar = true
    var accentColor = 0
    private val dayViewDecorators = mutableListOf<DayViewDecorator>()

    /**
     * Used for the dynamic calendar height.
     */
    var dynamicHeightEnabled = true

    @Suppress("MemberVisibilityCanBePrivate")
    protected val inflater: LayoutInflater? by lateInit {
        context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected open val content by lateInit {
        inflater?.inflate(R.layout.calendar_view, this, false)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    var selectionColor
        get() = accentColor
        set(value) {
            var color = value
            if (color == 0) {
                if (isInEditMode) {
                    return
                } else color = Color.GRAY
            }

            accentColor = color
            adapter?.setSelectionColor(color)
            invalidate()
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var textColor: Int = DEFAULT_TEXT_COLOR
        set(color) {
            field = color
            title?.setTextColor(color)
            adapter?.dateTextAppearance
        }

    // TODO create pixel int annotation
    // original value was INVALID_TILE_HEIGHT
    @Suppress("MemberVisibilityCanBePrivate")
    var tileHeight: Int = 40
        /**
         * Set the height of each tile that makes up the calendar.
         *
         * @param height the new height for each tile in pixels
         */
        set(value) {
            field = value
            requestLayout()
        }

    var firstDayOfWeekInt: Int = if (isInEditMode) -1 else DayOfWeek.SUNDAY.value

    /**
     * @return true if the week days names are shown
     */
/*    var isShowWeekDays
        get(): Boolean = showWeekDays*/

    var tileHeightDp = dpToPx(tileHeight)
        set(value) {
            field = dpToPx(value)
            tileHeight = field
        }

    var tileWidth: Int = INVALID_TILE_DIMENSION
        /**
         * Set the width of each tile that makes up the calendar.
         *
         * @param width the new width for each tile in pixels
         */
        set(value) {
            field = value
            requestLayout()
        }


    val isMonthMode: Boolean
        get() = calendarMode == CalendarMode.MONTHS

    @Suppress("unused")
    val isWeekMode: Boolean
        get() = calendarMode == CalendarMode.WEEKS

    /**
     * @param tileSizeDp the new size for each tile in dips
     * @see .setTileSize
     */
    var tileSizeDp: Int = dpToPx(tileSize)
        set(value) {
            tileSize = dpToPx(value)
        }

    var tileWidthDp = dpToPx(tileWidth)
        /**
         * @param tileWidthDp the new width for each tile in dips
         * @see #setTileWidth(int)
         */
        set(value) {
            field = dpToPx(value)
            tileWidth = field
        }

    private var _weekDayFormatter: WeekDayFormatter? = null
        set(formatter) {
            field = formatter
            adapter?.setWeekDayFormatter(formatter)
        }

    /**
     * Set a formatter for weekday labels.
     *
     * @param formatter the new formatter, null for default
     */
    private var weekDayFormatter: WeekDayFormatter
        get() = if (_weekDayFormatter == null) WeekDayFormatter.DEFAULT else _weekDayFormatter!!
        set(formatter) {
            _weekDayFormatter = formatter
        }


    /**
     * Whether the pager can page backward, meaning the previous month is enabled.
     *
     * @return true if there is a previous month that can be shown
     */
    private val canGoBack
        get(): Boolean {
            return pager.currentItem > 0
        }


    /**
     * Whether the pager can page forward, meaning the future month is enabled.
     *
     * @return true if there is a future month that can be shown
     */
    private val canGoForward
        get(): Boolean {
            var b = false
            adapter?.let {
                b = pager.currentItem < it.count - 1
            }
            return b
        }


    /**
     * Use {@link #getTileWidth()} or {@link #getTileHeight()} instead. This method is deprecated
     * and will just return the largest of the two sizes.
     *
     * @return tile height or width, whichever is larger
     */
    @Deprecated("Use {@link #tileWidth)} or {@link #tileHeight} instead. This method is deprecated")
    var tileSize
        get() = max(tileHeight, tileWidth)
        /**
         * Set the size of each tile that makes up the calendar.
         * Each day is 1 tile, so the widget is 7 tiles wide and 7 or 8 tiles tall
         * depending on the visibility of the {@link #topbar}.
         *
         * @param size the new size for each tile in pixels
         */
        set(size) {
            if (size > INVALID_TILE_DIMENSION) {
                tileHeight = size
                tileWidth = size
                requestLayout()
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var headerTextAppearance: Int = R.style.TextAppearance_MaterialCalendarWidget_Header
        /**
         * @param value The text appearance resource id.
         */
        set(value) {
            title?.setTextAppearance(context, value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var nextYearDrawableRes = R.drawable.ic_fast_foward_inset
        set(value) {
            nextYearDrawable = context?.getDrawableCompat(value)
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var prevYearDrawableRes = R.drawable.ic_fast_reverse_inset
        set(@DrawableRes value) {
            prevYearDrawable = context?.getDrawableCompat(value)
            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var nextYearDrawable: Drawable? = null
        set(value) {
            value?.setTintCompat(yearIconsColor)
            btnNextYear?.setImageDrawable(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var prevYearDrawable: Drawable? = null
        set(value) {
            value?.setTintCompat(yearIconsColor)
            btnPrevYear?.setImageDrawable(value)
        }

    @DrawableRes
    var yearIconsColor: Int = R.color.mcv_text_date_light
        set(@DrawableRes value) {
            field = value
            // update icons
            @Suppress("SelfAssignment")
            nextYearDrawableRes = nextYearDrawableRes

            @Suppress("SelfAssignment")
            prevYearDrawableRes = prevYearDrawableRes
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var leftArrowRes = R.drawable.mcv_action_previous
        set(value) {
            leftArrow = context?.getDrawableCompat(value)

            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var rightArrowRes = R.drawable.mcv_action_next
        set(value) {
            rightArrow = context?.getDrawableCompat(value)

            field = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var rightArrow: Drawable?
        get() = btnFuture?.drawable
        set(value) {
            value?.setTintCompat(dayIconsColor)
            btnFuture?.setImageDrawable(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var leftArrow: Drawable?
        get() = btnPast?.drawable
        set(value) {
            value?.setTintCompat(dayIconsColor)
            btnPast?.setImageDrawable(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    @DrawableRes
    var dayIconsColor: Int = R.color.mcv_text_date_light
        set(@DrawableRes value) {
            field = value
            // updates dates icons
            leftArrowRes = leftArrowRes
            rightArrowRes = rightArrowRes
        }

    var contentDescriptionArrowPast: CharSequence?
        get() = btnPast?.contentDescription
        set(value) {
            btnPast?.contentDescription = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var dateTextAppearance: Int
        get() = adapter?.dateTextAppearance ?: 0
        /**
         * @param value The text appearance resource id.
         */
        set(value) {
            adapter?.dateTextAppearance = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var weekDayTextAppearance: Int = R.style.TextAppearance_MaterialCalendarWidget_WeekDay
        get() = adapter?.weekDayTextAppearance ?: field
        /**
         * @param value The text appearance resource id.
         */
        set(value) {
            field = value
            adapter?.weekDayTextAppearance = value
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var dayFormatterContentDescription: DayFormatter? = null
        /**
         * Set a formatter for day content description.
         *
         * @param formatter the new formatter, null for default
         */
        set(value) {
            adapter?.setDayFormatterContentDescription(value)
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var selectedDate: CalendarDay?
        /**
         * Get the currently selected date, or null if no selection. Depending on the selection mode,
         * you might get different results.
         *
         * <p>For {@link #SELECTION_MODE_SINGLE}, returns the selected date.</p>
         * <p>For {@link #SELECTION_MODE_MULTIPLE}, returns the last date selected.</p>
         * <p>For {@link #SELECTION_MODE_RANGE}, returns the last date of the range. In most cases, you
         * should probably be using {@link #getSelectedDates()}.</p>
         * <p>For {@link #SELECTION_MODE_NONE}, returns null.</p>
         *
         * @return The selected day, or null if no selection. If in multiple selection mode, this
         * will return the last date of the list of selected dates.
         * @see MaterialCalendarView#getSelectedDates()
         */
        get() {
            val dates: List<CalendarDay>? = adapter?.selectedDates

            return if (dates != null && dates.isNotEmpty()) {
                dates[dates.size - 1]
            } else {
                null
            }
        }
        set(date) {
            clearSelection()
            date?.let {
                setSelectedDate(date, true)
            }
        }

    /**
     * @param day a CalendarDay to change. Passing null does nothing
     * @param selected true if day should be selected, false to deselect
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setSelectedDate(day: CalendarDay?, selected: Boolean) {
        day?.let {
            adapter?.setSelectedDate(it, selected)
        }
    }

    /**
     * Allow the user to click on dates from other months that are not out of range. Go to next or
     * previous month if a day outside the current month is clicked. The day still need to be
     * enabled to be selected.
     * Default value is true. Should be used with [.SHOW_OTHER_MONTHS].
     *
     * @param enabled True to allow the user to click on a day outside current month displayed
     */
    var allowClickDaysOutsideCurrentMonth = true

    private var showWeekDays = true
        set(value) {
            adapter?.showWeekDays = value
            field = value
        }

    var state: State? = null
        set(value) {
            // Save states parameters
            value?.let {
                calendarMode = it.calendarMode
                firstDayOfWeek = it.firstDayOfWeek
                minDate = it.minDate
                maxDate = it.maxDate
                showWeekDays = value.showWeekDays

                field = value
            }

        }


    var adapter: CalendarPagerAdapter<*>? = null
        set(value) {
            field = if (field == null) {
                value
            } else {
                field!!.migrateStateAndReturn(value)
            }

            pager.adapter = field
        }


    /**
     * Get the current [CalendarMode] set of the Calendar.
     *
     * @return Whichever mode the calendar is currently in.
     */
    var calendarMode: CalendarMode? = CalendarMode.MONTHS
        set(value) {
            var tileHeight = INVALID_TILE_DIMENSION
            // Reset height params after mode change
            value?.let {
                tileHeight =
                    if (showWeekDays) it.visibleWeeksCount + CalendarPagerView.DAY_NAMES_ROW else it.visibleWeeksCount
            }

            pager.layoutParams = LayoutParams(tileHeight)

            field = value
        }

    var currentMonth: CalendarDay? = if (isInEditMode) null else CalendarDay.today

    /**
     * @return The first day of the week as a {@linkplain Calendar} day constant.
     */
    var firstDayOfWeek: DayOfWeek? = null
    var minDate: CalendarDay? = null
    var maxDate: CalendarDay? = null

    var listener: OnDateSelectedListener? = null
    var longClickListener: OnDateLongClickListener? = null
    var monthListener: OnMonthChangedListener? = null
    var rangeListener: OnRangeSelectedListener? = null

    @Suppress("PropertyName")
    var _calendarContentDescription: CharSequence? = null


    var calendarContentDescription: CharSequence = context.getString(R.string.calendar)
        /**
         * Get content description for calendar
         *
         * @return calendar's content description
         */
        get() {
            _calendarContentDescription?.let {
                return it
            }
            return field
        }
        /**
         * Set content description for calendar
         *
         * @param description String to use as content description
         */
        set(description) {
            _calendarContentDescription = description
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var visibleWeeksCount: Int by lateInit {
        calendarMode?.visibleWeeksCount ?: DEFAULT_VISIBLE_WEEKS_COUNT
    }


    var contentDescriptionArrowFuture: CharSequence?
        get() = btnFuture?.contentDescription
        /**
         * Set content description for button future
         *
         * @param description String to use as content description
         */
        set(description) {
            btnFuture?.contentDescription = description
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var showOtherDates: Int = SHOW_ALL
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
        get() = adapter?.showOtherDates ?: field
        /**
         * The default value is {@link #SHOW_DEFAULTS}, which currently is just {@link
         * #SHOW_DECORATED_DISABLED}.
         * This means that the default visible days are of the current month, in the min-max range.
         *
         * @param showOtherDates flags for showing non-enabled dates
         * @see #SHOW_ALL
         * @see #SHOW_NONE
         * @see #SHOW_DEFAULTS
         * @see #SHOW_OTHER_MONTHS
         * @see #SHOW_OUT_OF_RANGE
         * @see #SHOW_DECORATED_DISABLED
         */
        set(@ShowOtherDates showOtherDates) {
            field = showOtherDates
            adapter?.showOtherDates = showOtherDates
        }

    var currentDate: CalendarDay? = if (isInEditMode) null else CalendarDay.today
        /**
         * Set the calendar to a specific month or week based on a date.
         * <p>
         * In month mode, the calendar will be set to the corresponding month.
         * <p>
         * In week mode, the calendar will be set to the corresponding week.
         *
         * @param day a CalendarDay to focus the calendar on. Null will do nothing
         */
        set(date) {
            field = date
            setCurrentDate(date, true)
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
    open fun setCurrentDate(calendar: LocalDate?) {
        currentDate = CalendarDay.from(calendar)
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
    fun setCurrentDate(day: CalendarDay?, useSmoothScroll: Boolean = true) {
        day?.let {

            adapter?.let {
                val index = it.getIndexForDay(day)
                pager.setCurrentItem(index, useSmoothScroll)
            }

            updateUi()
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
     * @see MaterialCalendarView.getSelectedDate
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var selectedDates: List<CalendarDay>
        get() = adapter?.selectedDates ?: listOf()
        set(dates) {
            adapter?.selectedDates = dates
        }

    /**
     * Change the selection mode of the calendar. The default mode is {@linkplain
     * #SELECTION_MODE_SINGLE}
     *
     * @param mode the selection mode to change to. This must be one of
     *             {@linkplain #SELECTION_MODE_NONE}, {@linkplain #SELECTION_MODE_SINGLE},
     *             {@linkplain #SELECTION_MODE_RANGE} or {@linkplain #SELECTION_MODE_MULTIPLE}.
     *             Unknown values will act as {@linkplain #SELECTION_MODE_SINGLE}
     * @see #getSelectionMode()
     * @see #SELECTION_MODE_NONE
     * @see #SELECTION_MODE_SINGLE
     * @see #SELECTION_MODE_MULTIPLE
     * @see #SELECTION_MODE_RANGE
     */
    /**
     * Get the current selection mode. The default mode is {@linkplain #SELECTION_MODE_SINGLE}
     *
     * @return the current selection mode
     * @see #setSelectionMode(int)
     * @see #SELECTION_MODE_NONE
     * @see #SELECTION_MODE_SINGLE
     * @see #SELECTION_MODE_MULTIPLE
     * @see #SELECTION_MODE_RANGE
     */
    @SelectionMode
    var selectionMode = SELECTION_MODE_SINGLE
        set(mode) {
            val oldMode = field
            field = mode

            when (mode) {
                SELECTION_MODE_RANGE -> clearSelection()
                SELECTION_MODE_MULTIPLE -> {
                }
                SELECTION_MODE_SINGLE -> if (oldMode == SELECTION_MODE_MULTIPLE || oldMode == SELECTION_MODE_RANGE) {
                    //We should only have one selection now, so we should pick one
                    val dates: List<CalendarDay> = selectedDates
                    if (dates.isNotEmpty()) selectedDates = selectedDates

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

    /**
     * Go to previous month or week without using the button [.buttonPast]. Should only go to
     * previous if [.canGoBack] is true, meaning it's possible to go to the previous month
     * or week.
     */
    open fun goToPrevious() {
        if (canGoBack) {
            pager.setCurrentItem(pager.currentItem - 1, true)
        }
    }


    /**
     * Go to next month or week without using the button [.buttonFuture]. Should only go to
     * next if [.canGoForward] is enabled, meaning it's possible to go to the next month or
     * week.
     */
    open fun goToNext() {
        if (canGoForward) {
            pager.setCurrentItem(pager.currentItem + 1, true)
        }
    }

    val yearUpdateDifference
        get(): Int {
            var updateDifference = 12
            if (calendarMode == CalendarMode.WEEKS) updateDifference = 52
            return updateDifference
        }

    var _titleChanger: TitleChanger? = null

    val titleChanger: TitleChanger?
        get() {
            if (_titleChanger == null)
                _titleChanger = if (!isInEditMode) TitleChanger(title) else null

            return _titleChanger
        }

    // views
    val topbar by lateInit {
        content?.findViewById<LinearLayout>(R.id.header)
    }

    var title by lateInit {
        content?.findViewById<TextView>(R.id.month_name)
    }
    var btnPast by lateInit {
        content?.findViewById<ImageView>(R.id.previous)
    }
    var btnNextYear by lateInit {
        content?.findViewById<ImageButton>(R.id.next_year)
    }
    var btnPrevYear by lateInit {
        content?.findViewById<ImageButton>(R.id.prev_year)
    }
    var btnFuture by lateInit {
        content?.findViewById<ImageView>(R.id.next)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val btnClearSelection by lateInit {
        content?.findViewById<ImageButton>(R.id.action_clear_selection)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val btnToggleMode by lateInit {
        content?.findViewById<ImageButton>(R.id.action_toggle_mode)
    }

    val pager: CalendarPager by lateInit {
        CalendarPager(context)
    }

    // listeners
    private val onClickListener: OnClickListener? =
        OnClickListener { v ->
            when (v) {
                btnFuture -> nextDay(true)

                btnPast -> previousDay(true)

                btnNextYear -> nextYear(true)

                btnPrevYear -> previousYear(true)

                btnClearSelection -> clearSelection()

                btnToggleMode -> toggleCalendarMode()
            }
        }

    private val pageChangeListener: ViewPager.OnPageChangeListener = object :
        ViewPager.OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            titleChanger?.setPreviousMonth(currentMonth)
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


    /**
     * [IntDef] annotation for selection mode.
     *
     * @see .setSelectionMode
     * @see .getSelectionMode
     */
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @IntDef(
        SELECTION_MODE_NONE,
        SELECTION_MODE_SINGLE,
        SELECTION_MODE_MULTIPLE,
        SELECTION_MODE_RANGE
    )
    annotation class SelectionMode {}

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {
        if (isInEditMode) AndroidThreeTen.init(context)

        initCompat()

        attrs?.let(::updateAttrs)

        initControls()

        initPager()

        currentDate = currentMonth

        // Adapter is created while parsing the TypedArray attrs, so setup has to happen after
        setupChildren()

        if (isInEditMode) {
            if (currentMonth != null && firstDayOfWeek != null) {
                removeView(pager)

                val monthView = MonthView(this, currentMonth, firstDayOfWeek, true)
                monthView.setSelectionColor(selectionColor)

                adapter?.let {
                    var dateTextAppearance = 0
                    dateTextAppearance = it.dateTextAppearance
                    var weekDayTextAppearance = 0
                    weekDayTextAppearance = it.weekDayTextAppearance
                    monthView.setDateTextAppearance(dateTextAppearance)
                    monthView.setWeekDayTextAppearance(weekDayTextAppearance)
                }

                monthView.setShowOtherDates(showOtherDates)
                addView(monthView, LayoutParams(visibleWeeksCount + CalendarPagerView.DAY_NAMES_ROW))
            }

        }


    }


    private fun initPager() {
        pager.addOnPageChangeListener(pageChangeListener)

        pager.setPageTransformer(false) { page, pos ->
            page.alpha = sqrt(1 - abs(pos).toDouble()).toFloat()
        }

        pager.id = R.id.mcv_pager
        pager.offscreenPageLimit = 1
    }


    private fun initCompat() {
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

    @Suppress("MemberVisibilityCanBePrivate")
    fun initControls() {
        btnPast?.setOnClickListener(onClickListener)
        btnFuture?.setOnClickListener(onClickListener)
        btnNextYear?.setOnClickListener(onClickListener)
        btnPrevYear?.setOnClickListener(onClickListener)
        btnClearSelection?.setOnClickListener(onClickListener)
        btnToggleMode?.setOnClickListener(onClickListener)
    }

    /** TODO
     * this update ui is misleading and does not update the whole UI
     * as one might expect
     * */
    fun updateUi() {
        titleChanger?.change(currentMonth)
        enableView(btnPast, canGoBack)
        enableView(btnFuture, canGoForward)

    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }


    /**
     * @return the minimum selectable date for the calendar, if any
     */
    open fun getMinimumDate(): CalendarDay? {
        return minDate
    }

    /**
     * @return the maximum selectable date for the calendar, if any
     */
    open fun getMaximumDate(): CalendarDay? {
        return maxDate
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun setupChildren() {
        addView(topbar)

        val tileHeight = if (calendarMode == null) {
            Log.d(TAG, "calendarMode is null defaulting to tileHeight = $tileHeight")
            40
        } else {
            if (showWeekDays) calendarMode!!.visibleWeeksCount + DAY_NAMES_ROW
            else calendarMode!!.visibleWeeksCount
        }

        addView(pager, LayoutParams(tileHeight))

    }

    open fun updateViewPager(position: Int, smoothScroll: Boolean) {
        pager.setCurrentItem(pager.currentItem + position, smoothScroll)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open fun nextDay(smoothScroll: Boolean) {
        updateViewPager(1, smoothScroll)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open fun previousDay(smoothScroll: Boolean) {
        updateViewPager(-1, smoothScroll)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    open fun nextYear(smoothScroll: Boolean) {
        updateViewPager(yearUpdateDifference, smoothScroll)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun previousYear(smoothScroll: Boolean) {
        updateViewPager(-yearUpdateDifference, smoothScroll)
    }

    /**
     * @param date a Date set to a day to select. Null to clear selection
     */
    open fun setSelectedDate(date: LocalDate?) {
        selectedDate = CalendarDay.from(date)
    }

    /**
     * @param icon the new icon to use for the left paging arrow
     */
    fun setLeftArrow(@DrawableRes icon: Int) {
        leftArrowRes = icon
    }


    /**
     * Set a formatter for day labels.
     *
     * @param formatter the new formatter, null for default
     */
    open fun setDayFormatter(formatter: DayFormatter?) {
        adapter?.setDayFormatter(formatter ?: DayFormatter.DEFAULT)
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
    open fun setWeekDayLabels(weekDayLabels: Array<CharSequence?>?) {
        weekDayFormatter = ArrayWeekDayFormatter(weekDayLabels)
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
    open fun setWeekDayLabels(@ArrayRes arrayRes: Int) {
        setWeekDayLabels(resources.getTextArray(arrayRes))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun toggleCalendarMode() {
        calendarMode = when (calendarMode) {
            CalendarMode.MONTHS -> CalendarMode.WEEKS

            CalendarMode.WEEKS -> CalendarMode.MONTHS

            else -> CalendarMode.MONTHS
        }

        newState {
            it.firstDayOfWeek = firstDayOfWeek
            it.setCalendarDisplayMode(calendarMode)
            it.showWeekDays = showWeekDays
        }.commit(this)

    }

    /**
     * @return true if allow click on days outside current month displayed
     */
    open fun allowClickDaysOutsideCurrentMonth(): Boolean {
        return allowClickDaysOutsideCurrentMonth
    }

    /**
     * Set a custom formatter for the month/year title
     *
     * @param titleFormatter new formatter to use, null to use default formatter
     */
    open fun setTitleFormatter(titleFormatter: TitleFormatter?) {
        titleChanger?.setTitleFormatter(titleFormatter)
        adapter?.setTitleFormatter(titleFormatter)
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
    open fun setTitleMonths(monthLabels: Array<CharSequence?>?) {
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
    open fun setTitleMonths(@ArrayRes arrayRes: Int) {
        setTitleMonths(resources.getTextArray(arrayRes))
    }


    /**
     * Change the title animation orientation to have a different look and feel.
     *
     * @param orientation [MaterialCalendarView.VERTICAL] or [                    ][MaterialCalendarView.HORIZONTAL]
     */
    open fun setTitleAnimationOrientation(orientation: Int) {
        titleChanger?.orientation = orientation
    }


    /**
     * Get the orientation of the animation of the title.
     *
     * @return Title animation orientation [MaterialCalendarView.VERTICAL] or [ ][MaterialCalendarView.HORIZONTAL]
     */
    open fun getTitleAnimationOrientation(): Int {
        return titleChanger?.orientation ?: MaterialCalendarView.HORIZONTAL
    }

    /**
     * Sets the visibility [.topbar], which contains
     * the previous month button [.buttonPast], next month button [.buttonFuture],
     * and the month title [.title].
     *
     * @param visible Boolean indicating if the topbar is visible
     */
    open fun setTopbarVisible(visible: Boolean) {
        topbar?.visibility = if (visible) View.VISIBLE else View.GONE
        requestLayout()
    }


    /**
     * @return true if the topbar is visible
     */
    open fun getTopbarVisible(): Boolean {
        return topbar?.visibility == View.VISIBLE
    }


    override fun onSaveInstanceState(): Parcelable? {
        val ss = SavedState(super.onSaveInstanceState())
        ss.showOtherDates = showOtherDates
        ss.allowClickDaysOutsideCurrentMonth = allowClickDaysOutsideCurrentMonth()
        ss.minDate = getMinimumDate()
        ss.maxDate = getMaximumDate()
        ss.selectedDates = selectedDates
        ss.selectionMode = selectionMode
        ss.topbarVisible = getTopbarVisible()
        ss.dynamicHeightEnabled = dynamicHeightEnabled
        ss.currentMonth = currentMonth
        ss.cacheCurrentPosition = state?.cacheCurrentPosition ?: false
        return ss
    }

    /**
     * @param icon the new icon to use for the right paging arrow
     */
    open fun setRightArrow(@DrawableRes icon: Int) {
        rightArrowRes = icon
    }


    /**
     * Pass all touch events to the pager so scrolling works on the edges of the calendar view.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return pager.dispatchTouchEvent(event)
    }

    /**
     * Clear the currently selected date(s)
     */
    open fun clearSelection() {
        adapter?.clearSelections()

        for (day in selectedDates) {
            dispatchOnDateSelected(day, false)
        }
    }

    open fun setRangeDates(min: CalendarDay?, max: CalendarDay?) {
        val c = currentMonth
        adapter?.setRangeDates(min, max)
        currentMonth = c
        min?.let {
            currentMonth = if (c != null && min.isAfter(c)) min else c
        }

        adapter?.getIndexForDay(c)?.let {
            pager.setCurrentItem(it, false)
        }

        updateUi()
    }


    /**
     * Add a collection of day decorators
     *
     * @param decorators decorators to add
     */
    open fun addDecorators(decorators: Collection<DayViewDecorator>?) {
        if (decorators == null) {
            return
        }
        dayViewDecorators.addAll(decorators)
        adapter?.setDecorators(dayViewDecorators)
    }


    /**
     * Add several day decorators
     *
     * @param decorators decorators to add
     */
    open fun addDecorators(vararg decorators: DayViewDecorator) {
        addDecorators(listOf(*decorators))
    }


    /**
     * Add a day decorator
     *
     * @param decorator decorator to add
     */
    open fun addDecorator(decorator: DayViewDecorator?) {
        if (decorator == null) {
            return
        }
        dayViewDecorators.add(decorator)
        adapter?.setDecorators(dayViewDecorators)
    }


    /**
     * Invalidate decorators after one has changed internally. That is, if a decorator mutates, you
     * should call this method to update the widget.
     */
    open fun invalidateDecorators() {
        adapter?.invalidateDecorators()
    }


    /*
     * Listener/Callback Code
     */

    /*
     * Listener/Callback Code
     */
    /**
     * Sets the listener to be notified upon selected date changes.
     *
     * @param listener thing to be notified
     */
    open fun setOnDateChangedListener(listener: OnDateSelectedListener?) {
        this.listener = listener
    }

    /**
     * Sets the listener to be notified upon long clicks on dates.
     *
     * @param longClickListener thing to be notified
     */
    open fun setOnDateLongClickListener(longClickListener: OnDateLongClickListener?) {
        this.longClickListener = longClickListener
    }


    /**
     * Sets the listener to be notified upon month changes.
     *
     * @param listener thing to be notified
     */
    open fun setOnMonthChangedListener(listener: OnMonthChangedListener?) {
        monthListener = listener
    }

    /**
     * Remove a specific decorator instance. Same rules as [List.remove]
     *
     * @param decorator decorator to remove
     */
    open fun removeDecorator(decorator: DayViewDecorator) {
        dayViewDecorators.remove(decorator)
        adapter?.setDecorators(dayViewDecorators)
    }

    /**
     * Sets the listener to be notified upon a range has been selected.
     *
     * @param listener thing to be notified
     */
    open fun setOnRangeSelectedListener(listener: OnRangeSelectedListener?) {
        rangeListener = listener
    }


    /**
     * Add listener to the title or null to remove it.
     *
     * @param listener Listener to be notified.
     */
    open fun setOnTitleClickListener(listener: OnClickListener?) {
        title?.setOnClickListener(listener)
    }

    /**
     * Dispatch date change events to a listener, if set
     *
     * @param day      the day that was selected
     * @param selected true if the day is now currently selected, false otherwise
     */
    protected open fun dispatchOnDateSelected(day: CalendarDay?, selected: Boolean) {
        listener?.onDateSelected(this, day!!, selected)
    }

    /**
     * Dispatch a range of days to a range listener, if set, ordered chronologically.
     *
     * @param days Enclosing days ordered from first to last day.
     */
    protected open fun dispatchOnRangeSelected(days: List<CalendarDay?>) {
        rangeListener?.onRangeSelected(this, days)
    }

    /**
     * Dispatch date change events to a listener, if set
     *
     * @param day first day of the new month
     */
    protected open fun dispatchOnMonthChanged(day: CalendarDay?) {
        monthListener?.onMonthChanged(this, day)
    }


    /**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it.
     * This method will always process the click to the selected date.
     *
     * @param date        date of the day that was clicked
     * @param nowSelected true if the date is now selected, false otherwise
     */
    fun onDateClicked(date: CalendarDay, nowSelected: Boolean) {
        when (selectionMode) {
            SELECTION_MODE_MULTIPLE -> {
                adapter?.setSelectedDate(date, nowSelected)
                dispatchOnDateSelected(date, nowSelected)
            }
            SELECTION_MODE_RANGE -> {
                adapter?.selectedDates?.let { currentSelection ->
                    when (currentSelection.size) {
                        0 -> {
                            // Selecting the first date of a range
                            adapter?.setSelectedDate(date, nowSelected)
                            dispatchOnDateSelected(date, nowSelected)
                        }
                        1 -> {
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
                                    dispatchOnRangeSelected(adapter?.selectedDates ?: listOf())
                                }
                                else -> {
                                    // Selecting a range, dispatching in order...
                                    adapter?.selectRange(firstDaySelected, date)
                                    dispatchOnRangeSelected(adapter?.selectedDates ?: listOf())
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

    /**
     * Call by [CalendarPagerView] to indicate that a day was clicked and we should handle it
     */
    open fun onDateClicked(dayView: DayView) {
        val selectedDate = dayView.date
        val currentMonth = currentDate?.getMonth()
        val selectedMonth = selectedDate.getMonth()
        if (isMonthMode && allowClickDaysOutsideCurrentMonth && currentMonth != selectedMonth) {
            currentDate?.let {
                if (it.isAfter(selectedDate)) {
                    goToPrevious()
                } else if (it.isBefore(selectedDate)) {
                    goToNext()
                }
            }

        }
        onDateClicked(dayView.date, !dayView.isChecked)
    }


    /**
     * Select a fresh range of date including first day and last day.
     *
     * @param firstDay first day of the range to select
     * @param lastDay  last day of the range to select
     */
    open fun selectRange(firstDay: CalendarDay?, lastDay: CalendarDay?) {
        if (firstDay == null || lastDay == null) {
            return
        } else if (firstDay.isAfter(lastDay)) {
            adapter?.selectRange(lastDay, firstDay)
            dispatchOnRangeSelected(adapter?.selectedDates ?: listOf())
        } else {
            adapter?.selectRange(firstDay, lastDay)
            dispatchOnRangeSelected(adapter?.selectedDates ?: listOf())
        }
    }


    /**
     * Call by [CalendarPagerView] to indicate that a day was long clicked and we should handle
     * it
     */
    protected fun onDateLongClicked(dayView: DayView) {
        longClickListener?.onDateLongClick(this, dayView.date)
    }


    /**
     * Called by the adapter for cases when changes in state result in dates being unselected
     *
     * @param date date that should be de-selected
     */
    protected open fun onDateUnselected(date: CalendarDay?) {
        dispatchOnDateSelected(date, false)
    }


    /*
     * Custom ViewGroup Code
     */

    /*
     * Custom ViewGroup Code
     */
    /**
     * {@inheritDoc}
     */
    override fun generateDefaultLayoutParams(): LayoutParams? {
        return LayoutParams(1)
    }

    /**
     * Remove all decorators
     */
    open fun removeDecorators() {
        dayViewDecorators.clear()
        adapter!!.setDecorators(dayViewDecorators)
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
        val weekCount: Int = getWeekCountBasedOnMode()
        val viewTileHeight = if (getTopbarVisible()) weekCount + 1 else weekCount

        //Calculate independent tile sizes for later
        val desiredTileWidth: Int = desiredWidth / CalendarPagerView.DEFAULT_DAYS_IN_WEEK
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
        var measuredWidth: Int = measureTileWidth * CalendarPagerView.DEFAULT_DAYS_IN_WEEK
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
                CalendarPagerView.DEFAULT_DAYS_IN_WEEK * measureTileWidth,
                MeasureSpec.EXACTLY
            )
            val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                p.height * measureTileHeight,
                MeasureSpec.EXACTLY
            )
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
        }
    }


    open fun getWeekCountBasedOnMode(): Int {
        var weekCount = CalendarMode.MONTHS.visibleWeeksCount

        calendarMode?.let {
            weekCount = it.visibleWeeksCount
            val isInMonthsMode = it == CalendarMode.MONTHS
            if (isInMonthsMode && dynamicHeightEnabled && adapter != null) {
                var a = adapter!!
                val cal = adapter!!.getItem(pager.currentItem).date
                val tempLastDay = cal.withDayOfMonth(cal.lengthOfMonth())
                weekCount = tempLastDay[WeekFields.of(firstDayOfWeek, 1).weekOfMonth()]
            }
        }

        return if (showWeekDays) weekCount + CalendarPagerView.DAY_NAMES_ROW else weekCount
    }


    /**
     * Clamp the size to the measure spec.
     *
     * @param size Size we want to be
     * @param spec Measure spec to clamp against
     * @return the appropriate size to pass to [View.setMeasuredDimension]
     */
    open fun clampSize(size: Int, spec: Int): Int {
        val specMode = MeasureSpec.getMode(spec)
        val specSize = MeasureSpec.getSize(spec)
        return when (specMode) {
            MeasureSpec.EXACTLY -> {
                specSize
            }
            MeasureSpec.AT_MOST -> {
                min(size, specSize)
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
     * {@inheritDoc}
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
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
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
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
     * Enable or disable the ability to swipe between months.
     *
     * @param pagingEnabled pass false to disable paging, true to enable (default)
     */
    open fun setPagingEnabled(pagingEnabled: Boolean) {
        pager.isPagingEnabled = pagingEnabled
        updateUi()
    }


    /**
     * @return true if swiping months is enabled, false if disabled. Default is true.
     */
    open fun isPagingEnabled(): Boolean {
        return pager.isPagingEnabled
    }

    /**
     * Preserve the current parameters of the Material Calendar View.
     */
    open fun state(builder: ((StateBuilder?) -> Unit)? = null): State? {

        builder?.let {
            it.invoke(state?.edit())
        }

        return state
    }

    /**
     * Initialize the parameters from scratch.
     */
    fun newState(builder: ((StateBuilder) -> Unit)? = null): StateBuilder {
        builder?.let {
            val state = StateBuilder()
            builder.invoke(state)
            return state
        }
        return StateBuilder()
    }

    fun commit(state: State) {
        // Use the calendarDayToShow to determine which date to focus on for the case of switching between month and week views
        var calendarDayToShow: CalendarDay? = null
        if (adapter != null && state.cacheCurrentPosition) {
            calendarDayToShow = adapter?.getItem(pager.currentItem)

            if (calendarMode != state.calendarMode) {
                val currentlySelectedDate = selectedDate

                if (calendarMode == CalendarMode.MONTHS && currentlySelectedDate != null) {
                    // Going from months to weeks
                    val lastVisibleCalendarDay = CalendarDay.from(calendarDayToShow?.date?.plusDays(1))

                    calendarDayToShow?.let {
                        if (
                            currentlySelectedDate == it ||
                            currentlySelectedDate.isAfter(it) &&
                            lastVisibleCalendarDay != null &&
                            currentlySelectedDate.isBefore(lastVisibleCalendarDay)
                        ) {
                            // Currently selected date is within view, so center on that
                            calendarDayToShow = currentlySelectedDate
                        }
                    }

                } else if (calendarMode == CalendarMode.WEEKS) {
                    // Going from weeks to months
                    val lastVisibleCalendar = calendarDayToShow?.date
                    val lastVisibleCalendarDay =
                        CalendarDay.from(lastVisibleCalendar?.plusDays(6))

                    calendarDayToShow?.let { day ->
                        lastVisibleCalendarDay?.let {
                            calendarDayToShow = if (
                                currentlySelectedDate != null &&
                                (
                                        currentlySelectedDate == calendarDayToShow ||
                                                currentlySelectedDate == lastVisibleCalendarDay ||
                                                (
                                                        currentlySelectedDate.isAfter(day) &&
                                                                currentlySelectedDate.isBefore(it)
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
            }
        }
        this.state = state
        // Recreate adapter
        adapter = when (calendarMode) {
            CalendarMode.MONTHS -> MonthPagerAdapter(this)
            CalendarMode.WEEKS -> WeekPagerAdapter(this)
            else -> throw IllegalArgumentException("Provided display mode which is not yet implemented")
        }

        setRangeDates(minDate, maxDate)

        adapter?.let {
            if (selectionMode == SELECTION_MODE_SINGLE && it.selectedDates.isNotEmpty()) {
                currentDate = it.selectedDates[0]
            }
        }

        calendarDayToShow?.let { day ->
            adapter?.let {
                pager.currentItem = it.getIndexForDay(day)
            }
        }

        invalidateDecorators()

        updateUi()
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

    /**
     * Simple layout params for MaterialCalendarView. The only variation for layout is height.
     */
    class LayoutParams
    /**
     * Create a layout that matches parent width, and is X number of tiles high
     *
     * @param tileHeight view height in number of tiles
     */
        (tileHeight: Int) : MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, tileHeight)

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

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>) {
        dispatchThawSelfOnly(container)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)


        state {
            it?.showOtherDates = ss.showOtherDates
            it?.setMinimumDate(ss.minDate)
            it?.setMaximumDate(ss.maxDate)
            it?.isCacheCalendarPositionEnabled(ss.cacheCurrentPosition)
            it?.commit(this)
        }

        showOtherDates = ss.showOtherDates
        allowClickDaysOutsideCurrentMonth = ss.allowClickDaysOutsideCurrentMonth
        clearSelection()
        for (calendarDay in ss.selectedDates) {
            setSelectedDate(calendarDay, true)
        }
        setTopbarVisible(ss.topbarVisible)
        selectionMode = ss.selectionMode
        dynamicHeightEnabled = ss.dynamicHeightEnabled
        currentDate = ss.currentMonth
    }

    fun setMode(mode: CalendarMode?) {
        state()?.apply {
            edit()
                .setCalendarDisplayMode(mode)
                .commit(this@MaterialCalendarView)
        }
    }

    fun updateAttrs(attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialCalendarView,
            0,
            0
        )

        try {

            val calendarModeIndex = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_calendarMode,
                0
            )

            firstDayOfWeekInt = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_firstDayOfWeek,
                firstDayOfWeekInt
            )

            titleChanger?.orientation = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_titleAnimationOrientation,
                VERTICAL
            )

            firstDayOfWeek = if (firstDayOfWeekInt in 1..7) {
                DayOfWeek.of(firstDayOfWeekInt)
            } else {
                WeekFields.of(Locale.getDefault()).firstDayOfWeek
            }

            showWeekDays = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_showWeekDays,
                true
            )

            newState {
                it.firstDayOfWeek = firstDayOfWeek
                it.setCalendarDisplayMode(CalendarMode.values()[calendarModeIndex])
                it.showWeekDays = showWeekDays
            }.commit(this)

            selectionMode = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_selectionMode,
                selectionMode
            )

            calendarContentDescription

            tileSize = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileSize,
                INVALID_TILE_DIMENSION
            )

            var tWidth = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileWidth,
                INVALID_TILE_DIMENSION
            )

            if (tWidth > INVALID_TILE_DIMENSION) {
                tileWidth = tWidth
            }

            val tHeight = a.getLayoutDimension(
                R.styleable.MaterialCalendarView_mcv_tileHeight,
                INVALID_TILE_DIMENSION
            )
            if (tileHeight > INVALID_TILE_DIMENSION) {
                tileHeight = tHeight
            }
            leftArrowRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_leftArrow,
                leftArrowRes
            )

            rightArrowRes = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_rightArrow,
                rightArrowRes
            )

            yearIconsColor = a.getResourceId(
                R.styleable.MaterialCalendarView_yearIconsColor,
                yearIconsColor
            )

            dayIconsColor = a.getResourceId(
                R.styleable.MaterialCalendarView_dayIconsColor,
                dayIconsColor
            )

            selectionColor = a.getColor(
                R.styleable.MaterialCalendarView_mcv_selectionColor,
                getThemeAccentColor(context)
            )


            a.getTextArray(R.styleable.MaterialCalendarView_mcv_weekDayLabels)
                ?.let {
                    weekDayFormatter = ArrayWeekDayFormatter(it)
                }

            a.getTextArray(R.styleable.MaterialCalendarView_mcv_monthLabels)
                ?.let {
                    setTitleFormatter(MonthArrayTitleFormatter(it))
                }

            headerTextAppearance = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_headerTextAppearance,
                headerTextAppearance
            )

            weekDayTextAppearance =
                a.getResourceId(
                    R.styleable.MaterialCalendarView_mcv_weekDayTextAppearance,
                    weekDayTextAppearance
                )

            dateTextAppearance = a.getResourceId(
                R.styleable.MaterialCalendarView_mcv_dateTextAppearance,
                R.style.TextAppearance_MaterialCalendarWidget_Date
            )

            showOtherDates = a.getInteger(
                R.styleable.MaterialCalendarView_mcv_showOtherDates,
                SHOW_DEFAULTS
            )

            allowClickDaysOutsideCurrentMonth = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_allowClickDaysOutsideCurrentMonth,
                true
            )

            textColor = a.getResourceId(
                R.styleable.MaterialCalendarView_dateTextColor,
                textColor
            )


            showTopBar = a.getBoolean(
                R.styleable.MaterialCalendarView_mcv_showTopBar,
                true
            )

        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        } finally {
            a.recycle()
        }
    }

    companion object {
        /**
         * Use this orientation to animate the title vertically
         */
        const val VERTICAL = 0

        const val INVALID_TILE_DIMENSION = -10

        /**
         * Selection mode that disallows all selection.
         * When changing to this mode, current selection will be cleared.
         */
        const val SELECTION_MODE_NONE = 0

        @JvmStatic
        val DEFAULT_DAY_TEXT_COLOR = DEFAULT_TEXT_COLOR

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
        const val SHOW_ALL = SHOW_OTHER_MONTHS or SHOW_OUT_OF_RANGE or SHOW_DECORATED_DISABLED


        /**
         * Use this orientation to animate the title horizontally
         */
        const val HORIZONTAL = 1

        @JvmStatic
        val DEFAULT_VISIBLE_WEEKS_COUNT = CalendarMode.MONTHS.visibleWeeksCount

        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the decorated disabled flag is set
         */
        @JvmStatic
        fun showDecoratedDisabled(@ShowOtherDates showOtherDates: Int): Boolean =
            showOtherDates and SHOW_DECORATED_DISABLED != 0

        /*
         * Show Other Dates Utils
         */
        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the other months flag is set
         */
        @JvmStatic
        fun showOtherMonths(@ShowOtherDates showOtherDates: Int): Boolean =
            (showOtherDates and SHOW_OTHER_MONTHS) != 0

        /*
         * Show Other Dates Utils
         */
        /**
         * @param showOtherDates int flag for show other dates
         * @return true if the out of range flag is set
         */
        @JvmStatic
        fun showOutOfRange(@ShowOtherDates showOtherDates: Int): Boolean {
            return showOtherDates and SHOW_OUT_OF_RANGE != 0
        }

        /**
         * Default tile size in DIPs. This is used in cases where there is no tile size specificed and the
         * view is set to [WRAP_CONTENT][ViewGroup.LayoutParams.WRAP_CONTENT]
         */
        const val DEFAULT_TILE_SIZE_DP = 44
        private const val DAYS_IN_WEEK = 7
        private const val DEFAULT_MAX_WEEKS = 6
        private const val DAY_NAMES_ROW = 1

        @JvmStatic
        fun getThemeAccentColor(context: Context): Int {
            val colorAttr: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                R.attr.colorAccent
            } else {
                //Get colorAccent defined for AppCompat
                context.resources.getIdentifier("colorAccent", "attr", context.packageName)
            }
            val outValue = TypedValue()
            context.theme.resolveAttribute(colorAttr, outValue, true)
            return outValue.data
        }


    }
}

private fun Context.getDrawableCompat(value: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getDrawable(value)
    } else {
        resources.getDrawable(value)
    }
}


fun Drawable.setTintCompat(@DrawableRes value: Int) {
    DrawableCompat.setTint(this, value)
}