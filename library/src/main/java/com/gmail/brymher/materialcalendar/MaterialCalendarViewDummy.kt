package com.gmail.brymher.materialcalendar

import android.app.Service
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.IntDef
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.gmail.brymher.materialcalendar.CalendarDay.Companion.from
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.DEFAULT_VISIBLE_WEEKS_COUNT
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.INVALID_TILE_DIMENSION
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.SELECTION_MODE_MULTIPLE
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.SELECTION_MODE_NONE
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.SELECTION_MODE_RANGE
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.SELECTION_MODE_SINGLE
import com.gmail.brymher.materialcalendar.MaterialCalendarView.Companion.SHOW_ALL
import com.gmail.brymher.materialcalendar.MaterialCalendarView.ShowOtherDates
import com.gmail.brymher.materialcalendar.format.DayFormatter
import com.gmail.brymher.materialcalendar.utils.lateInit
import com.gmail.brymher.materialcalendar.utils.lazyVar
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import kotlin.math.max

open class MaterialCalendarViewDummy