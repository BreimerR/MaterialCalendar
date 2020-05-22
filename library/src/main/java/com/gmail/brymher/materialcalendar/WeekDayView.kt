package com.gmail.brymher.materialcalendar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.gmail.brymher.materialcalendar.format.WeekDayFormatter

import org.threeten.bp.DayOfWeek

/**
 * Display a day of the week
 */
class WeekDayView : AppCompatTextView {

    var firstDayOfWeek = DayOfWeek.SUNDAY

    private var formatter = WeekDayFormatter.DEFAULT

    constructor(context: Context, dayOfWeek: DayOfWeek) : super(context) {
        this.dayOfWeek = dayOfWeek
    }

    constructor(context: Context) : super(context)

    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int
    ) : super(
        context,
        attributeSet,
        defStyleAttr
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet,
        defStyleAttr: Int,
        dayOfWeek: DayOfWeek
    ) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        this.dayOfWeek = dayOfWeek
    }

    constructor(
        context: Context,
        attributeSet: AttributeSet
    ) : super(
        context,
        attributeSet
    )

    constructor(
        context: Context,
        attributeSet: AttributeSet,
        dayOfWeek: DayOfWeek
    ) : super(
        context,
        attributeSet
    ) {
        this.dayOfWeek = dayOfWeek
    }

    var dayOfWeek: DayOfWeek = firstDayOfWeek
        set(value) {
            field = value
            text = formatter.format(dayOfWeek)
        }

    var weekDayFormatter: WeekDayFormatter?
        get() = formatter
        set(value) {
            formatter = value ?: WeekDayFormatter.DEFAULT
            dayOfWeek = dayOfWeek
        }

    init {
        gravity = Gravity.CENTER
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }
    }
}
