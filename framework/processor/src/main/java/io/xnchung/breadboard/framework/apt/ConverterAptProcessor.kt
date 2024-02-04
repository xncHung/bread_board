package io.xnchung.breadboard.framework.apt

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import io.xnchung.breadboard.annotations.ConverterProvider
import io.xnchung.breadboard.annotations.DefaultQualifier
import io.xnchung.breadboard.framework.ProcessConstants
import io.xnchung.breadboard.framework.ksp.ClassNames
import java.io.IOException
import java.lang.reflect.Type
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedOptions
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.coroutines.Continuation


@SupportedAnnotationTypes("io.xnchung.breadboard.annotations.ConverterProvider")
@SupportedOptions(ProcessConstants.ARG_CODE_GEN_ENABLE)
class ConverterAptProcessor : BaseComponentAptProcessor() {
    override fun realProcess(annotations: MutableSet<out TypeElement>, environment: RoundEnvironment): Boolean {
        if (environment.processingOver()){
            return true
        }
        val components = environment.getElementsAnnotatedWith(ConverterProvider::class.java)
        val typeUtils = processingEnv.typeUtils
        val elementUtils = processingEnv.elementUtils
        components.forEach {
            when (it.kind) {
                ElementKind.METHOD -> {
                    val executableElement = it as ExecutableElement
                    val returnType = executableElement.returnType as DeclaredType
                    val isJvmFunc2 = typeUtils.isSameType(
                        returnType.asElement().asType(),
                        elementUtils.getTypeElement(ProcessConstants.KOTLIN_JVM_FUNC_2).asType()
                    )
                    val isJvmFunc3 = typeUtils.isSameType(
                        returnType.asElement().asType(),
                        elementUtils.getTypeElement(ProcessConstants.KOTLIN_JVM_FUNC_3).asType()
                    )

                    fun suspendArgIsValid(): Boolean {
                        return run {

                            returnType.typeArguments.run {
                                size == 4
                                        //                                    first must be String
                                        && typeUtils.isSameType(
                                    first() as TypeMirror,
                                    elementUtils.getTypeElement(String::class.java.canonicalName).asType()
                                )
                                        //                                    second must be Type
                                        && typeUtils.isSameType(
                                    this[1] as TypeMirror,
                                    elementUtils.getTypeElement(Type::class.java.canonicalName).asType()
                                )
                                        //                                    third must be Continuation
                                        && typeUtils.isSubtype(
                                    (this[2] as DeclaredType).asElement().asType(),
                                    elementUtils.getTypeElement(Continuation::class.java.canonicalName).asType()
                                )

                            }
                        }
                    }


                    fun argIsValid() = run {
                        returnType.typeArguments.run {
                            size == 3
//                                    first must be String
                                    && typeUtils.isSameType(
                                first() as TypeMirror,
                                elementUtils.getTypeElement(String::class.java.canonicalName).asType()
                            )
//                                    second must be Type
                                    && typeUtils.isSameType(
                                this[1] as TypeMirror,
                                elementUtils.getTypeElement(Type::class.java.canonicalName).asType()
                            )


                        }
                    }

                    val emptyParams = executableElement.parameters.isEmpty()
                    if (emptyParams && it.isPublic && it.isStatic &&
                        (isJvmFunc2 && argIsValid() || isJvmFunc3 && suspendArgIsValid())
                    ) {
                        createProviderClass(it, isJvmFunc3)
                    } else {
                        processingEnv.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "converterFactory method:'${it.toFullPath()}' is not valid,the method should has not any parameter,should be public and static,the return type should be ${ProcessConstants.KOTLIN_JVM_FUNC_2},like Function2<String,Type,Bean>"
                        )
                        null
                    }
                }

                else -> {
                    processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, "not supported breadboard converter")
                    null
                }
            }

        }
        return true
    }

    private fun createProviderClass(element: ExecutableElement, isSuspend: Boolean) {
        val typeUtils = processingEnv.typeUtils
        val elementUtils = processingEnv.elementUtils
        val converter = element.getAnnotation(ConverterProvider::class.java)
        val qualifier = element.annotationMirrors.firstOrNull {
            it.annotationType.asElement().annotationMirrors.firstOrNull {
                it.annotationType.let {
                    typeUtils.isSameType(
                        it,
                        elementUtils.getTypeElement(ProcessConstants.PARAM_QUALIFIER).asType()
                    )
                }
            } != null
        }?.annotationType?.asElement()?.toFullPath() ?: DefaultQualifier::class.java.canonicalName
        val methodClass = element.enclosingElement.asType().let { ClassName.get(it) }

        val fileName = "_breadboard" + element.toFullPath().replace(".", "_") + "Generated"
        val generatedClass: TypeSpec = TypeSpec.classBuilder(fileName)
            .addMethod(
                MethodSpec.methodBuilder("${ClassNames.GEN_FUNC_NAME_SUFFIX}${fileName}Converter")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get("io.xnchung.breadboard.converters", "ConverterHolder"))
                    .addStatement(
                        """
                        return new $typeRefLiteral("$qualifier",${converter.order},$typeRefLiteral.${element.simpleName}())
                        """.trimIndent(),
                        ClassName.get("io.xnchung.breadboard.converters", if (isSuspend) "ConverterHolder.Suspend" else "ConverterHolder.Normal"),
                        methodClass
                    )
                    .build()
            )
            .build()
        val javaFile: JavaFile = JavaFile.builder(ClassNames.CONVERTERS_PACKAGE_NAME, generatedClass)
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

