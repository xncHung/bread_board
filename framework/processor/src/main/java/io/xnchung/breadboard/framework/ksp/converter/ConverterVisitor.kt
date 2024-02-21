package io.xnchung.breadboard.framework.ksp.converter

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.DefaultQualifier
import io.xnchung.breadboard.framework.ProcessConstants
import io.xnchung.breadboard.framework.ksp.ClassNames
import io.xnchung.breadboard.framework.ksp.isValidConverterFactory
import io.xnchung.breadboard.framework.ksp.qualifierAnnotation
import io.xnchung.breadboard.framework.ksp.toFullPath

class ConverterVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val isInstrumentation: Boolean,
) : KSVisitorVoid() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (function.isValidConverterFactory) {
            val ksName = function.qualifiedName ?: return
            val fqn = ksName.asString()
            val name = fqn.replace(".", "_")
            val file = codeGenerator.createNewFile(
                dependencies = function.containingFile?.let {
                    Dependencies(aggregating = false, it)
                } ?: Dependencies(aggregating = false),
                packageName = ClassNames.CONVERTERS_PACKAGE_NAME,
                fileName = "_$name"
            )
            file.use {
                it.write(convertFqnTemplate(isInstrumentation, function, "_${name}Module").toByteArray())
            }
        } else {
            logger.error("converterFactory func:'${function.qualifiedName?.asString()}' is not valid,the function should has not any parameter,should be public and static,the return type should be function type,like:(String,Type)->T? ,for java it should be ${ProcessConstants.KOTLIN_JVM_FUNC_2},like Function2<String,Type,Bean>")
        }
    }
}

@OptIn(KspExperimental::class)
private fun convertFqnTemplate(
    isInstrumentation: Boolean,
    function: KSFunctionDeclaration,
    moduleName: String
): String {
    val qualifier =
        function.qualifierAnnotation()?.annotationType?.resolve()?.declaration?.qualifiedName?.asString()
            ?: DefaultQualifier::class.qualifiedName!!
    val converterProvider = function.getAnnotationsByType(ConverterProvider::class).first()
    val suspend = function.returnType?.resolve()?.declaration?.toFullPath() == "kotlin.coroutines.SuspendFunction2"
    val ksName = function.qualifiedName ?: return ""
    val fqn = ksName.asString()
    val name = fqn.replace(".", "_")
    val initStatement = if (suspend) {
        """
           ConverterHolder.Suspend("$qualifier",${converterProvider.order},${function.toFullPath()}()) 
        """.trimIndent()
    } else {
        """
        ConverterHolder.Normal("$qualifier",${converterProvider.order},${function.toFullPath()}()) 
        """.trimIndent()
    }
    val func = """
        fun ${ClassNames.GEN_FUNC_NAME_SUFFIX}${name}ConverterProvider(): ConverterHolder {
            return $initStatement
        }"""
    return if (isInstrumentation) {
        """
            package ${ClassNames.CONVERTERS_PACKAGE_NAME}
            $func
           """.trimIndent()
    } else {
        """
        package ${ClassNames.CONVERTERS_PACKAGE_NAME}
        
        import dagger.Module
        import dagger.Provides
        import dagger.hilt.InstallIn
        import dagger.hilt.components.SingletonComponent
        import dagger.multibindings.IntoSet
        
        @Module
        @InstallIn(SingletonComponent::class)
        class $moduleName {
        
            @Provides
            @IntoSet
            $func
        }
        """.trimIndent()
    }


}