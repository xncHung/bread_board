package io.xnchung.breadboard.sample.bizhome

import io.xnchung.breadboard.annotations.ConverterProvider
import java.lang.reflect.Array
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

@ConverterProvider
fun provideDateConverter(): String.(Type) -> Date? = l@{
    try {
        if (it != Date::class.java) return@l null
        SimpleDateFormat("yyyyMMdd", Locale.US).parse(this)
    } catch (e: Exception) {
        null
    }
}

fun getRawType(type: Type): Class<*> {
    Objects.requireNonNull(type, "type == null")
    if (type is Class<*>) {
        // Type is a normal class.
        return type
    }
    if (type is ParameterizedType) {

        // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
        // suspects some pathological case related to nested classes exists.
        val rawType = type.rawType
        require(rawType is Class<*>)
        return rawType
    }
    if (type is GenericArrayType) {
        val componentType = type.genericComponentType
        return Array.newInstance(getRawType(componentType), 0).javaClass
    }
    if (type is TypeVariable<*>) {
        // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
        // type that's more general than necessary is okay.
        return Any::class.java
    }
    if (type is WildcardType) {
        return getRawType(type.upperBounds[0])
    }
    throw IllegalArgumentException(
        "Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <"
                + type
                + "> is of type "
                + type.javaClass.name
    )
}