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
import java.util.*


/**
 * 列表 Behavior
 * Created by NanBox on 2018/1/19.
 */
open class CalendarBehavior(context: Context?, attrs: AttributeSet?) :
    ViewOffsetBehavior<MaterialCalendarView>(context, attrs) {

    var calendarMode: CalendarMode? = CalendarMode.MONTHS
        protected set
    var weekOfMonth = Calendar.getInstance()[Calendar.WEEK_OF_MONTH]
    private var calendarLineHeight = 0
    private var weekCalendarHeight = 0
    private var monthCalendarHeight = 0
    private var listMaxOffset = 0
    private var velocityY = 0f
    private var canAutoScroll = true
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
        if (calendarMode === CalendarMode.MONTHS) {
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

            // MobileList
            val behavior =
                (target.layoutParams as CoordinatorLayout.LayoutParams).behavior
            if (behavior is CalendarScrollBehavior) {
                val listBehavior =
                    behavior
                val listOffset = MathUtils.clamp(
                    listBehavior.getTopAndBottomOffset() - dy, -listMaxOffset, 0
                )
                listBehavior.setTopAndBottomOffset(listOffset)
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
        if (calendarMode === CalendarMode.MONTHS) {
            val scroller = Scroller(coordinatorLayout.context)
            val offset: Int
            var duration = 800
            offset = if (Math.abs(velocityY) < 1000) {
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
            duration = duration * Math.abs(offset) / listMaxOffset
            scroller.startScroll(
                0, target.top,
                0, offset,
                duration
            )
            ViewCompat.postOnAnimation(child, object : Runnable {
                override fun run() {
                    if (scroller.computeScrollOffset() &&
                        target is RecyclerView
                    ) {
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

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: MaterialCalendarView,
        target: View,
        velocityX: Float, velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
    }

    protected open fun setMonthMode(calendarView: MaterialCalendarView) {
        if (calendarMode !== CalendarMode.WEEKS) {
            return
        }
        calendarMode = null
        calendarView.state?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit(calendarView)
        }
        setTopAndBottomOffset(-calendarLineHeight * (weekOfMonth - 1))
        calendarMode = CalendarMode.MONTHS
    }

    private fun setWeekMode(calendarView: MaterialCalendarView) {
        if (calendarMode !== CalendarMode.MONTHS) {
            return
        }
        calendarMode = null
        calendarView.state?.apply {
            edit()
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .commit(calendarView)
        }
        setTopAndBottomOffset(0)
        calendarMode = CalendarMode.WEEKS
    }

}