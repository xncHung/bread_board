package io.xnchung.breadboard.framework.apt

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import io.xnchung.breadboard.annotations.CollectionType
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.Param
import io.xnchung.breadboard.annotations.TypeToken
import io.xnchung.breadboard.framework.ProcessConstants
import io.xnchung.breadboard.framework.ksp.ClassNames
import io.xnchung.breadboard.framework.ksp.ITypeTokenAnnotation
import io.xnchung.breadboard.framework.ksp.parseToFqn
import io.xnchung.breadboard.framework.ksp.quoteJava
import java.io.IOException
import java.util.Collections
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.PrimitiveType
import javax.tools.Diagnostic


@SupportedAnnotationTypes("io.xnchung.breadboard.annotations.Component")
@SupportedOptions(value = [ProcessConstants.ARG_CODE_GEN_ENABLE, ProcessConstants.ARG_CODE_GEN_STRATEGY, ProcessConstants.ARG_CODE_GEN_IS_APP])
class ComponentAptProcessor : BaseComponentAptProcessor() {
    override fun realProcess(
        annotations: MutableSet<out TypeElement>,
        environment: RoundEnvironment,
        isInstrumentation: Boolean,
        isApp: Boolean
    ): Boolean {
        if (environment.processingOver()) {
            return true
        }
        val components = environment.getElementsAnnotatedWith(Component::class.java).filter { it in environment.rootElements }
        val typeUtils = processingEnv.typeUtils
        val elementUtils = processingEnv.elementUtils
        if (isApp && !isInstrumentation) {
            generateDiActivityFoJava(processingEnv)
            generateEmptySetDi(processingEnv, ClassNames.SAMPLE_CONTRACT_FQN)
        }
        components.forEach {
            processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "element=${it.javaClass} ${it.simpleName}")
            val targetStatementArgs = when (it.kind) {
                ElementKind.CLASS -> {
                    when {
                        typeUtils.isSubtype(it.asType(), elementUtils.getTypeElement(ClassNames.ACTIVITY).asType()) -> {
                            "${typeRefLiteral}.targetActivity(${typeRefLiteral}.class)" to listOf(
                                ClassName.get("io.xnchung.breadboard.model", "TargetKt"),
                                ClassName.get(it.asType())
                            )
                        }

                        else -> "" to emptyList()
                    }
                }

                ElementKind.METHOD -> "" to emptyList()
                else -> {
                    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "not supported breadboard component")
                    null
                }
            }
            val (statement, args) = requireNotNull(targetStatementArgs) {
                "@Component only supports Fragments, Activities,Dialog,View or Composable functions"
            }
            createProviderClass(it, statement, isInstrumentation, *args.toTypedArray())
        }
        return true
    }

    private fun createProviderClass(element: Element, componentTarget: String, isInstrumentation: Boolean, vararg args: Any?) {
        val component = element.getAnnotation(Component::class.java)
        val annotationMirror = element.annotationMirrors.first { it.annotationType.asElement().toFullPath() == Component::class.java.canonicalName }
        val annotationValue = annotationMirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("params") }?.value
        val paramsAnnotationMirrors = annotationValue?.value as List<*>
        val paramHolders = component.params.mapIndexed { i: Int, param: Param ->
            val mirror = paramsAnnotationMirrors[i] as AnnotationMirror
            val fqn = ParamTypeTokenAnnotation(param, mirror).parseToFqn().let {
                when (param.collectionType) {
                    CollectionType.None -> it
                    CollectionType.Array -> "$it[]"
                    CollectionType.ArrayList -> "${ArrayList::class.qualifiedName}<$it>"
                    CollectionType.List -> "${List::class.qualifiedName}<$it>"
                }
            }
            val qualifier =
                mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("qualifier") }?.value?.value?.toString().orEmpty()

            val defaultVaule = param.defaultValue.takeIf { it.isNotBlank() }?.let {
                param.defaultValue
            } ?: when (param.collectionType) {
                CollectionType.None -> ""
                CollectionType.Array,
                CollectionType.ArrayList,
                CollectionType.List -> "[]"
            }
            """
                   new ParamHolder<$fqn>("${param.key}","${param.desc.quoteJava()}","${defaultVaule.quoteJava()}","$qualifier") 
                """.trimIndent()
        }.toTypedArray()


        val initStatement = """
             ${typeRefLiteral}<${typeRefLiteral}<?>> params = new ${typeRefLiteral}<>();
             ${paramHolders.joinToString("\n") { "params.add($it);" }}
             
             ${typeRefLiteral}<String> tags = new ${typeRefLiteral}<>();
             ${component.tags.joinToString("\n") { "tags.add(\"$it\");" }}
             
             ${typeRefLiteral}<String> owners = new ${typeRefLiteral}<>();
             ${component.owners.joinToString("\n") { "owners.add(\"$it\");" }}
            
             ${typeRefLiteral}<String> routes = new ${typeRefLiteral}<>();
             ${component.routes.joinToString("\n") { "routes.add(\"$it\");" }}
            
        """.trimIndent()
        val fullPath = element.toFullPath()
        val returnStatement = """
            return new ${typeRefLiteral}(
            "$fullPath",
             "${component.name.quoteJava()}",
             "${component.description.quoteJava()}",
             tags,
             "${component.documentation.quoteJava()}",
             owners,
             target,
              routes,
              "${component.sourcePath}",
             params
            )
        """.trimIndent()
        val fileName = "_breadboard" + fullPath.replace(".", "_") + "Generated"
        val targetClassName = ClassName.get("io.xnchung.breadboard.model", "Target")
        val generatedClass: TypeSpec = TypeSpec.classBuilder(fileName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("${ClassNames.GEN_FUNC_NAME_SUFFIX}${fileName}Component")
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .returns(ClassName.bestGuess(ClassNames.SAMPLE_CONTRACT_FQN))
                    .addCode(
                        initStatement,
                        List::class.java,
                        ClassName.get("io.xnchung.breadboard.model", "ParamHolder"),
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ArrayList::class.java,
                        ArrayList::class.java,
                    )
                    .addStatement("${typeRefLiteral} target = $componentTarget", targetClassName, *args)
                    .addStatement(returnStatement, ClassName.get("io.xnchung.breadboard.model", "Sample"))
                    .run {
                        if (!isInstrumentation) {
                            addAnnotation(ClassName.get("dagger", "Provides"))
                            addAnnotation(ClassName.get("dagger.multibindings", "IntoSet"))
                        } else this
                    }
                    .build()
            ).run {
                if (!isInstrumentation) {
                    addModuleAnnotation()
                } else this
            }
            .build()

        val javaFile: JavaFile = JavaFile.builder(ClassNames.SAMPLES_PACKAGE_NAME, generatedClass)
            .build()

        try {
            javaFile.writeTo(processingEnv.filer)
        } catch (e: IOException) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.ERROR,
                "breadboard >>>Could not write generated file: " + e.message
            )
        }
    }

}

