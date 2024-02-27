package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.xnchung.breadboard.framework.ProcessConstants

abstract class BaseSymbolProcessorProvider : SymbolProcessorProvider {
    final override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val enable = environment.options.getOrDefault(ProcessConstants.ARG_CODE_GEN_ENABLE, "false").toBooleanStrictOrNull() == true
        val isApp = environment.options[ProcessConstants.ARG_CODE_GEN_IS_APP]?.toBoolean() == true
        val isInstrumentation = environment.options.getOrDefault(
            ProcessConstants.ARG_CODE_GEN_STRATEGY,
            ProcessConstants.ARG_VALUE_INSTRUMENTATION
        ) == ProcessConstants.ARG_VALUE_INSTRUMENTATION
        return if (enable) {
            realCreate(environment, isInstrumentation, isApp)
        } else {
            environment.logger.info("The breadboard processor has been disabled because the variant being assembled is not debuggable")
            NoOpSymbolProcessor()
        }
    }

    abstract fun realCreate(environment: SymbolProcessorEnvironment, isInstrumentation: Boolean, isApp: Boolean): SymbolProcessor
}