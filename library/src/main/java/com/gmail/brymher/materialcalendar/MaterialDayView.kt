package com.gmail.brymher.materialcalendar

import android.app.Service
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.min

/**@description
 * display selected day
 * enable adding custom day view
 * */
class MaterialDayView : ViewGroup {

    var dayHeight = SIZE.px
    var dayWidth = SIZE.px

    val size get() = min(height, width)

    private val Int.px: Int
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics
            ).toInt()
        }

    @Suppress("MemberVisibilityCanBePrivate")
    val inflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    private val view = inflater.inflate(R.layout.day_view, this, false)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet? = null) {
        initView()
    }

    fun initView() {
        addView(view)
    }

    /**@description
     * this method is used to position added views
     * and not exactly to add views to the layout
     * */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {

            val child = getChildAt(i)
            if (child.visibility == View.GONE) continue

            val width = child.measuredWidth
            val height = child.measuredHeight


        }
    }

    /**@description
     * used to decide the view size as required
     * */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    companion object {
        var SIZE = 40
    }


    abstract class OnClickListener {
        abstract fun onClickAction()
    }

    abstract class OnLongPressListener

}