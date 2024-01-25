package io.xnchung.breadboard.annotations

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class ConverterProvider(
    val order: Int = 0
)