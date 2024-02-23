package io.xnchung.breadboard.converters

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

internal val gson by lazy {
    Gson()
}

internal fun <T> provideBuiltinConverterFactory(): String.(Type) -> T? = {
    try {
        gson.fromJson<T>(this, TypeToken.get(it).type)
    } catch (e: Exception) {
        null
    }
}