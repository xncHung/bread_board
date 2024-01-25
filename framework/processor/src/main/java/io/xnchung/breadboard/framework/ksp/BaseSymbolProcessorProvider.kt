package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

abstract class BaseSymbolProcessorProvider : SymbolProcessorProvider {
    final override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val enable = environment.options.getOrDefault("io.xnchung.breadboard.codegen.enable", "false").toBooleanStrictOrNull() == true
        return if (enable) {
            realCreate(environment)
        } else {
            environment.logger.warn("The breadboard processor has been disabled because the variant being assembled is not debuggable")
            NoOpSymbolProcessor()
        }
    }

    abstract fun realCreate(environment: SymbolProcessorEnvironment): SymbolProcessor
}