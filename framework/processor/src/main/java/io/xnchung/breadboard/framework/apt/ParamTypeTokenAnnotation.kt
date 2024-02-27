package io.xnchung.breadboard.framework.apt

import io.xnchung.breadboard.annotations.Param
import io.xnchung.breadboard.framework.ksp.ITypeTokenAnnotation
import javax.lang.model.element.AnnotationMirror

internal class ParamTypeTokenAnnotation(annotation: Param, mirror: AnnotationMirror) : ITypeTokenAnnotation {
    override val rawType = mirror.rawTypeFqnFromMirror()


    override val typeArgs = run {
        val list = mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("typeArgs") }?.value?.value as? List<*>
        annotation.typeArgs.mapIndexed { index, it ->
            val annotationMirror = list?.get(index) as AnnotationMirror
            TypeTokenAnnotation(it, annotationMirror)
        }
    }
    override val nullable: Boolean = annotation.nullable

}