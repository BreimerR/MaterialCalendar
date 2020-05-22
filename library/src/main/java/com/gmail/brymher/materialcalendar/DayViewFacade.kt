package com.gmail.brymher.materialcalendar

import android.graphics.drawable.Drawable
import java.util.*


/**
 * Abstraction layer to help in decorating Day views
 */
class DayViewFacade internal constructor() {
    var isDecorated = false
        private set
    var backgroundDrawable: Drawable? = null
        private set
    var selectionDrawable: Drawable? = null
        private set
    private val spans: LinkedList<Span>? = LinkedList()
    private var daysDisabled = false

    /**
     * Set a drawable to draw behind everything else
     *
     * @param drawable Drawable to draw behind everything
     */
    fun setBackgroundDrawable(drawable: Drawable) {
        requireNotNull(drawable) { "Cannot be null" }
        backgroundDrawable = drawable
        isDecorated = true
    }

    /**
     * Set a custom selection drawable
     * TODO: define states that can/should be used in StateListDrawables
     *
     * @param drawable the drawable for selection
     */
    fun setSelectionDrawable(drawable: Drawable) {
        requireNotNull(drawable) { "Cannot be null" }
        selectionDrawable = drawable
        isDecorated = true
    }

    /**
     * Add a span to the entire text of a day
     *
     * @param span text span instance
     */
    fun addSpan(span: Any) {
        if (spans != null) {
            spans.add(Span(span))
            isDecorated = true
        }
    }

    /**
     *
     * Set days to be in a disabled state, or re-enabled.
     *
     * Note, passing true here will **not** override minimum and maximum dates, if set.
     * This will only re-enable disabled dates.
     *
     * @param daysDisabled true to disable days, false to re-enable days
     */
    fun setDaysDisabled(daysDisabled: Boolean) {
        this.daysDisabled = daysDisabled
        isDecorated = true
    }

    fun reset() {
        backgroundDrawable = null
        selectionDrawable = null
        spans!!.clear()
        isDecorated = false
        daysDisabled = false
    }

    /**
     * Apply things set this to other
     *
     * @param other facade to apply our data to
     */
    fun applyTo(other: DayViewFacade) {
        if (selectionDrawable != null) {
            other.setSelectionDrawable(selectionDrawable!!)
        }
        if (backgroundDrawable != null) {
            other.setBackgroundDrawable(backgroundDrawable!!)
        }
        other.spans!!.addAll(spans!!)
        other.isDecorated = other.isDecorated or isDecorated
        other.daysDisabled = daysDisabled
    }

    fun getSpans(): List<Span> {
        return Collections.unmodifiableList(spans)
    }

    /**
     * Are days from this facade disabled
     *
     * @return true if disabled, false if not re-enabled
     */
    fun areDaysDisabled(): Boolean {
        return daysDisabled
    }

    class Span(val span: Any)

}
