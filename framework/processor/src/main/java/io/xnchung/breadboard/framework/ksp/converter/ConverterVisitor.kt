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
import io.xnchung.breadboard.framework.ksp.ClassNames
import io.xnchung.breadboard.framework.ksp.isValidConverterFactory
import io.xnchung.breadboard.framework.ksp.qualifierAnnotation
import io.xnchung.breadboard.framework.ksp.toFullPath

class ConverterVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        if (function.isValidConverterFactory) {
            val ksName = function.qualifiedName ?: return
            val fqn = ksName.asString()
            val name = fqn.replace(".", "_")
            val file = codeGenerator.createNewFile(
                Dependencies(aggregating = true),
                packageName = ClassNames.CONVERTERS_PACKAGE_NAME,
                fileName = "_$name"
            )
            file.use {
                it.write(convertFqnTemplate(function).toByteArray())
            }
        } else {
            logger.error("converterFactory func:'${function.qualifiedName?.asString()}' is not valid,the function should has not any parameter,should be public and static,the return type should be function type,like:(String,Type)->T? ,for java it should be kotlin.jvm.functions.Function2")
        }
    }
}

@OptIn(KspExperimental::class)
private fun convertFqnTemplate(
    function: KSFunctionDeclaration
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
    return """
package ${ClassNames.CONVERTERS_PACKAGE_NAME}

  fun _provide_${name}_converterProvider(): ConverterHolder {
    return $initStatement
  }
""".trimIndent()


}