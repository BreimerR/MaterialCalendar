package com.gmail.brymher.materialcalendar

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

open class CalendarViewPager : ViewPager {

    var pagingEnabled = true

    @JvmOverloads
    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean =
        pagingEnabled && super.onInterceptTouchEvent(ev)

    /** TODO
     * Implement onTouchEvent for this section
     * */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return pagingEnabled && super.onTouchEvent(ev)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        /**
         * disables scrolling vertically when paging disabled, fixes scrolling
         * for nested {@link android.support.v4.view.ViewPager}
         */
        return pagingEnabled && super.canScrollVertically(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        /**
         * disables scrolling horizontally when paging disabled, fixes scrolling
         * for nested {@link android.support.v4.view.ViewPager}
         */
        return pagingEnabled && super.canScrollHorizontally(direction)
    }
}