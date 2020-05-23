package com.gmail.brymher.chama.utils.variables

import kotlin.reflect.KProperty

open class NullableSingleInit<T>(private val initializer: () -> T?) {

    val isInitialized
        get() = value != null

    val reInit: Boolean
        get() {

            value = initializer.invoke()

            return isInitialized
        }

    var onInit: ((T?) -> Unit)? = null

    open fun onInit(value: T?) {

    }

    protected var value: T? = null
        get() {
            if (field == null)
                field = initializer.invoke()




            return field
        }

    operator fun getValue(receiver: Any?, property: KProperty<*>): T? = value
    operator fun setValue(materialCalendarView: Any, property: KProperty<*>, value: T?) {
        this.value = value
    }


}