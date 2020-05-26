package com.gmail.brymher.materialcalendar.utils

import kotlin.reflect.KProperty

class LateInit<T>(val initializer: () -> T, lock: Any?) {

    val locker = lock ?: this

    var value: T? = null

    var isInit = false

    operator fun setValue(any: Any, property: KProperty<*>, newValue: T) = synchronized(locker) {
        value = newValue
    }

    operator fun getValue(materialCalendarViewDummy: Any, property: KProperty<*>): T =
        synchronized(locker) {
            if (!isInit) {
                value = initializer()
                isInit = true
            }

            value!!
        }


}