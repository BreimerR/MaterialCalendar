package com.gmail.brymher.materialcalendar.variables

import com.gmail.brymher.chama.utils.variables.MutableSingleInit
import com.gmail.brymher.chama.utils.variables.NullableSingleInit
import com.gmail.brymher.chama.utils.variables.SingleInit

fun <T> singleInit(initializer: () -> T): SingleInit<T> = SingleInit(initializer)

fun <T> mutableSingleInit(initializer: () -> T): MutableSingleInit<T> =
    MutableSingleInit(initializer)

fun <T> nullableSingleInit(initializer: () -> T?): NullableSingleInit<T> =
    NullableSingleInit(initializer)

fun <T> lazyVar(lock: Any? = null, initializer: () -> T?): LazyVar<T> = LazyVar(initializer, lock)