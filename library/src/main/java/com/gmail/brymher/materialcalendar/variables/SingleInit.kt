package com.gmail.brymher.chama.utils.variables


import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
open class SingleInit<T>(private val initializer: () -> T, private val locker: Any? = null) {

    val isInitialized
        get() = value != UNINITIALIZED_VAR

    private val lock get() = locker ?: this

    @Suppress("PropertyName")
    @Volatile
    protected var _value: Any = UNINITIALIZED_VAR

    protected val value: T
        get() {
            return (if (_value == UNINITIALIZED_VAR) {
                synchronized(lock) {
                    _value = initializer.invoke() as Any
                    _value
                }
            } else _value) as T
        }

    operator fun getValue(receiver: Any?, property: KProperty<*>): T {
        return value
    }

    @Suppress("ClassName")
    internal object UNINITIALIZED_VAR
}

