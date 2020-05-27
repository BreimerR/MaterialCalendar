package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.GravityCompat
import androidx.core.view.ScrollingView
import androidx.recyclerview.widget.RecyclerView
import com.gmail.brymher.behaviors.DependencyBehavior
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import com.google.android.material.appbar.AppBarLayout
import org.w3c.dom.Attr


class Behaviors {


    abstract class AppBarBehavior<T : View>(context: Context, attributeSet: AttributeSet) :
        DependencyBehavior<T>(context, attributeSet) {

        override fun dependsOn(
            parent: CoordinatorLayout,
            child: T,
            dependency: View
        ): Boolean = checkView(dependency)

        override fun checkView(view: View?): Boolean = view is AppBarLayout

    }

    class BelowAppBar(context: Context, attrs: AttributeSet) :
        AppBarBehavior<MaterialCalendarView>(context, attrs)

    abstract class BelowMaterialCalendar<T : View>(context: Context, attrs: AttributeSet) :
        DependencyBehavior<T>(context, attrs) {

        override fun checkView(view: View?): Boolean {
            return view is MaterialCalendarView
        }

        override fun dependsOn(parent: CoordinatorLayout, child: T, dependency: View): Boolean {
            return checkView(dependency)
        }

    }

    class MaterialCalendarRecyclerScroll(context: Context, attrs: AttributeSet) :
        BelowMaterialCalendar<RecyclerView>(context, attrs) {

    }


    class Below : ViewOffsetBehavior<ConstraintLayout> {

        var calendarHeight = 0

        constructor() : super()

        constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

        override fun layoutChild(
            parent: CoordinatorLayout,
            child: ConstraintLayout,
            layoutDirection: Int
        ) {

            super.layoutChild(parent, child, layoutDirection)

            child.top = calendarHeight
            child.bottom = child.bottom + calendarHeight
        }

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: ConstraintLayout,
            dependency: View
        ): Boolean {
            val b = dependency is MaterialCalendarView

            if (b) calendarHeight = dependency.measuredHeight

            return b
        }
    }
}