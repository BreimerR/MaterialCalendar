package com.gmail.brymher.materialcalendar.utils

import kotlin.reflect.KProperty

class LazyVar<T>(val initializer: () -> T, lock: Any?) {

    private var locker = lock ?: this

    var isInit = false

    var value: T? = null
        get() = synchronized(locker) {
            field
        }
        set(value) = synchronized(locker) {
            field = value
        }

    operator fun setValue(any: Any, property: KProperty<*>, newValue: T?) {
        value = newValue
    }

    operator fun getValue(any: Any, property: KProperty<*>): T =
        synchronized(locker) {
            if (!isInit) value = initializer()

            value!!
        }


}

