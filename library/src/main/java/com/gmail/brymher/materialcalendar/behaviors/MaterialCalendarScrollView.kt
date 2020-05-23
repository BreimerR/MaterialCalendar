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
        private var calendarHeight: Int? = null

        var calendarView: MaterialCalendarView? = null

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: RecyclerView,
            dependency: View
        ): Boolean {
            val res = dependency is MaterialCalendarView

            if (res) {
                calendarView = dependency as MaterialCalendarView
            }

            return res
        }

        override fun layoutChild(
            parent: CoordinatorLayout,
            child: RecyclerView,
            layoutDirection: Int
        ) {
            super.layoutChild(parent, child, layoutDirection)

            val height = if (calendarHeight == null) calendarView?.measuredHeight ?: 0 else 0

            child.top = (calendarView?.bottom ?: 0)
            child.bottom = child.bottom + height
        }
    }
}