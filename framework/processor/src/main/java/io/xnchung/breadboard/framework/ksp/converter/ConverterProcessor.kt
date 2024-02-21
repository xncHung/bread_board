package io.xnchung.breadboard.framework.ksp.converter

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.framework.ksp.ClassNames
import io.xnchung.breadboard.framework.ksp.generateEmptyDi

class ConverterProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
    private val isInstrumentation: Boolean,
    val isApp: Boolean
) : SymbolProcessor {
    private val visitor = ConverterVisitor(logger, codeGenerator, isInstrumentation)

    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ksAnnotatedSequence = resolver.getSymbolsWithAnnotation(ConverterProvider::class.java.name)
            .filter {
                val isValid = it.validate()
                if (!isValid) {
                    logger.warn("Annotated converter function is not valid ${it.containingFile?.filePath}")
                }

                it is KSFunctionDeclaration &&
                        isValid &&
                        !it.isAnnotationPresent(Deprecated::class)
            }
        ksAnnotatedSequence
            .forEach {
                it.accept(visitor, Unit)
            }
        if (isApp && !isInstrumentation && !ksAnnotatedSequence.iterator().hasNext()) {
            generateEmptyDi(resolver, codeGenerator, ClassNames.CONVERTER_HOLDER_FQN)
        }
        return emptyList()
    }


}
