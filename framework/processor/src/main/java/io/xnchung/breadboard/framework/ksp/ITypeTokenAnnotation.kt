package io.xnchung.breadboard.framework.ksp

internal interface ITypeTokenAnnotation {
    val rawType: String?
    val typeArgs: List<ITypeTokenAnnotation>?
    val nullable: Boolean
}