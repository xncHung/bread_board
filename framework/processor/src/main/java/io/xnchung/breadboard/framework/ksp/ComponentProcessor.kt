package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
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
import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.DefaultQualifier
import io.xnchung.breadboard.annotations._ConverterHolder

/**
 */
@OptIn(KspExperimental::class)
class ComponentProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val ksAnnotatedSequence = resolver.getSymbolsWithAnnotation(Component::class.java.name)
            .filter {
                logger.warn("handle ksAnnotated = ${it::class} ,the content=$it")
                val isValid = it.validate()
                if (!isValid) {
                    logger.warn("this component is not valid ${it.containingFile?.filePath}")
                }
                (it is KSFunctionDeclaration || it is KSClassDeclaration) &&
                        isValid &&
                        !it.isAnnotationPresent(Deprecated::class)
            }.takeIf { it.iterator().hasNext() } ?: return emptyList()
//        collect the  converter factories
        val converterFactories = mutableMapOf<String, MutableList<KSFunctionDeclaration>>()
        for (ksDeclaration in resolver.getDeclarationsFromPackage(ClassNames.CONVERTERS_PACKAGE_NAME)) {
            when (ksDeclaration) {
//                builtin
                is KSFunctionDeclaration -> {
                    handleKsFuncDeclaration(ksDeclaration, resolver, converterFactories)
                }
// in the classpath
                is KSClassDeclaration -> {
                    logger.warn("declar=${ksDeclaration::class} $ksDeclaration")
                    ksDeclaration.annotations.forEach {
                        logger.warn("anno=$it")
                    }
                    val fqn = ksDeclaration.getAnnotationsByType(_ConverterHolder::class)
                        .firstOrNull()?.value ?: continue
                    logger.warn("fqn=$fqn")
                    resolver.getFunctionDeclarationsByName(resolver.getKSNameFromString(fqn), true).forEach {
                        logger.warn("declaration=$it")
                        handleKsFuncDeclaration(it, resolver, converterFactories)
                    }
                }

                else -> {}
            }
        }
//        in the source
        resolver.getSymbolsWithAnnotation(ConverterProvider::class.java.name).forEach {
            (it as? KSFunctionDeclaration)?.also {
                handleKsFuncDeclaration(it, resolver, converterFactories)
            }
        }
        converterFactories.forEach {
            it.value.sortBy {
                it.getAnnotationsByType(ConverterProvider::class).firstOrNull()?.order ?: Int.MAX_VALUE
            }
        }
        logger.warn(" converterFactories=$converterFactories")
        val visitor = ComponentVisitor(resolver, logger, codeGenerator, converterFactories)
        ksAnnotatedSequence
            .forEach { it.accept(visitor, Unit) }
        return emptyList()
    }

    private fun handleKsFuncDeclaration(
        ksDeclaration: KSFunctionDeclaration,
        resolver: Resolver,
        converterFactories: MutableMap<String, MutableList<KSFunctionDeclaration>>
    ) {
        val annotation =
            ksDeclaration.qualifierAnnotation()?.annotationType?.resolve()?.declaration?.qualifiedName?.asString()
                ?: DefaultQualifier::class.qualifiedName!!
        if (ksDeclaration.isValidConverterFactory) {
            converterFactories.getOrPut(annotation) { mutableListOf() }.add(ksDeclaration)
        } else {
            logger.error("converterFactory func:'${ksDeclaration.qualifiedName?.asString()}' is not valid,the function should has not any parameter,should be public and static,the return type should be function type,like:(String,Type)->T? ,for java it should be kotlin.jvm.functions.Function2")
        }
    }
}

