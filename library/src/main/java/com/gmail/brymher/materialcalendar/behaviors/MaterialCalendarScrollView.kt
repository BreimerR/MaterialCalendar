package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.MaterialCalendarView

class MaterialCalendarScrollView {

    class Behavior(context: Context, attrs: AttributeSet) :
        ViewOffsetBehavior<RecyclerView>(context, attrs) {
        private var calendarHeight = 0

        var calendarView: MaterialCalendarView? = null

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: RecyclerView,
            dependency: View
        ): Boolean {
            var res = false
            (if (dependency is MaterialCalendarView) dependency else null)?.let {
                calendarView = dependency as MaterialCalendarView
                res = true
            }

            return res
        }

        override fun layoutChild(
            parent: CoordinatorLayout,
            child: RecyclerView,
            layoutDirection: Int
        ) {
            super.layoutChild(parent, child, layoutDirection)

            if (calendarHeight == 0) calendarHeight = calendarView?.measuredHeight ?: 0

            child.top = calendarHeight
            child.bottom = child.bottom + calendarHeight
        }
    }
}