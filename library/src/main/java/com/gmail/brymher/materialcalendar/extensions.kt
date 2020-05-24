package com.gmail.brymher.materialcalendar

import android.os.Build
import android.widget.TextView

fun <T : TextView> T.setTextAppearanceCompat(resId: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.setTextAppearance(resId)
    }
}