package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Scroller
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.CalendarMode
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import com.google.android.material.appbar.AppBarLayout
import java.util.*
import kotlin.math.abs

class AppBarMaterialCalendarBehavior(
    context: Context,
    attrs: AttributeSet
) : ViewOffsetBehavior<MaterialCalendarView>(context, attrs) {

    private var calendarMode: CalendarMode? = CalendarMode.MONTHS
    private var weekOfMonth =
        Calendar.getInstance()[Calendar.WEEK_OF_MONTH]
    private var calendarLineHeight = 0
    private var weekCalendarHeight = 0
    private var monthCalendarHeight = 0
    private var listMaxOffset = 0
    private var velocityY = 0f
    private var canAutoScroll = true
    var appBarHeight: Int = 0

    val calendarHeight
        get() =
            if (calendarMode == CalendarMode.WEEKS) weekCalendarHeight else monthCalendarHeight

    val calendarBottom get() = calendarHeight + appBarHeight

    var calendarView: MaterialCalendarView? = null

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: MaterialCalendarView,
        dependency: View
    ): Boolean {
        var res = false

        (if (dependency is AppBarLayout) dependency else null)?.let {
            appBarHeight = dependency.measuredHeight
            res = true
        }

        return res
    }

    override fun layoutChild(
        parent: CoordinatorLayout,
        child: MaterialCalendarView,
        layoutDirection: Int
    ) {
        super.layoutChild(parent, child, layoutDirection)

        child.top = appBarHeight
        child.bottom = calendarBottom

    }


    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        directTargetChild: View,
        target: View,
        axes: Int, type: Int
    ): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0 && !target.canScrollVertically(-1)
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        target: View,
        dx: Int, dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // When the list does not slide to the top, it is not processed
        if (target.canScrollVertically(-1)) {
            return
        }
        setMonthMode(child)
        if (calendarMode == CalendarMode.MONTHS) {
            if (calendarLineHeight == 0) {
                calendarLineHeight = child.measuredHeight / 7
                weekCalendarHeight = calendarLineHeight * 2
                monthCalendarHeight = calendarLineHeight * 7
                listMaxOffset = calendarLineHeight * 5
            }
            // Mobile calendar
            val calendarMinOffset = -calendarLineHeight * (weekOfMonth - 1)
            val calendarOffset = MathUtils.clamp(
                getTopAndBottomOffset() - dy, calendarMinOffset, 0
            )
            setTopAndBottomOffset(calendarOffset)

            // ListViewBehavior
            val behavior = (target.layoutParams as CoordinatorLayout.LayoutParams).behavior
            if (behavior is CalendarScrollBehavior) {
                val listOffset = MathUtils.clamp(
                    behavior.getTopAndBottomOffset() - dy, -listMaxOffset, 0
                )
                behavior.setTopAndBottomOffset(listOffset)
                if (listOffset > -listMaxOffset && listOffset < 0) {
                    consumed[1] = dy
                }
            }
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        target: View,
        type: Int
    ) {
        if (calendarLineHeight == 0) {
            return
        }
        if (target.top == weekCalendarHeight) {
            setWeekMode(child)
            return
        } else if (target.top == monthCalendarHeight) {
            setMonthMode(child)
            return
        }
        if (!canAutoScroll) {
            return
        }
        if (calendarMode == CalendarMode.MONTHS) {
            val scroller = Scroller(coordinatorLayout.context)
            var duration = 800
            val offset: Int = if (abs(velocityY) < 1000) {
                if (target.top > calendarLineHeight * 4) {
                    // ScrollToMonthMode
                    monthCalendarHeight - target.top
                } else {
                    // scrollToWeeklyMode
                    weekCalendarHeight - target.top
                }
            } else {
                if (velocityY > 0) {
                    // scrollToWeeklyMode
                    weekCalendarHeight - target.top
                } else {
                    // scrollToMonthMode
                    monthCalendarHeight - target.top
                }
            }
            velocityY = 0f
            duration = duration * abs(offset) / listMaxOffset
            scroller.startScroll(
                0, target.top,
                0, offset,
                duration
            )
            ViewCompat.postOnAnimation(child, object : Runnable {
                override fun run() {
                    if (scroller.computeScrollOffset() && target is RecyclerView) {
                        canAutoScroll = false
                        val delta = target.getTop() - scroller.currY
                        target.startNestedScroll(
                            ViewCompat.SCROLL_AXIS_VERTICAL,
                            ViewCompat.TYPE_TOUCH
                        )
                        target.dispatchNestedPreScroll(
                            0, delta, IntArray(2), IntArray(2), ViewCompat.TYPE_TOUCH
                        )
                        ViewCompat.postOnAnimation(child, this)
                    } else {
                        canAutoScroll = true
                        if (target.top == weekCalendarHeight) {
                            setWeekMode(child)
                        } else if (target.top == monthCalendarHeight) {
                            setMonthMode(child)
                        }
                    }
                }
            })
        }
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        target: View,
        velocityX: Float, velocityY: Float
    ): Boolean {
        this.velocityY = velocityY
        return !(target.top == weekCalendarHeight ||
                target.top == monthCalendarHeight) && !target.canScrollVertically(-1)
    }

    private fun setMonthMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.MONTHS) {
            return
        }

        calendarView.state()?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit(calendarView)
        }

        setTopAndBottomOffset(-calendarLineHeight * (weekOfMonth - 1))
        calendarMode = CalendarMode.MONTHS
    }

    private fun setWeekMode(calendarView: MaterialCalendarView) {
        if (calendarMode == CalendarMode.WEEKS) {
            return
        }

        calendarView.state()?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit(calendarView)
        }

        setTopAndBottomOffset(calendarLineHeight)

        calendarMode = CalendarMode.WEEKS
    }

}