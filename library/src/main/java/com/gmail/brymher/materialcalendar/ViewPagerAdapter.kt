package com.gmail.brymher.materialcalendar

import androidx.viewpager.widget.PagerAdapter


internal abstract class ViewPagerAdapter<V : CalendarPagerView>(
    val mcv: MaterialCalendarView,
    today:CalendarDay? = CalendarDay.today()
) : PagerAdapter() {
    
    private val currentViews: ArrayDeque<V> = ArrayDeque()


    val textColor: Int = DayView.DEFAULT_TEXT_COLOR
    
    init{
        currentViews.iterator();
    }


}