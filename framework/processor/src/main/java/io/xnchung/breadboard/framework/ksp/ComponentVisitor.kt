package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
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
    private val resolver: Resolver,
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


    @OptIn(KspExperimental::class)
    private fun createModule(componentFunction: KSDeclaration, target: String) {
        val packageName = ClassNames.SAMPLES_PACKAGE_NAME
        val componentFile = "_${componentFunction.toFullPath().replace(".", "_")}"
        val component = componentFunction.getAnnotationsByType(Component::class).first()
//        访问注解中的 KClass 存在 bug,https://github.com/google/ksp/issues/888
        val paramHolders = componentFunction.getKSAnnotationsByType(Component::class).first().getArg<Iterable<KSAnnotation>>("params")
            ?.mapIndexed { i: Int, ksAnnotation: KSAnnotation ->
                val param = component.params[i]
                val fqn = TypeTokenAnnotation(ksAnnotation, resolver).parseToFqn().let {
                    when (param.collectionType) {
                        CollectionType.None -> it
                        CollectionType.Array -> "${Array::class.qualifiedName}<$it>"
                        CollectionType.ArrayList -> "${ArrayList::class.qualifiedName}<$it>"
                        CollectionType.List -> "${List::class.qualifiedName}<$it>"
                    }
                }
                val ksNameFromString = resolver.getKSNameFromString(fqn)
                val mapJavaNameToKotlin = resolver.mapJavaNameToKotlin(ksNameFromString)
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

internal fun ITypeTokenAnnotation.parseToFqn(forJava: Boolean = false): String {
    val rawTypeFqn = requireNotNull(rawType)
    val typeArgs1 = typeArgs
    val typeArgsFqn = if (!typeArgs1.isNullOrEmpty()) {
        typeArgs1.joinToString(prefix = "<", postfix = ">") { it.parseToFqn() }
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

fun ${ClassNames.GEN_FUNC_NAME_SUFFIX}${componentFile}Component(): SampleContract {
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

private class TypeTokenAnnotation(annotation: KSAnnotation, resolver: Resolver) : ITypeTokenAnnotation {
    @OptIn(KspExperimental::class)
    override val rawType = annotation.getArg<KSType>("rawType")?.declaration?.qualifiedName?.let { resolver.getKotlinClassByName(it) }?.toFullPath()
    override val typeArgs = annotation.getArg<Iterable<KSAnnotation>>("typeArgs")?.map {
        TypeTokenAnnotation(it, resolver)
    }

}