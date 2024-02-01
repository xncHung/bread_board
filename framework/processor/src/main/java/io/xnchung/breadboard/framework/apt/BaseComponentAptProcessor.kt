package io.xnchung.breadboard.framework.apt

import io.xnchung.breadboard.framework.ProcessConstants
import javax.annotation.processing.AbstractProcessor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

abstract class BaseComponentAptProcessor : AbstractProcessor() {
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    protected fun Element.toFullPath(): String {
        return when (this) {
            is TypeElement -> {
                qualifiedName.toString()
            }

            is ExecutableElement -> {
                enclosingElement.toFullPath() + "." + simpleName.toString()
            }

            else -> {
                processingEnv.elementUtils.getPackageOf(this).qualifiedName.toString() + "." + simpleName.toString()
            }
        }

    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return if (processingEnv.options[ProcessConstants.ARG_CODE_GEN_ENABLE]?.toBoolean() == true) {
            super.getSupportedAnnotationTypes()
        } else {
            emptySet()
        }
    }
}