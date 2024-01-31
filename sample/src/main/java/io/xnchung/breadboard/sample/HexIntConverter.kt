package io.xnchung.breadboard.sample

import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.ParamQualifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import java.lang.reflect.Type

@ParamQualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HexInt

@ConverterProvider
@HexInt
fun provideHexIntConverter(): suspend (String, Type) -> Int? = { s: String, type: Type ->
    flow<String> {
        delay(1000)
        emit(s)
    }.single().toIntOrNull(16)
}