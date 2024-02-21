package io.xnchung.breadboard.framework.apt

import io.xnchung.breadboard.framework.ProcessConstants
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

abstract class BaseComponentAptProcessor : AbstractProcessor() {
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    final override fun process(annotations: MutableSet<out TypeElement>, environment: RoundEnvironment): Boolean {
        return if (processingEnv.options[ProcessConstants.ARG_CODE_GEN_ENABLE]?.toBoolean() == true) {
            val isApp = processingEnv.options[ProcessConstants.ARG_CODE_GEN_IS_APP]?.toBoolean() == true
            val isInstrumentation = processingEnv.options.getOrDefault(
                ProcessConstants.ARG_CODE_GEN_STRATEGY,
                ProcessConstants.ARG_VALUE_INSTRUMENTATION
            ) == ProcessConstants.ARG_VALUE_INSTRUMENTATION
            realProcess(annotations, environment, isInstrumentation, isApp)
        } else {
            true
        }
    }

    abstract fun realProcess(
        annotations: MutableSet<out TypeElement>,
        environment: RoundEnvironment,
        isInstrumentation: Boolean,
        isApp: Boolean
    ): Boolean

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


}