package io.xnchung.breadboard.model.builtin

import io.xnchung.breadboard.annotations.Converter
import java.math.BigDecimal
import java.math.BigInteger

@Converter
fun String.int(): Int = toInt()

@Converter
fun String.long(): Long = toLong()

@Converter
fun String.string(): String = this

@Converter
fun String.boolean(): Boolean = toBoolean()

@Converter
fun String.float(): Float = toFloat()

@Converter
fun String.double(): Double = toDouble()

@Converter
fun String.short(): Short = toShort()

@Converter
fun String.byte(): Byte = toByte()

@Converter
fun String.bigInteger(): BigInteger = toBigInteger()

@Converter
fun String.bigDecimal(): BigDecimal = toBigDecimal()
