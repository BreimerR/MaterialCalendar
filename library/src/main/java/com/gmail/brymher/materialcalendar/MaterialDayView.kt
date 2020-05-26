package com.gmail.brymher.materialcalendar

import android.app.Service
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlin.math.min

class MaterialDayView : ViewGroup {

    var dayHeight = SIZE.px
    var dayWidth = SIZE.px

    val size get() = min(height, width)
    val Int.px: Int
        get() {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics
            ).toInt()
        }

    val inflater = context.getSystemService(Service.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val view = inflater.inflate(R.layout.day_view, this, false)

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet? = null) {

    }

    /**@description
     * this method is used to position added views
     * and not exactly to add views to the layout
     * */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {

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


}