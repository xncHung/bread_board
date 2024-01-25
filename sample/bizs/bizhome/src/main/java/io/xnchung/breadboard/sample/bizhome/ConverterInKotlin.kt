package io.xnchung.breadboard.sample.bizhome

import io.xnchung.breadboard.annotations.ConverterProvider
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ConverterProvider
fun provideDateConverter(): String.(Type) -> Date? = l@{
    try {
        if (it != Date::class.java) return@l null
        SimpleDateFormat("yyyyMMdd", Locale.US).parse(this)
    } catch (e: Exception) {
        null
    }
}