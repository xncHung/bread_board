package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import io.xnchung.breadboard.annotations.CollectionType
import io.xnchung.breadboard.annotations.Component

internal class ComponentVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val target = classDeclaration.getAllSuperTypes().firstNotNullOfOrNull {
            val className = it.declaration.qualifiedName?.asString().orEmpty()
            logger.warn(className)
            when (className) {
                ClassNames.ACTIVITY -> {
                    "targetActivity<${classDeclaration.toFullPath()}>()"
                }

                ClassNames.FRAGMENT -> {
                    "targetFragment<${classDeclaration.toFullPath()}>()"
                }

                ClassNames.DIALOG -> {
                    "targetDialog<${classDeclaration.toFullPath()}>()"
                }

                ClassNames.VIEW -> {
                    "targetView<${classDeclaration.toFullPath()}>()"
                }

                else -> null
            }
        }
        requireNotNull(target) {
            "@Component only supports Fragments, Activities,Dialog,View or Composable functions"
        }

        createModule(classDeclaration, target)
    }

//    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//        require(
//            function.annotations.any { it.shortName.asString() == "Composable" } &&
//                function.parameters.isEmpty()
//        ) {
//            "@component must be a in a Composable function with empty parameters"
//        }
//
//        createModule(function, "targetComposable { ${function.toFullPath()}() }")
//    }

    @OptIn(KspExperimental::class)
    private fun createModule(componentFunction: KSDeclaration, target: String) {
        val packageName = ClassNames.SAMPLES_PACKAGE_NAME
        val componentFile = "_${componentFunction.toFullPath().replace(".", "_")}"
        val component = componentFunction.getAnnotationsByType(Component::class).first()
//        访问注解中的 KClass 存在 bug,https://github.com/google/ksp/issues/888
        val paramHolders = componentFunction.getKSAnnotationsByType(Component::class).first().getArg<Iterable<KSAnnotation>>("params")
            ?.mapIndexed { i: Int, ksAnnotation: KSAnnotation ->
                val param = component.params[i]
                val fqn = TypeTokenAnnotation(ksAnnotation).parseToFqn().let {
                    when (param.collectionType) {
                        CollectionType.None -> it
                        CollectionType.Array -> "${Array::class.qualifiedName}<$it>"
                        CollectionType.ArrayList -> "${ArrayList::class.qualifiedName}<$it>"
                        CollectionType.List -> "${List::class.qualifiedName}<$it>"
                    }
                }
                val qualifier = ksAnnotation.getArg<KSType>("qualifier")!!.declaration.toFullPath()
                val defaultVaule = param.defaultValue.takeIf { it.isNotBlank() }?.let {
                    """
                    ""${'"'}${param.defaultValue}""${'"'}.trimIndent()
                    """.trimIndent()
                } ?: when (param.collectionType) {
                    CollectionType.None -> "\"\""
                    CollectionType.Array,
                    CollectionType.ArrayList,
                    CollectionType.List -> "\"[]\""
                }
                """
                   ParamHolder<$fqn>(key="${param.key}",defaultValue=$defaultVaule,qualifier="$qualifier") 
                """.trimIndent()
            }?.toTypedArray() ?: emptyArray()
        val componentSource = component.sourcePath.ifBlank { getFilePath(componentFunction) }


        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(
                aggregating = true,
            ),
            packageName = packageName,
            fileName = "${componentFile}Component"
        )
        file.use { stream ->
            stream.write(
                componentTemplate(
                    componentFile = componentFile,
                    componentPackage = packageName,
                    componentName = component.name,
                    componentDescription = component.description,
                    componentTags = component.tags,
                    componentDocs = component.documentation,
                    componentSource = componentSource,
                    componentOwners = component.owners,
                    componentTarget = target,
                    componentRoute = component.routes,
                    paramHolders = paramHolders
                ).toByteArray()
            )
        }
    }


    private fun getFilePath(declaration: KSDeclaration): String {
        return (declaration.location as? FileLocation)?.filePath.orEmpty()
    }
}

internal fun KSDeclaration.toFullPath() =
    qualifiedName?.asString() ?: "${packageName.asString()}.${simpleName.asString()}"

private fun TypeTokenAnnotation.parseToFqn(): String {
    val kClass = rawType?.declaration?.toFullPath()
    val rawTypeFqn = requireNotNull(kClass)
    val typeArgsFqn = if (!typeArgs.isNullOrEmpty()) {
        typeArgs.joinToString(prefix = "<", postfix = ">") { it.parseToFqn() }
    } else ""
    return "$rawTypeFqn$typeArgsFqn"
}

private fun componentTemplate(
    componentFile: String,
    componentPackage: String,
    componentName: String,
    componentDescription: String,
    componentTags: Array<String>,
    componentDocs: String,
    componentSource: String,
    componentOwners: Array<String>,
    componentTarget: String,
    componentRoute: Array<String>,
    paramHolders: Array<String>,
) = """
package $componentPackage

import io.xnchung.breadboard.model.*

fun provide${componentFile}Component(): Sample {
    return Sample(
      name = ${componentName.quote()},
      description = ${componentDescription.quote()},
      tags =  listOf(${componentTags.joinToString(",") { "\"$it\"" }}),
      documentation = ${componentDocs.quote()},
      owners = listOf(${componentOwners.joinToString(",") { "\"$it\"" }}),
      target = $componentTarget,
      routes=  listOf(${componentRoute.joinToString(",") { "\"$it\"" }}),
      source =  "$componentSource",
      paramHolders = listOf(${paramHolders.joinToString(",\n") { it }}),
    )
}
""".trimIndent()

private class TypeTokenAnnotation(annotation: KSAnnotation) {
    val rawType = annotation.getArg<KSType>("rawType")
    val typeArgs = annotation.getArg<Iterable<KSAnnotation>>("typeArgs")?.map {
        TypeTokenAnnotation(it)
    }

}