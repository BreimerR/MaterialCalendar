package com.gmail.brymher.materialcalendar

import android.R
import android.animation.Animator
import android.os.Build
import android.text.TextUtils
import android.util.TypedValue
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.TextView
import com.gmail.brymher.materialcalendar.format.TitleFormatter
import java.util.*

class TitleChanger(private val title: TextView?) {
    var titleFormatter: TitleFormatter? = TitleFormatter.DEFAULT
    private val animDelay: Int
    val animDuration: Int
    private val translate: Int
    val interpolator: Interpolator = DecelerateInterpolator(2f)
    var orientation = MaterialCalendarView.VERTICAL
    private var lastAnimTime: Long = 0
    val today = CalendarDay.today

    var previousMonth: CalendarDay? = CalendarDay.previousMonth

    val currentTime get() = System.currentTimeMillis()

    fun change(currentMonth: CalendarDay?) {

        val time = currentTime

        if (currentMonth == null) {
            return
        }
        if (TextUtils.isEmpty(title?.text) || time - lastAnimTime < animDelay) {
            doChange(time, currentMonth, false)
        }
        if (currentMonth == previousMonth || (
                    currentMonth.month == previousMonth?.month && currentMonth.year == previousMonth?.year
                    )
        ) {
            return
        }
        doChange(currentTime, currentMonth, true)
    }

    private fun doChange(
        now: Long,
        currentMonth: CalendarDay,
        animate: Boolean
    ) {
        title?.animate()?.cancel()
        doTranslation(title, 0)
        title?.alpha = 1f
        lastAnimTime = now

        val newTitle = titleFormatter?.format(currentMonth)

        if (!animate) {
            title?.text = newTitle
        } else {
            val translation =
                translate * if (previousMonth?.isBefore(currentMonth) == true) 1 else -1

            val viewPropertyAnimator = title?.animate()

            if (orientation == MaterialCalendarView.HORIZONTAL) {
                viewPropertyAnimator?.translationX(translation * (-1).toFloat())
            } else {
                viewPropertyAnimator?.translationY(translation * (-1).toFloat())
            }
            viewPropertyAnimator?.apply {
                alpha(0f)
                duration = animDuration.toLong()
                if (Build.VERSION.SDK_INT >= 18) {
                    interpolator = this@TitleChanger.interpolator
                }

                setListener(object : AnimatorListener() {
                    override fun onAnimationCancel(animator: Animator) {
                        doTranslation(title, 0)
                        title?.alpha = 1f
                    }

                    override fun onAnimationEnd(animator: Animator) {
                        title?.text = newTitle

                        doTranslation(title, translation)

                        title?.animate()?.apply {
                            if (orientation == MaterialCalendarView.HORIZONTAL) {
                                translationX(0f)
                            } else {
                                translationY(0f)
                            }
                            alpha(1f)
                            duration = animDuration.toLong()

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                interpolator = this@TitleChanger.interpolator
                            }
                            setListener(AnimatorListener())
                            start()
                        }
                    }
                }).start()
            }

        }
        previousMonth = currentMonth
    }

    private fun doTranslation(title: TextView?, translate: Int) {
        if (orientation == MaterialCalendarView.HORIZONTAL) {
            title?.translationX = translate.toFloat()
        } else {
            title?.translationY = translate.toFloat()
        }
    }

    companion object {
        const val DEFAULT_ANIMATION_DELAY = 400
        const val DEFAULT_Y_TRANSLATION_DP = 20
    }

    init {
        val res = title?.resources
        animDelay = DEFAULT_ANIMATION_DELAY
        animDuration = (res?.getInteger(R.integer.config_shortAnimTime) ?: 500) / 2
        translate = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            DEFAULT_Y_TRANSLATION_DP.toFloat(),
            res?.displayMetrics
        ).toInt()
    }
}
