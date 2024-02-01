package io.xnchung.breadboard.framework.apt

import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

val Element.isPublic get() = Modifier.PUBLIC in modifiers
val Element.isStatic get() = Modifier.STATIC in modifiers
