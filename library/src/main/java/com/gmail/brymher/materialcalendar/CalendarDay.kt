package com.gmail.brymher.materialcalendar

import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.LocalDate

open class CalendarDay protected constructor(date: LocalDate) : Parcelable {

    /**
     * Everything is based on this variable for {@link CalendarDayX}.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    public val date = date
    val year
        get() = date.year

    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt(), parcel.readInt())

    protected constructor(year: Int, month: Int, day: Int) : this(LocalDate.of(year, month, day))


    /**
     * Determine if this day is within a specified range
     *
     * @param minDate the earliest day, may be null
     * @param maxDate the latest day, may be null
     * @return true if the between (inclusive) the min and max dates.
     */
    open fun isInRange(minDate: CalendarDay?, maxDate: CalendarDay?): Boolean {
        return !(minDate != null && minDate.isAfter(this)) &&
                !(maxDate != null && maxDate.isBefore(this))
    }



    /**
     * Get the month, represented by values from [LocalDate]
     *
     * @return the month of the year as defined by [LocalDate]
     */
    open fun getMonth(): Int {
        return date.monthValue
    }

    /**
     * Determine if this day is after the given instance
     *
     * @param other the other day to test
     * @return true if this is after other, false if equal or before
     */
    open fun isAfter(other: CalendarDay): Boolean {
        return date.isAfter(other.date)
    }

    override fun equals(other: Any?): Boolean {
        return other is CalendarDay && date == other.date
    }


    override fun hashCode(): Int {
        return CalendarDay.hashCode(date.year, date.monthValue, date.dayOfMonth)
    }


    override fun toString(): String {
        return ("CalendarDay{" + date.year + "-" + date.monthValue + "-" + date.dayOfMonth + "}")
    }


    /**
     * Get the day
     *
     * @return the day of the month for this day
     */
    open fun getDay(): Int {
        return date.dayOfMonth
    }

    /**
     * Determine if this day is before the given instance
     *
     * @param other the other day to test
     * @return true if this is before other, false if equal or after
     */
    fun isBefore(other: CalendarDay): Boolean {
        return date.isBefore(other.date)
    }

    /**
     * Parcelable Stuff
     */

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(date.year)
        dest.writeInt(date.monthValue)
        dest.writeInt(date.dayOfMonth)
    }

    companion object {
        /**
         * Get a new instance set to the specified day
         *
         * @param date [LocalDate] to pull date information from. Passing null will return null
         * @return CalendarDayX set to the specified date
         */
        @JvmStatic
        fun from(date: LocalDate?): CalendarDay? {
            return date?.let { CalendarDay(it) }
        }

        @JvmStatic
        val today = from(LocalDate.now())

        @JvmStatic
        fun today(): CalendarDay? = from(LocalDate.now())

        /**
         * Get a new instance set to the specified day
         *
         * @param year  new instance's year
         * @param month new instance's month as defined by [java.util.Calendar]
         * @param day   new instance's day of month
         * @return CalendarDayX set to the specified date
         */
        @JvmStatic
        fun from(year: Int, month: Int, day: Int): CalendarDay {
            return CalendarDay(year, month, day)
        }

        @JvmStatic
        public fun hashCode(year: Int, month: Int, day: Int): Int {
            //Should produce hashes like "20150401"
            return year * 10000 + month * 100 + day
        }


        @JvmStatic
        val CREATOR: Parcelable.Creator<CalendarDay> = object : Parcelable.Creator<CalendarDay> {
            override fun createFromParcel(`in`: Parcel): CalendarDay? {
                return CalendarDay(`in`)
            }

            override fun newArray(size: Int): Array<CalendarDay?> {
                return arrayOfNulls(size)
            }
        }
    }

}