internal fun TypeSpec.Builder.addModuleAnnotation(): TypeSpec.Builder {
    addAnnotation(ClassName.get("dagger", "Module"))
    val installIn = ClassName.get("dagger.hilt", "InstallIn")
    val singletonComponent = ClassName.get("dagger.hilt.components", "SingletonComponent")
    return addAnnotation(AnnotationSpec.builder(installIn).addMember("value", "${typeRefLiteral}.class", singletonComponent).build())
}


private fun AnnotationMirror.rawTypeFqnFromMirror() =
    (elementValues.entries.first { it.key.simpleName.contentEquals("rawType") }.value as AnnotationValue).value.let {
        if (it is PrimitiveType) ClassName.get(it).box() else it
    }.toString().replace("java.lang.", "")

internal class ParamTypeTokenAnnotation(annotation: Param, mirror: AnnotationMirror) : ITypeTokenAnnotation {
    override val rawType = mirror.rawTypeFqnFromMirror()


    override val typeArgs = run {
        val list = mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("typeArgs") }?.value?.value as? List<*>
        annotation.typeArgs.mapIndexed { index, it ->
            val annotationMirror = list?.get(index) as AnnotationMirror
            TypeTokenAnnotation(it, annotationMirror)
        }
    }

}

internal class TypeTokenAnnotation(annotation: TypeToken, mirror: AnnotationMirror) : ITypeTokenAnnotation {
    override val rawType = mirror.rawTypeFqnFromMirror()
    override val typeArgs = run {
        val list = mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("typeArgs") }?.value?.value as? List<*>
        annotation.typeArgs.mapIndexed { index, it ->
            TypeTokenAnnotation(it, list?.get(index) as AnnotationMirror)
        }
    }
}

internal const val typeRefLiteral = "${'$'}T"

internal fun generateDiActivityFoJava(processingEnv: ProcessingEnvironment) {

    val diActivityFqn = ClassNames.DI_ACTIVITY_FQN
    if (processingEnv.elementUtils.getTypeElement(diActivityFqn) != null) {
        return
    }
    val packageName = diActivityFqn.substringBeforeLast(".")
    val className = diActivityFqn.substringAfterLast(".")
    val superClass = ClassName.get(packageName, "BreadBoardLauncherActivityWithDIDeclaration")
    val entryPointAnnotation = ClassName.get("dagger.hilt.android", "AndroidEntryPoint")
    val typeSpec = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .superclass(superClass)
        .addAnnotation(entryPointAnnotation)
        .build()
    val javaFile = JavaFile.builder(packageName, typeSpec).build()
    try {
        javaFile.writeTo(processingEnv.filer)
    } catch (e: IOException) {
        processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "breadboard >>>Could not write generated file: " + e.message
        )
    }
}

internal fun generateEmptySetDi(processingEnv: ProcessingEnvironment, targetFqn: String) {
    val packageName = ClassNames.DI_EMPTY_PACKAGE_NAME
    val targetPkgName = targetFqn.substringBeforeLast(".")
    val className = targetFqn.substringAfterLast(".")
    val generateClassName = "${className}Module"
    if (processingEnv.elementUtils.getTypeElement("$packageName.$generateClassName") != null) {
        return
    }
    if (processingEnv.elementUtils.getTypeElement(targetFqn) == null) {
        return
    }
    val typeSpec = TypeSpec.classBuilder(generateClassName)
        .addModifiers(Modifier.PUBLIC)
        .addModuleAnnotation()
        .addMethod(
            MethodSpec.methodBuilder("provideEmpty")
                .addAnnotation(ClassName.get("dagger", "Provides"))
                .addAnnotation(ClassName.get("dagger.multibindings", "ElementsIntoSet"))
                .returns(ParameterizedTypeName.get(ClassName.get(Set::class.java), ClassName.get(targetPkgName, className)))
                .addStatement("return $typeRefLiteral.EMPTY_SET", ClassName.get(Collections::class.java))
                .build()
        )
        .build()
    val javaFile = JavaFile.builder(packageName, typeSpec).build()
    try {
        javaFile.writeTo(processingEnv.filer)
    } catch (e: IOException) {
        processingEnv.messager.printMessage(
            Diagnostic.Kind.ERROR,
            "breadboard >>>Could not write generated file: " + e.message
        )
    }
}