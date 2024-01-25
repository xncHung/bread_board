package io.xnchung.breadboard.framework.ksp.converter

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import io.xnchung.breadboard.framework.ksp.ClassNames

class ConverterVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {
    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val ksName = function.qualifiedName ?: return
        val fqn = ksName.asString()
        val name = fqn.replace(".", "_")
        val file = codeGenerator.createNewFile(
            Dependencies(aggregating = true),
            packageName = ClassNames.CONVERTERS_PACKAGE_NAME,
            fileName = "_$name"
        )
        file.use {
            it.write(convertFqnTemplate(function, ClassNames.CONVERTERS_PACKAGE_NAME).toByteArray())
        }
    }
}

private fun convertFqnTemplate(function: KSFunctionDeclaration, converterPackage: String): String {
    val ksName = function.qualifiedName ?: return ""
    val fqn = ksName.asString()
    val name = fqn.replace(".", "_")
    return """
package $converterPackage

import io.xnchung.breadboard.annotations._ConverterHolder

@_ConverterHolder("$fqn")
private class  _$name
""".trimIndent()
}