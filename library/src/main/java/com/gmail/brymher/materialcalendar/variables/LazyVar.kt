package com.gmail.brymher.materialcalendar.variables

import kotlin.reflect.KProperty

class LazyVar<T>(initializer: () -> T?, val locker: Any? = null) {

    private val lock get() = locker ?: this

    private var _value: T? = null
        get() = synchronized(lock) {
            field
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }


    operator fun getValue(receiver: Any?, property: KProperty<*>): T? {
        return _value
    }

    operator fun setValue(materialCalendarView: Any, property: KProperty<*>, value: T?) {
        _value = value
    }
}