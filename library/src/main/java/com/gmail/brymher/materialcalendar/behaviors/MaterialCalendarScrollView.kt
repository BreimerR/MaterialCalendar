package com.gmail.brymher.materialcalendar.behaviors

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.RecyclerView
import com.gmail.brymher.behaviors.ViewOffsetBehavior
import com.gmail.brymher.materialcalendar.MaterialCalendarView
import com.google.android.material.appbar.AppBarLayout

class MaterialCalendarScrollView {

    class Behavior(context: Context, attrs: AttributeSet) :
        ViewOffsetBehavior<RecyclerView>(context, attrs) {

        var tempRect1 = Rect()
        var tempRect2 = Rect()


        private var verticalLayoutGap = 0
        private var overlayTop = 0

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
                calendarHeight = dependency.measuredHeight
            }

            return res
        }

        /*
                override fun layoutChild(
                    parent: CoordinatorLayout,
                    child: RecyclerView,
                    layoutDirection: Int
                ) {
                    super.layoutChild(parent, child, layoutDirection)

                    val height = calendarView?.measuredHeight ?: 0


                    child.top = (calendarView?.bottom ?: 0)
                    child.bottom = child.height + height

                }
        */
        fun findFirstDependency(views: List<View?>): MaterialCalendarView? {
            var i = 0
            val z = views.size
            while (i < z) {
                val view = views[i]
                if (view is MaterialCalendarView) {
                    return view
                }
                i++
            }
            return null
        }

        override fun layoutChild(
            parent: CoordinatorLayout,
            child: RecyclerView,
            layoutDirection: Int
        ) {
            val dependencies =
                parent.getDependencies(child)
            val header: MaterialCalendarView? = findFirstDependency(dependencies)
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
}