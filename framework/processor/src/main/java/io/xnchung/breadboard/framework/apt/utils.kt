package io.xnchung.breadboard.framework.apt

import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier

val Element.isPublic get() = Modifier.PUBLIC in modifiers
val Element.isStatic get() = Modifier.STATIC in modifiers
val Element.isPrivate get() = Modifier.PRIVATE in modifiers
val Element.isClass get() = kind == ElementKind.CLASS
fun Element.staticCallElement(): Element? {
    val parent = enclosingElement
    if (isStatic) return parent
    val predicate: (Element) -> Boolean = {
        it.kind == ElementKind.FIELD && it.isStatic && it.asType() == parent.asType() && !it.isPrivate
    }
    return parent.enclosedElements.firstOrNull(predicate) // kotlin object
        ?: parent.enclosingElement.enclosedElements.firstOrNull(predicate)// kotlin companion object
}
