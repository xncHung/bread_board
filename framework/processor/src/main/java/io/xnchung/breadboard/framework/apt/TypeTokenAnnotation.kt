package io.xnchung.breadboard.framework.apt

import io.xnchung.breadboard.annotations.TypeToken
import io.xnchung.breadboard.framework.ksp.ITypeTokenAnnotation
import javax.lang.model.element.AnnotationMirror

internal class TypeTokenAnnotation(annotation: TypeToken, mirror: AnnotationMirror) : ITypeTokenAnnotation {
    override val rawType = mirror.rawTypeFqnFromMirror()
    override val typeArgs = run {
        val list = mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("typeArgs") }?.value?.value as? List<*>
        annotation.typeArgs.mapIndexed { index, it ->
            TypeTokenAnnotation(it, list?.get(index) as AnnotationMirror)
        }
    }
    override val nullable: Boolean = annotation.nullable
}