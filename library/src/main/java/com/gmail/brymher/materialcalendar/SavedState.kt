package com.gmail.brymher.materialcalendar

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import java.util.*

class SavedState : View.BaseSavedState {
    var showOtherDates = MaterialCalendarView.SHOW_DEFAULTS
    var allowClickDaysOutsideCurrentMonth = true
    var minDate: CalendarDay? = null
    var maxDate: CalendarDay? = null
    var selectedDates: MutableList<CalendarDay> = mutableListOf()
    var topbarVisible = true
    var selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
    var dynamicHeightEnabled = false
    var currentMonth: CalendarDay? = null
    var cacheCurrentPosition = false

    internal constructor(superState: Parcelable?) : super(superState) {}

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(showOtherDates)
        out.writeByte((if (allowClickDaysOutsideCurrentMonth) 1 else 0).toByte())
        out.writeParcelable(minDate, 0)
        out.writeParcelable(maxDate, 0)
        out.writeTypedList(selectedDates)
        out.writeInt(if (topbarVisible) 1 else 0)
        out.writeInt(selectionMode)
        out.writeInt(if (dynamicHeightEnabled) 1 else 0)
        out.writeParcelable(currentMonth, 0)
        out.writeByte((if (cacheCurrentPosition) 1 else 0).toByte())
    }

    private constructor(`in`: Parcel) : super(`in`) {
        showOtherDates = `in`.readInt()
        allowClickDaysOutsideCurrentMonth = `in`.readByte().toInt() != 0
        val loader = CalendarDay::class.java.classLoader
        minDate = `in`.readParcelable(loader)
        maxDate = `in`.readParcelable(loader)
        `in`.readTypedList(selectedDates, CalendarDay.CREATOR)
        topbarVisible = `in`.readInt() == 1
        selectionMode = `in`.readInt()
        dynamicHeightEnabled = `in`.readInt() == 1
        currentMonth = `in`.readParcelable(loader)
        cacheCurrentPosition = `in`.readByte().toInt() != 0
    }

    companion object {
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
            override fun createFromParcel(`in`: Parcel): SavedState? {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}