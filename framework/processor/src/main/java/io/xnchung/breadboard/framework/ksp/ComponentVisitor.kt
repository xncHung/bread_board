package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getKotlinClassByName
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import io.xnchung.breadboard.annotations.CollectionType
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.EmptyParamProvider
import io.xnchung.breadboard.annotations.Ignore
import io.xnchung.breadboard.annotations.ParamProvider
import io.xnchung.breadboard.framework.ProcessConstants

internal class ComponentVisitor(
    private val logger: KSPLogger,
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val isInstrumentation: Boolean,
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val target = classDeclaration.generateTargetCodeByClassDeclaration(
            handleActivity = {
                "targetActivity<${it.toFullPath()}>()"
            },
            handleFragment = {
                "targetFragment<${it.toFullPath()}>()"
            },
            handleDialog = {
                "targetDialog<${it.toFullPath()}>()"
            },
            handleView = {
                "targetView<${it.toFullPath()}>()"
            },
            defaultAction = { null }

        )
        requireNotNull(target) {
            "@Component only supports Fragments, Activities,Dialog,View or Composable functions"
        }

        createModule(classDeclaration, target)
    }

    private fun KSClassDeclaration.generateTargetCodeByClassDeclaration(
        handleActivity: KSType.(KSClassDeclaration) -> String?,
        handleFragment: KSType.(KSClassDeclaration) -> String?,
        handleDialog: KSType.(KSClassDeclaration) -> String?,
        handleView: KSType.(KSClassDeclaration) -> String?,
        defaultAction: KSType.(KSClassDeclaration) -> String?,
    ) = getAllSuperTypes().firstNotNullOfOrNull {
        when (it.declaration.qualifiedName?.asString().orEmpty()) {
            ClassNames.ACTIVITY -> it.handleActivity(this)

            ClassNames.FRAGMENT -> it.handleFragment(this)

            ClassNames.DIALOG -> it.handleDialog(this)

            ClassNames.VIEW -> it.handleView(this)

            else -> it.defaultAction(this)
        }
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        super.visitFunctionDeclaration(function, data)
        val fullPath = when {
            function.isStaticOrTopLevelOrObject -> function.toFullPath()
            (function.parentDeclaration as? KSClassDeclaration)?.getConstructors()
                ?.singleOrNull { !it.isPrivate() && it.parameters.isEmpty() } != null -> {
                "${(function.parentDeclaration as KSClassDeclaration).toFullPath()}().${function.simpleName.asString()}"
            }

            else -> {
                error("Component function must be top level or static or in companionObject or object or the component class must has an empty constructor")
            }
        }
        val paramString = function.parameters.filterIgnore().joinToString(",\n", "\n") {
            val name = it.name?.asString().orEmpty()
            val resolve = it.type.resolve()
            val nullable = resolve.nullability == Nullability.NULLABLE
            """                     $name = this["${it.parameterKey()}"] ${if (nullable) "as?" else "as"} ${resolve.fqnForDeclaration()}"""
        }
        val isComposable = function.annotations.any { it.annotationType.resolve().declaration.toFullPath() == ClassNames.COMPOSABLE }

        val target = if (isComposable) {
            """targetComposable {
                    $fullPath($paramString
                    )
                }"""
        } else {
            function.returnType?.resolve()?.run {
                val ksClassDeclaration = declaration as KSClassDeclaration
                ksClassDeclaration.generateTargetCodeByClassDeclaration(
                    handleActivity = {
                        """targetCommonFunc {
                    $fullPath($paramString
                    )
                }"""
                    },
                    handleFragment = {
                        """targetFragmentBuilder {
                    $fullPath($paramString
                    )
                }"""
                    },
                    handleDialog = {
                        """targetDialogBuilder {
                    $fullPath($paramString
                    )
                }"""
                    },
                    handleView = {
                        """targetViewBuilder {
                    $fullPath($paramString
                    )
                }"""
                    },
                    defaultAction = {
                        """targetCommonFunc {
                    $fullPath($paramString
                    )
                }"""
                    }
                )
            }
        }
        requireNotNull(target) {
            "@Component only supports Fragments, Activities,Dialog,View or Composable functions"
        }
        createModule(function, target)
    }

    private fun KSValueParameter.parameterKey(
    ): String? = if ((type.resolve().declaration as KSClassDeclaration).qualifiedName?.asString() == ClassNames.CONTEXT) {
        ProcessConstants.PARAMS_MAP_CONTEXT_KEY
    } else {
        name?.asString()
    }

    @OptIn(KspExperimental::class)
    private fun createModule(ksDeclaration: KSDeclaration, target: String) {
        val packageName = ksDeclaration.packageName.asString()
        val declarationFullPath = ksDeclaration.toFullPath() + when (ksDeclaration) {
            is KSFunctionDeclaration -> {
//                取参数的sampleName 也有冲突的可能，但概率很低，如果有此情况，应该由开发者修改代码
                ksDeclaration.parameters.joinToString("_", "_") {
                    val ksType = it.type.resolve()
                    simpleNameForDeclaration(ksType)

                }
            }

            else -> ""
        }
        val component = ksDeclaration.getAnnotationsByType(Component::class).first()
        val paramHolders = if (ksDeclaration is KSFunctionDeclaration) {
            getParamsFromFuncParameters(ksDeclaration)
        } else {
            getParamsHolderFromAnnotation(ksDeclaration, component)
        }
        val componentSource = component.sourcePath.ifBlank { getFilePath(ksDeclaration) }


        val dependencies = ksDeclaration.containingFile?.let {
            Dependencies(aggregating = false, it)
        } ?: Dependencies(aggregating = false)
        val componentFile = declarationFullPath.replace(".", "_")
        val file = codeGenerator.createNewFile(
            dependencies = dependencies, packageName = packageName, fileName = "${ClassNames.GEN_FILE_NAME_PREFIX}${componentFile}Component"
        )
        file.use { stream ->
            stream.write(
                componentTemplate(
                    isInstrumentation,
                    id = declarationFullPath,
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

    private fun simpleNameForDeclaration(ksType: KSType): String {
        val declaration = ksType.declaration
        val simpleName = declaration.simpleName.asString()
        return if (declaration.toFullPath() == Array::class.qualifiedName) {
            val parameter = ksType.arguments.first().type?.resolve()
            "${parameter?.let { simpleNameForDeclaration(it) }}$simpleName"
        } else {
            simpleName
        }
    }

    private fun KSType.fqnForDeclaration(): String {
        val declaration = declaration
        val fullPath = declaration.toFullPath().replace("^kotlin\\.".toRegex(), "")
        return if (arguments.isNotEmpty()) {
            fullPath + arguments.joinToString(prefix = "<", postfix = ">") {
                it.type?.resolve()?.fqnForDeclaration() ?: "*"
            }
        } else {
            fullPath
        }
    }

    private fun getParamsFromFuncParameters(funcDeclaration: KSFunctionDeclaration) = funcDeclaration.parameters
        .filterIgnore()
        .map {
            // TODO: get desc from comment
            val parameterKey = it.parameterKey()
            val fqn = it.type.resolve().fqnForDeclaration()
            val declaration = it.getKSAnnotationsByType(ParamProvider::class).firstOrNull()?.getArg<KSType>("provider")?.declaration
            val providerFqn =
                (declaration as? KSClassDeclaration)?.run {
                    toFullPath().let { if (classKind == ClassKind.OBJECT) it else "$it()" }
                }
            """
                       ParamHolder<$fqn>(
                            key = "$parameterKey",
                            desc = "",
                            defaultValue = "",
                            qualifier = "",
                            provider = ${providerFqn ?: "null"}
                       )"""
        }.toList().toTypedArray()

    private fun getParamsHolderFromAnnotation(
        ksDeclaration: KSDeclaration,
        component: Component
    ): Array<String> {
        //        访问注解中的 KClass 存在 bug,https://github.com/google/ksp/issues/888
        return ksDeclaration.getKSAnnotationsByType(Component::class).first().getArg<Iterable<KSAnnotation>>("params")
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
                val qualifier = ksAnnotation.getArg<KSType>("qualifier")!!.declaration.toFullPath()
                val providerFqn =
                    (ksAnnotation.getArg<KSType>("provider")?.declaration as? KSClassDeclaration)?.run {
                        toFullPath().takeIf { it != EmptyParamProvider::class.qualifiedName }
                            ?.let {
                                if (classKind == ClassKind.OBJECT) it else "$it()"
                            }
                    }
                val defaultVaule = param.defaultValue.takeIf { it.isNotBlank() }?.let {
                    """""${'"'}${param.defaultValue.lines().joinToString("\n") { "           $it" }}""${'"'}.trimIndent()"""
                } ?: when (param.collectionType) {
                    CollectionType.None -> "\"\""
                    CollectionType.Array, CollectionType.ArrayList, CollectionType.List -> "\"[]\""
                }
                """
                           ParamHolder<$fqn>(
                                key="${param.key}",
                                desc="${param.desc}",
                                defaultValue=$defaultVaule,
                                qualifier="$qualifier",
                                provider = ${providerFqn ?: "null"}
                           )"""
            }?.toTypedArray() ?: emptyArray()
    }


    private fun getFilePath(declaration: KSDeclaration): String {
        return (declaration.location as? FileLocation)?.filePath.orEmpty()
    }
}

internal fun KSDeclaration.toFullPath() = qualifiedName?.asString() ?: "${packageName.asString()}.${simpleName.asString()}"

internal fun ITypeTokenAnnotation.parseToFqn(forJava: Boolean = false): String {
    val rawTypeFqn = requireNotNull(rawType)
    val typeArgs1 = typeArgs
    val typeArgsFqn = if (!typeArgs1.isNullOrEmpty()) {
        typeArgs1.joinToString(prefix = "<", postfix = ">") { it.parseToFqn(forJava) }
    } else ""
    return "$rawTypeFqn$typeArgsFqn".let {
        if (forJava) {
            it
        } else {
            if (nullable) "$it?" else it
        }
    }
}

private fun componentTemplate(
    isInstrumentation: Boolean,
    id: String,
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
): String {
    val funcName = if (isInstrumentation) {
        "${ClassNames.GEN_FUNC_NAME_PREFIX}${componentFile}Component"
    } else {
        "_provide_component"
    }
    val func =
        """fun $funcName(): SampleContract {
            return Sample(
              id="$id",
              name = ${componentName.quote()},
              description = ${componentDescription.quote()},
              tags =  listOf(${componentTags.joinToString(",") { "\"$it\"" }}),
              documentation = ${componentDocs.quote()},
              owners = listOf(${componentOwners.joinToString(",") { "\"$it\"" }}),
              target = $componentTarget,
              routes=  listOf(${componentRoute.joinToString(",") { "\"$it\"" }}),
              source =  "$componentSource",
              paramHolders = listOf(${
            paramHolders.joinToString("") {
                """
                  $it,"""
            }
        }),
            )
        }
        """
    return if (isInstrumentation) {
        """
        package $componentPackage
        
        import io.xnchung.breadboard.model.*
        
        $func
        """.trimIndent()
    } else {
        """
        package $componentPackage
        
        import io.xnchung.breadboard.model.*
        
        import dagger.Module
        import dagger.Provides
        import dagger.hilt.InstallIn
        import dagger.hilt.components.SingletonComponent
        import dagger.multibindings.IntoSet
        
        @Module
        @InstallIn(SingletonComponent::class)
        object ${componentFile}Module {
        
            @Provides
            @IntoSet
            $func
        }
        """.trimIndent()
    }
}

private class TypeTokenAnnotation(annotation: KSAnnotation, resolver: Resolver) : ITypeTokenAnnotation {
    @OptIn(KspExperimental::class)
    override val rawType = annotation.getArg<KSType>("rawType")?.declaration?.qualifiedName?.let { resolver.getKotlinClassByName(it) }?.toFullPath()
    override val typeArgs = annotation.getArg<Iterable<KSAnnotation>>("typeArgs")?.map {
        TypeTokenAnnotation(it, resolver)
    }
    override val nullable: Boolean = annotation.getArg<Boolean>("nullable") == true
}

fun List<KSValueParameter>.filterIgnore() = asSequence().filterNot { it.hasDefault && it.getKSAnnotationsByType(Ignore::class).iterator().hasNext() }