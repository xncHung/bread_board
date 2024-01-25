package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import io.xnchung.breadboard.annotations.ParamQualifier
import java.lang.reflect.Type
import kotlin.reflect.KClass

fun KSAnnotated.qualifierAnnotation() =
    annotations.firstOrNull {
        it.annotationType.resolve().declaration.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == ParamQualifier::class.qualifiedName
        }
    }

val KSFunctionDeclaration.returnStringConverterFunc
    get() =
        returnType?.resolve()?.let {
            when (it.declaration.toFullPath()) {
                "kotlin.Function2", "kotlin.coroutines.SuspendFunction2" -> {
                    it.arguments.first().type?.resolve()?.declaration?.toFullPath() == String::class.qualifiedName &&
                            it.arguments[1].type?.resolve()?.declaration?.toFullPath() == Type::class.qualifiedName
                }

                else -> false
            }
        } == true

val KSFunctionDeclaration.isStaticOrTopLevel get() = functionKind == FunctionKind.TOP_LEVEL || functionKind == FunctionKind.STATIC
val KSFunctionDeclaration.onlyTypeParam get() = parameters.size == 1 && parameters.first().type.resolve().declaration.qualifiedName?.asString() == Type::class.qualifiedName
val KSFunctionDeclaration.stringExtReceiver get() = extensionReceiver?.resolve()?.declaration?.qualifiedName?.asString() == String::class.qualifiedName
val KSFunctionDeclaration.isValidConverterFactory
    get() =
        isPublic() &&
                parameters.isEmpty() &&
                returnStringConverterFunc &&
                isStaticOrTopLevel

fun <T : Annotation> KSAnnotated.getKSAnnotationsByType(annotationKClass: KClass<T>): Sequence<KSAnnotation> {
    return this.annotations.filter {
        it.shortName.getShortName() == annotationKClass.simpleName && it.annotationType.resolve().declaration
            .qualifiedName?.asString() == annotationKClass.qualifiedName
    }
}

fun <T> KSAnnotation.getArg(param: String) = arguments.firstOrNull { it.name?.asString() == param }?.value as? T
fun KSAnnotation.getStringArg(param: String) = getArg<String>(param).orEmpty()
fun String.quote() = """
    ""${'"'}$this""${'"'}
""".trimIndent()