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
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.Converter

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
        resolver.getSymbolsWithAnnotation(Converter::class.java.name).forEach {
            logger.warn("conveter:${it::class}, $it")
        }
        resolver.getSymbolsWithAnnotation(Component::class.java.name)
            .filter {
                logger.warn("handle ksAnnotated = ${it::class} ,the content=$it")
                val isValid = it.validate { first, second ->
                    logger.warn("first= $first,second=${second::class} $second")
                    !(first is KSClassDeclaration && second is KSPropertyDeclaration)
                    false
                }
                logger.warn("the validate result=$isValid")
                if (!isValid) {
                    logger.warn("this component is not valid ${it.containingFile?.filePath}")
                }
                (it is KSFunctionDeclaration || it is KSClassDeclaration) &&
                        isValid &&
                        !it.isAnnotationPresent(Deprecated::class)
            }
            .forEach { it.accept(visitor, Unit) }

        return emptyList()
    }
}
