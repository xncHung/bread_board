package io.xnchung.breadboard.converters

import java.lang.reflect.Type

sealed class ConverterHolder(val qualifier: String, val order: Int) {
    class Normal(qualifier: String, order: Int, val func: String.(Type) -> Any?) : ConverterHolder(qualifier, order)
    class Suspend(qualifier: String, order: Int, val func: suspend String.(Type) -> Any?) : ConverterHolder(qualifier, order)
}

