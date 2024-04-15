package io.xnchung.breadboard.sample

import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.ParamQualifier
import io.xnchung.breadboard.sample.bizhome.getRawType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import org.json.JSONArray
import java.lang.reflect.Type

@ParamQualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class HexInt

@ConverterProvider
@HexInt
fun provideHexIntConverter(): suspend (String, Type) -> Int? = { s: String, type: Type ->
    val rawType = getRawType(type)
    if (rawType == Int::class.javaPrimitiveType || rawType == Int::class.javaObjectType) {
        flow<String> {
            delay(10000)
            emit(s)
        }.single().toIntOrNull(16)
    } else null
}

@ConverterProvider
@HexInt
fun provideHexIntArrayConverter(): suspend (String, Type) -> IntArray? = { s: String, type: Type ->
    try {
        if (getRawType(type).isArray) {
            val jsonArray = JSONArray(s)
            (0 until jsonArray.length()).map {
                jsonArray.getString(it).toInt(16)
            }.toIntArray()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


@ConverterProvider
@HexInt
fun provideHexIntArrayListConverter(): suspend (String, Type) -> IntArray? = { s: String, type: Type ->
    try {
        if (getRawType(type).isAssignableFrom(ArrayList::class.java)) {
            val jsonArray = JSONArray(s)
            (0 until jsonArray.length()).map {
                jsonArray.getString(it).toInt(16)
            }.toIntArray()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}