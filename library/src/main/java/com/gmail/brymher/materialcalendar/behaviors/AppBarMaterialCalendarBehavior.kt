package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.CalendarMode
import com.gmail.brymher.materialcalendar.MaterialCalendarView

import com.google.android.material.appbar.AppBarLayout
import java.util.*

class AppBarMaterialCalendarBehavior : CalendarBehavior {

    var calendarLineHeight = 0
    var weekCalendarHeight = 0
    var monthCalendarHeight = 0
    var listMaxOffset = 0
    var velocityY = 0f
    var canAutoScroll = true

    constructor(context: Context) : super(context, null)

    constructor (context: Context?, attrs: AttributeSet) : super(context, attrs)



    val calendarHeight
        get() = if (calendarMode == CalendarMode.WEEKS) weekCalendarHeight else monthCalendarHeight

    val calendarBottom get() = calendarHeight + appBarHeight

    var calendarView: MaterialCalendarView? = null

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: MaterialCalendarView,
        dependency: View
    ): Boolean {
        val res = dependency is AppBarLayout

        if (res) appBarHeight = dependency.measuredHeight

        return res
    }

    override fun layoutChild(
        parent: CoordinatorLayout,
        child: MaterialCalendarView,
        layoutDirection: Int
    ) {
        super.layoutChild(parent, child, layoutDirection)

        child.top = appBarHeight
        child.bottom = child.bottom + appBarHeight

    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }

    override fun setMonthMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.MONTHS) {
            return
        }

        calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit(calendarView)

        setTopAndBottomOffset(-calendarLineHeight * (weekOfMonth - 1))
        calendarMode = CalendarMode.MONTHS
    }

    fun setWeekMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.WEEKS) {
            return
        }

        calendarView.state().edit()
            .setCalendarDisplayMode(CalendarMode.WEEKS)
            .commit(calendarView)

        setTopAndBottomOffset(calendarLineHeight)

        calendarMode = CalendarMode.WEEKS
    }

}