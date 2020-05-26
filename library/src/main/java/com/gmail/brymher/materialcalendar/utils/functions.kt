package com.gmail.brymher.materialcalendar.utils

fun <T> lazyVar(lock: Any? = null, initializer: () -> T): LazyVar<T> = LazyVar(initializer, lock)

fun <T> lateInit(lock: Any? = null, initializer: () -> T) = LateInit(initializer, lock)