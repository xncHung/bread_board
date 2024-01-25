package io.xnchung.breadboard.framework.ksp.converter

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import io.xnchung.breadboard.framework.ksp.BaseSymbolProcessorProvider

class ConverterProcessorProvider : BaseSymbolProcessorProvider() {
    override fun realCreate(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ConverterProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}