package com.gmail.brymher.materialcalendar

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager


/**
 * Custom ViewPager that allows swiping to be disabled.
 */
class CalendarPager : ViewPager {
    /**
     * @return is this viewpager allowed to page
     */
    /**
     * enable disable viewpager scroll
     *
     * @param pagingEnabled false to disable paging, true for paging (default)
     */
    var isPagingEnabled = true

    constructor(context: Context) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isPagingEnabled && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isPagingEnabled && super.onTouchEvent(ev)
    }

    override fun canScrollVertically(direction: Int): Boolean {
        /**
         * disables scrolling vertically when paging disabled, fixes scrolling
         * for nested [android.support.v4.view.ViewPager]
         */
        return isPagingEnabled && super.canScrollVertically(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        /**
         * disables scrolling horizontally when paging disabled, fixes scrolling
         * for nested [android.support.v4.view.ViewPager]
         */
        return isPagingEnabled && super.canScrollHorizontally(direction)
    }
}
