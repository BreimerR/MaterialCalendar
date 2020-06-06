package com.gmail.brymher.materialcalendar.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.DrawableCompat

fun <T> lazyVar(lock: Any? = null, initializer: () -> T): LazyVar<T> = LazyVar(initializer, lock)

fun <T> lateInit(lock: Any? = null, initializer: () -> T) = LateInit(initializer, lock)


fun Context.getDrawableCompat(value: Int): Drawable? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        getDrawable(value)
    } else {
        resources.getDrawable(value)
    }
}


fun Drawable.setTintCompat(@DrawableRes value: Int) {
    DrawableCompat.setTint(this, value)
}