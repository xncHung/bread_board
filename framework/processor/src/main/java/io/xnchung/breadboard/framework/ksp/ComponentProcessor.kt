package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.validate
import io.xnchung.breadboard.annotations.Component

/**
 */
@OptIn(KspExperimental::class)
class ComponentProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    private val visitor = ComponentVisitor(logger, codeGenerator)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ksAnnotatedSequence = resolver.getSymbolsWithAnnotation(Component::class.java.name)
            .filter {
                logger.warn("handle ksAnnotated = ${it::class} isclass=${it is KSClassDeclaration},isFunction=${it is KSFunctionDeclaration},the content=$it")
                val isValid = it.validate()
                if (!isValid) {
                    logger.warn("this component is not valid ${it.containingFile?.filePath}")
                }
                (it is KSFunctionDeclaration || it is KSClassDeclaration) &&
                        isValid &&
                        !it.isAnnotationPresent(Deprecated::class)
            }.takeIf { it.iterator().hasNext() } ?: return emptyList()
        ksAnnotatedSequence
            .forEach { it.accept(visitor, Unit) }
        return emptyList()
    }


}

