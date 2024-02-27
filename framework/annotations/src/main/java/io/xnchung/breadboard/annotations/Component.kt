package io.xnchung.breadboard.annotations

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class Component(
    val name: String,
    val params: Array<Param> = [],
    val description: String = "",
    val tags: Array<String> = [],
    val routes: Array<String> = [],
    val documentation: String = "",
    val owners: Array<String> = [],
    val sourcePath: String = "",
)

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class Param(
    val key: String,
    val rawType: KClass<*>,
    val typeArgs: Array<TypeToken> = [],
    val defaultValue: String = "",
    val desc: String = "",
    val collectionType: CollectionType = CollectionType.None,
    val nullable: Boolean = false,
    val qualifier: KClass<out Annotation> = DefaultQualifier::class,
    val provider: KClass<out ParamProviderContract<*>> = EmptyParamProvider::class
)

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParamProvider(
    val provider: KClass<out ParamProviderContract<*>>
)

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Ignore()


interface ParamProviderContract<out T> {
    val values: Iterable<T>
}

class EmptyParamProvider : ParamProviderContract<Any> {
    override val values get() = emptyList<Any>()
}

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class TypeToken(
    val rawType: KClass<*>,
    val typeArgs: Array<TypeToken> = [],
    val nullable: Boolean = false
)


enum class CollectionType {
    None,
    Array,
    ArrayList,
    List
}

@Target(AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ParamQualifier

@ParamQualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DefaultQualifier