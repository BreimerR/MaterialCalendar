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
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import com.google.android.material.appbar.AppBarLayout
import org.w3c.dom.Attr


class Behaviors {

    abstract class DependencyBehavior<T : View>(context: Context, attributeSet: AttributeSet) :
        ViewOffsetBehavior<T>(context, attributeSet) {

        var tempRect1 = Rect()
        var tempRect2 = Rect()


        private var verticalLayoutGap = 0
        private var overlayTop = 0

        override fun layoutDependsOn(
            parent: CoordinatorLayout,
            child: T,
            dependency: View
        ): Boolean {
            return dependsOn(parent, child, dependency)
        }

        abstract fun dependsOn(parent: CoordinatorLayout, child: T, dependency: View): Boolean

        private var calendarHeight: Int? = null

        override fun layoutChild(
            parent: CoordinatorLayout,
            child: T,
            layoutDirection: Int
        ) {
            val dependencies = parent.getDependencies(child)

            val header: T? = findFirstDependency(dependencies)

            if (header != null) {
                val lp = child.layoutParams as CoordinatorLayout.LayoutParams
                val available: Rect = tempRect1
                available[parent.paddingLeft + lp.leftMargin, header.bottom + lp.topMargin, parent.width - parent.paddingRight - lp.rightMargin] =
                    parent.height + header.bottom - parent.paddingBottom - lp.bottomMargin
                /*       val parentInsets = parent.lastWindowInsets
                       if (parentInsets != null && ViewCompat.getFitsSystemWindows(parent)
                           && !ViewCompat.getFitsSystemWindows(child)
                       ) {
                           // If we're set to handle insets but this child isn't, then it has been measured as
                           // if there are no insets. We need to lay it out to match horizontally.
                           // Top and bottom and already handled in the logic above
                           available.left += parentInsets.systemWindowInsetLeft
                           available.right -= parentInsets.systemWindowInsetRight
                       }*/
                val out: Rect = tempRect2
                GravityCompat.apply(
                    resolveGravity(lp.gravity),
                    child.measuredWidth,
                    child.measuredHeight,
                    available,
                    out,
                    layoutDirection
                )
                val overlap: Int = getOverlapPixelsForOffset(header)
                child.layout(out.left, out.top - overlap, out.right, out.bottom - overlap)
                verticalLayoutGap = out.top - header.bottom
            } else {
                // If we don't have a dependency, let super handle it
                super.layoutChild(parent, child, layoutDirection)
                verticalLayoutGap = 0
            }


        }

        abstract fun checkView(view: View?): Boolean

        fun findFirstDependency(views: List<View?>): T? {
            var i = 0
            val z = views.size
            while (i < z) {
                val view = views[i]
                if (checkView(view)) {
                    @Suppress("UNCHECKED_CAST")
                    return view as T
                }
                i++
            }
            return null
        }

        fun getOverlapPixelsForOffset(header: View?): Int {
            return if (overlayTop == 0) 0 else MathUtils.clamp(
                (getOverlapRatioForOffset(
                    header
                ) * overlayTop).toInt(), 0, overlayTop
            )
        }

        private fun resolveGravity(gravity: Int): Int {
            return if (gravity == Gravity.NO_GRAVITY) GravityCompat.START or Gravity.TOP else gravity
        }

        private fun getOverlapRatioForOffset(header: View?): Float {
            return 1f
        }
    }

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