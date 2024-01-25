package io.xnchung.breadboard.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class Component(
    val name: String,
    val params: Array<Param<*>> = [],
    val description: String = "",
    val tags: Array<String> = [],
    val routes: Array<String> = [],
    val documentation: String = "",
    val owners: Array<String> = [],
    val sourcePath: String = "",
)

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
annotation class Param<T : Any>(
    val key: String,
    val rawType: KClass<T>,
    val typeArgs: Array<TypeToken<*>> = [],
    val defaultValue: String = "",
    val desc: String = "",
    val collectionType: CollectionType = CollectionType.None,
    val qualifier: KClass<out Annotation> = DefaultQualifier::class
)


annotation class TypeToken<T : Any>(
    val rawType: KClass<T>,
    val typeArgs: Array<TypeToken<*>> = []
)

enum class CollectionType {
    None,
    Array,
    ArrayList,
    List
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ParamQualifier

@ParamQualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class DefaultQualifier