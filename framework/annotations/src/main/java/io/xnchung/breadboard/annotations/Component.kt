package io.xnchung.breadboard.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
annotation class Component(
    val name: String,
    val params: Array<Param<*>> = [],
    val description: String = "",
    val tags: Array<String> = [],
    val routes: Array<String> = [],
    val documentation: String = "",
    val owners: Array<String> = [],
)

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class Param<T : Any>(
    val key: String,
    val type: KClass<T>,
    val collectionType: CollectionType = CollectionType.None,
    val qualifier: KClass<out Annotation> = DefaultQualifier::class
)

enum class CollectionType {
    None,
    Array,
    ArrayList
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ParamQualifier

@ParamQualifier
annotation class DefaultQualifier