package io.xnchung.breadboard.framework.apt

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import io.xnchung.breadboard.annotations.CollectionType
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.Ignore
import io.xnchung.breadboard.annotations.Param
import io.xnchung.breadboard.annotations.ParamProvider
import io.xnchung.breadboard.framework.ProcessConstants
import io.xnchung.breadboard.framework.ksp.ClassNames
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
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


@SupportedAnnotationTypes("io.xnchung.breadboard.annotations.Component")
@SupportedOptions(
    value = [
        ProcessConstants.ARG_CODE_GEN_ENABLE,
        ProcessConstants.ARG_CODE_GEN_STRATEGY,
        ProcessConstants.ARG_CODE_GEN_IS_APP,
        ProcessConstants.ARG_CODE_GEN_PROJECT_ROOT_PATH,
        ProcessConstants.ARG_CODE_GEN_PROJECT_PATH,
        ProcessConstants.ARG_CODE_GEN_PROJECT_NAME,

    ]
)
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
        val components = environment.getElementsAnnotatedWith(Component::class.java)
        val typeUtils = processingEnv.typeUtils
        val elementUtils = processingEnv.elementUtils
        if (isApp && !isInstrumentation) {
            generateDiActivityFoJava(processingEnv)
            generateEmptySetDi(processingEnv, ClassNames.SAMPLE_CONTRACT_FQN)
        }
        val targetKt = ClassName.get("io.xnchung.breadboard.model", "TargetKt")
        components.forEach {
            val targetStatementArgs = when (it.kind) {
                ElementKind.CLASS -> {
                    val asType = it.asType()
                    val commonArgs = listOf(
                        targetKt,
                        ClassName.get(asType)
                    )
                    getTargetJavaPoetArgs(
                        typeUtils = typeUtils,
                        type = asType,
                        elementUtils = elementUtils,
                        handleActivity = {
                            "${typeRefLiteral}.targetActivity(${typeRefLiteral}.class)" to commonArgs
                        },
                        handleFragment = {
                            "${typeRefLiteral}.targetFragment(${typeRefLiteral}.class)" to commonArgs
                        },
                        handleDialog = {
                            "${typeRefLiteral}.targetDialog(${typeRefLiteral}.class)" to commonArgs
                        },
                        handleView = {
                            "${typeRefLiteral}.targetView(${typeRefLiteral}.class)" to commonArgs
                        },
                        default = { null }
                    )
                }

                ElementKind.METHOD -> {
                    val element = it as ExecutableElement
                    val returnType = element.returnType
                    val targetArg = listOf(targetKt)
                    val isComposable = element.annotationMirrors.any { it.annotationType.asElement().toFullPath() == ClassNames.COMPOSABLE }
                    val (statement, args) = if (isComposable) {
                        val typeName = requireNotNull((element.enclosingElement as? TypeElement)?.qualifiedName?.toString()) {
                            "can't find the class declaration for method ${element.simpleName}"
                        }
                        val simpleName = element.simpleName.toString()
                        """"$typeName","$simpleName"""" to emptyList()
                    } else {
                        generateCallMethodWithParamMapLambada(element, typeUtils, elementUtils)
                    }
                    getTargetJavaPoetArgs(
                        typeUtils = typeUtils,
                        type = returnType,
                        elementUtils = elementUtils,
                        handleActivity = {
                            "${typeRefLiteral}.targetCommonFuncJava($statement)" to targetArg + args
                        },
                        handleFragment = {
                            "${typeRefLiteral}.targetFragmentBuilderJava($statement)" to targetArg + args
                        },
                        handleDialog = {
                            "${typeRefLiteral}.targetDialogBuilderJava($statement)" to targetArg + args
                        },
                        handleView = {
                            "${typeRefLiteral}.targetViewBuilderJava($statement)" to targetArg + args
                        },
                        default = {
                            if (isComposable) {
                                "${typeRefLiteral}.targetComposableJava($statement)" to targetArg + args
                            } else {
                                if (returnType.kind == TypeKind.VOID) {
                                    "${typeRefLiteral}.targetCommonFuncJavaVoid($statement)" to targetArg + args
                                } else {
                                    "${typeRefLiteral}.targetCommonFuncJava($statement)" to targetArg + args
                                }
                            }
                        }

                    )
                }

                else -> {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "not supported breadboard component")
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

    private fun getTargetJavaPoetArgs(
        typeUtils: Types,
        type: TypeMirror?,
        elementUtils: Elements,
        handleActivity: () -> Pair<String, List<Any>>?,
        handleFragment: () -> Pair<String, List<Any>>?,
        handleDialog: () -> Pair<String, List<Any>>?,
        handleView: () -> Pair<String, List<Any>>?,
        default: () -> Pair<String, List<Any>>?
    ): Pair<String, List<Any>>? = when {
        typeUtils.isSubtype(type, elementUtils.getTypeElement(ClassNames.ACTIVITY).asType()) -> handleActivity()

        typeUtils.isSubtype(type, elementUtils.getTypeElement(ClassNames.FRAGMENT).asType()) -> handleFragment()

        typeUtils.isSubtype(type, elementUtils.getTypeElement(ClassNames.DIALOG).asType()) -> handleDialog()

        typeUtils.isSubtype(type, elementUtils.getTypeElement(ClassNames.VIEW).asType()) -> handleView()

        else -> default()
    }

    private fun ComponentAptProcessor.generateCallMethodWithParamMapLambada(
        element: ExecutableElement,
        typeUtils: Types,
        elementUtils: Elements,
    ): Pair<String, List<Any>> {
        val elementSequence = element.parameters
        val paramsStatement = elementSequence.joinToString {
            checkIgnoreAnnotation(it)
            val key = it.parameterKey(typeUtils, elementUtils)
            """(${typeRefLiteral})paramsMap.get("$key")"""
        }
        val paramStatementArgs = elementSequence.map { ClassName.get(it.asType()) }
        return run {
            val callElement = element.staticCallElement()
            when {
                callElement != null -> {
                    val methodClass = ClassName.get(if (callElement.isClass) callElement.asType() else callElement.enclosingElement.asType())
                    val fieldDot = if (callElement.isClass) "" else "${callElement.simpleName}."
                    "paramsMap ->${typeRefLiteral}.${fieldDot}${element.simpleName}($paramsStatement)" to listOf(methodClass) + paramStatementArgs
                }

                element.enclosingElement?.enclosedElements?.any { it.kind == ElementKind.CONSTRUCTOR && !it.isPrivate && (it as ExecutableElement).parameters.isEmpty() } == true -> {
//                    考虑内部类会有无限套娃的问题，这里不考虑。
                    val typeName = ClassName.get(element.enclosingElement.asType())
                    "paramsMap -> new ${typeRefLiteral}().${element.simpleName}($paramsStatement)" to listOf(typeName) + paramStatementArgs
                }

                else -> {
                    error("component method must be static/no private or the component class must has an empty parameter and no private constructor,method = ${element.toFullPath()}")

                }
            }
        }
    }

    private fun checkIgnoreAnnotation(variableElement: VariableElement) {
        check(variableElement.getAnnotation(Ignore::class.java) == null) {
            "@Ignore Annotation has no effect for apt/kapt,please migration to ksp or remove the @Ignore Annotation"
        }
    }

    private fun VariableElement.parameterKey(
        typeUtils: Types,
        elementUtils: Elements
    ): CharSequence? = if (typeUtils.isSameType(asType(), elementUtils.getTypeElement(ClassNames.CONTEXT).asType())) {
        ProcessConstants.PARAMS_MAP_CONTEXT_KEY
    } else {
        simpleName
    }

    fun VariableElement.isNullableByAnnotation(): Boolean {
        return annotationMirrors.any { it.annotationType.asElement().toFullPath() == "org.jetbrains.annotations.Nullable" }
    }

    private fun createProviderClass(element: Element, componentTarget: String, isInstrumentation: Boolean, vararg args: Any?) {
        val component = element.getAnnotation(Component::class.java)
        val pkgName = processingEnv.elementUtils.getPackageOf(element).toString()
        val paramHolders = if (element is ExecutableElement) {
            getParamsFromMethodParameters(element)
        } else {
            getParamsFromAnnotation(element, component)
        }
        val fullPath = element.toFullPath() + (element as? ExecutableElement)?.run {
//                取参数的sampleName 也有冲突的可能，但概率很低，如果有此情况，应该由开发者修改代码
            parameters.joinToString("_", "_") {
                val asType = it.asType()
                simpleTypeString(asType)
            }
        }.orEmpty()
        val projectName = processingEnv.options.getOrDefault(ProcessConstants.ARG_CODE_GEN_PROJECT_NAME, "")
        val returnStatement = """
            return new ${typeRefLiteral}(
            "$fullPath",
             "${component.name.quoteJava()}",
             "${component.description.quoteJava()}",
             tags,
             "${component.documentation.quoteJava()}",
             owners,
             target,
              "${component.route.ifBlank { fullPath }}",
              "${component.sourcePath}",
             params,
             "$projectName"
            )
        """.trimIndent()
        val fileName = ("${ClassNames.GEN_FILE_NAME_PREFIX}${fullPath.substringAfter(pkgName).replace(".", "_")}Generated")
        val targetClassName = ClassName.get("io.xnchung.breadboard.model", "Target")
        val generatedClass: TypeSpec = TypeSpec.classBuilder(fileName)
            .addModifiers(Modifier.PUBLIC)
            .addMethod(
                MethodSpec.methodBuilder("${ClassNames.GEN_FUNC_NAME_PREFIX}component")
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .returns(ClassName.bestGuess(ClassNames.SAMPLE_CONTRACT_FQN))
                    .addStatement(
                        "${typeRefLiteral}<${typeRefLiteral}<?>> params = new ${typeRefLiteral}<>();",
                        List::class.java,
                        ClassName.get("io.xnchung.breadboard.model", "ParamHolderContract"),
                        ArrayList::class.java
                    )
                    .run {
                        paramHolders.fold(this) { builder: MethodSpec.Builder, pair: Pair<String, List<Any>> ->
                            builder.addStatement("params.add(${pair.first});", *pair.second.toTypedArray())
                        }
                    }.run {
                        listOf(
                            "tags" to component.tags,
                            "owners" to component.owners,
                        ).fold(this) { builder, field ->
                            val fieldName = field.first
                            builder.addStatement(
                                "${typeRefLiteral}<String> $fieldName = new ${typeRefLiteral}<>();",
                                List::class.java,
                                ArrayList::class.java
                            ).run {
                                field.second.fold(this) { builder, s ->
                                    builder.addStatement("$fieldName.add(\"$s\")")
                                }
                            }
                        }
                    }
                    .addStatement("$typeRefLiteral target = $componentTarget", targetClassName, *args)
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

        val javaFile: JavaFile = JavaFile.builder(pkgName, generatedClass)
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

    private fun simpleTypeString(asType: TypeMirror): String = when (asType.kind) {
        TypeKind.BOOLEAN -> "Boolean"
        TypeKind.BYTE -> "Byte"
        TypeKind.SHORT -> "Short"
        TypeKind.INT -> "Int"
        TypeKind.LONG -> "Long"
        TypeKind.CHAR -> "Char"
        TypeKind.FLOAT -> "Float"
        TypeKind.DOUBLE -> "Double"
        TypeKind.ARRAY -> {
            val array: ArrayType = asType as ArrayType
            "${simpleTypeString(array.componentType)}Array"
        }

        TypeKind.DECLARED -> {
            val declaredType = asType as DeclaredType
            declaredType.asElement().simpleName.toString().let {
                if (it == "Integer") "Int" else it
            }
        }

        else -> ""
    }

    private fun getParamsFromMethodParameters(element: ExecutableElement) = element.parameters
        .map {
            checkIgnoreAnnotation(it)
            val typeName = ClassName.get(it.asType()).let { if (it.isPrimitive) it.box() else it }
            // TODO: get desc from comment
            val parameterKey = it.parameterKey(processingEnv.typeUtils, processingEnv.elementUtils)
            val annotationMirror =
                element.annotationMirrors.firstOrNull { it.annotationType.asElement().toFullPath() == ParamProvider::class.java.canonicalName }
            val providerType =
                annotationMirror?.elementValues?.entries?.first { it.key.simpleName.contentEquals("provider") }?.value?.value as? TypeMirror
            val (providerStatement, providerArgs) = if (providerType != null) {
                getInitStatement(providerType)
            } else {
                "null" to listOf()
            }
            val statement =
                """new ${typeRefLiteral}<${typeRefLiteral}>("$parameterKey","","","",${it.isNullableByAnnotation()},${providerStatement},new ${typeRefLiteral}<${typeRefLiteral}>(){})""".trimIndent()
            val paramHolderTypeName = ClassName.get("io.xnchung.breadboard.model", "ParamHolder")
            val typeTokenTypeName = ClassName.get("io.xnchung.breadboard.model", "TypeToken")
            val args = listOf<Any>(paramHolderTypeName, typeName) + providerArgs + listOf(typeTokenTypeName, typeName)
            statement to args
        }.toTypedArray()

    private fun getParamsFromAnnotation(
        element: Element,
        component: Component
    ): Array<Pair<String, List<Any>>> {
        val annotationMirror =
            element.annotationMirrors.first { it.annotationType.asElement().toFullPath() == Component::class.java.canonicalName }
        val annotationValue = annotationMirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("params") }?.value
        val paramsAnnotationMirrors = annotationValue?.value as? List<*>
        val paramHolders = component.params.mapIndexedNotNull { i: Int, param: Param ->
            val mirror = paramsAnnotationMirrors?.get(i) as? AnnotationMirror ?: return@mapIndexedNotNull null
            val fqn = ParamTypeTokenAnnotation(param, mirror).parseToFqn(true).let {
                when (param.collectionType) {
                    CollectionType.None -> it
                    CollectionType.Array -> "$it[]"
                    CollectionType.ArrayList -> "${ArrayList::class.qualifiedName}<$it>"
                    CollectionType.List -> "${List::class.qualifiedName}<$it>"
                }
            }
            val qualifier =
                mirror.elementValues.entries.firstOrNull { it.key.simpleName.contentEquals("qualifier") }?.value?.value?.toString().orEmpty()

            val providerType =
                mirror.elementValues?.entries?.firstOrNull { it.key.simpleName.contentEquals("provider") }?.value?.value as? TypeMirror
            val (providerStatement, providerArgs) = if (providerType != null) {
                getInitStatement(providerType)
            } else {
                "null" to listOf()
            }
            val defaultValue = param.defaultValue.takeIf { it.isNotBlank() }?.let {
                param.defaultValue
            } ?: when (param.collectionType) {
                CollectionType.None -> ""
                CollectionType.Array,
                CollectionType.ArrayList,
                CollectionType.List -> "[]"
            }
            val statement =
                """new ${typeRefLiteral}<$fqn>("${param.key}","${param.desc.quoteJava()}","${defaultValue.quoteJava()}","$qualifier",${param.nullable},${providerStatement},new ${typeRefLiteral}<$fqn>(){})""".trimIndent()
            val paramHolderTypeName = ClassName.get("io.xnchung.breadboard.model", "ParamHolder")
            val typeTokenTypeName = ClassName.get("io.xnchung.breadboard.model", "TypeToken")
            statement to listOf<Any>(paramHolderTypeName) + providerArgs + listOf(typeTokenTypeName)
        }.toTypedArray()
        return paramHolders
    }

    private fun getInitStatement(providerType: TypeMirror?): Pair<String, List<TypeName>> {
        val asElement = processingEnv.typeUtils.asElement(providerType)
        val allMembers = processingEnv.elementUtils.getAllMembers(asElement as TypeElement)
        val constructors =
            allMembers.filter { it.kind == ElementKind.CONSTRUCTOR && it.isPublic && (it as ExecutableElement).parameters.isEmpty() }
        val statement = if (constructors.isNotEmpty()) {
            "new ${typeRefLiteral}()"
        } else {
            //                    kotlin object
            allMembers.firstOrNull { it.kind == ElementKind.FIELD && it.isStatic && it.asType() == providerType }?.let {
                "${typeRefLiteral}.${it.simpleName}"
            }
        }
        return requireNotNull(statement) {
            "can't find the public and empty constructor or static instance for $providerType"
        } to listOf(ClassName.get(providerType))
    }

}

internal fun TypeSpec.Builder.addModuleAnnotation(): TypeSpec.Builder {
    addAnnotation(ClassName.get("dagger", "Module"))
    val installIn = ClassName.get("dagger.hilt", "InstallIn")
    val singletonComponent = ClassName.get("dagger.hilt.components", "SingletonComponent")
    return addAnnotation(AnnotationSpec.builder(installIn).addMember("value", "${typeRefLiteral}.class", singletonComponent).build())
}


internal fun AnnotationMirror.rawTypeFqnFromMirror() =
    (elementValues.entries.first { it.key.simpleName.contentEquals("rawType") }.value as AnnotationValue).value.let {
        if (it is PrimitiveType) ClassName.get(it).box() else it
    }.toString().replace("java.lang.", "")

internal const val typeRefLiteral = "${'$'}T"

internal fun generateDiActivityFoJava(processingEnv: ProcessingEnvironment) {

    val diActivityFqn = ClassNames.DI_ACTIVITY_FQN
    if (processingEnv.elementUtils.getTypeElement(diActivityFqn) != null) {
        return
    }
    val packageName = diActivityFqn.substringBeforeLast(".")
    val className = diActivityFqn.substringAfterLast(".")
    val superClass = ClassName.get(packageName, "BaseBreadBoardLauncherActivity")
    val entryPointAnnotation = ClassName.get("dagger.hilt.android", "AndroidEntryPoint")
    val hiltViewModel = ClassName.get("dagger.hilt.android.lifecycle", "HiltViewModel")
    val savedStateHandle = ClassName.get("androidx.lifecycle", "SavedStateHandle")
    val javaXInject = ClassName.get("javax.inject", "Inject")
    val launcherViewModel = ClassName.get("io.xnchung.breadboard.launcher.ui", "LauncherViewModel")
    val stateHandleFiledName = "stateHandle"
    val myLauncherViewModel = ClassName.get("", "MyLauncherViewModel")
    val viewModelProvider = ClassName.get("androidx.lifecycle", "ViewModelProvider")
    val setTypeName = ClassName.get(Set::class.java)
    val arrayList = ClassName.get(ArrayList::class.java)
    val sampleClassName = ClassName.bestGuess(ClassNames.SAMPLE_CONTRACT_FQN)
    val typeSpec = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .superclass(superClass)
        .addType(
            TypeSpec.classBuilder("MyLauncherViewModel")
                .superclass(launcherViewModel)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addAnnotation(hiltViewModel)
                .addMethod(
                    MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(savedStateHandle, stateHandleFiledName)
                        .addParameter(ParameterizedTypeName.get(setTypeName, sampleClassName), "injectedSamples")
                        .addParameter(
                            ParameterizedTypeName.get(setTypeName, ClassName.bestGuess(ClassNames.CONVERTER_HOLDER_FQN)),
                            "injectedConverters"
                        )
                        .addStatement("super(injectedConverters,stateHandle,new $typeRefLiteral<>(injectedSamples))", arrayList)
                        .addAnnotation(javaXInject)
                        .build()
                )
                .build()
        )
        .addField(myLauncherViewModel, "viewModel", Modifier.PRIVATE)
        .addMethod(
            MethodSpec.methodBuilder("getViewModel")
                .addModifiers(Modifier.PROTECTED)
                .addAnnotation(Override::class.java)
                .returns(launcherViewModel)
                .beginControlFlow(" if (viewModel == null)")
                .addStatement("viewModel = initViewModel()")
                .endControlFlow()
                .addStatement("return viewModel")
                .build()
        )
        .addMethod(
            MethodSpec.methodBuilder("initViewModel")
                .addModifiers(Modifier.PRIVATE)
                .returns(myLauncherViewModel)
                .addStatement("return new $typeRefLiteral(this).get($typeRefLiteral.class)", viewModelProvider, myLauncherViewModel)
                .build()
        )

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

fun List<VariableElement>.filterIgnore() = asSequence().filter { it.getAnnotation(Ignore::class.java) == null }