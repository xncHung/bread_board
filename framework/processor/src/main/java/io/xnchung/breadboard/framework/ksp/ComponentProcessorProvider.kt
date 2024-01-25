package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

class ComponentProcessorProvider : BaseSymbolProcessorProvider() {

    override fun realCreate(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ComponentProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}
