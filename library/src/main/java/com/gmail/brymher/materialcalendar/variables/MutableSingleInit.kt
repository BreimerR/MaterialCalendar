package com.gmail.brymher.chama.utils.variables

import kotlin.reflect.KProperty

open class MutableSingleInit<T>(initializer: () -> T) : SingleInit<T>(initializer) {
    operator fun setValue(receiver: Any?, property: KProperty<*>, value: T) {
        this._value = value as Any
    }
}