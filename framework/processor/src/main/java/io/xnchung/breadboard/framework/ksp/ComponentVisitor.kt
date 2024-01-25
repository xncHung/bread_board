package io.xnchung.breadboard.framework.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid

internal class ComponentVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : KSVisitorVoid() {

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val target = classDeclaration.getAllSuperTypes().firstNotNullOfOrNull {
            val className = it.declaration.qualifiedName?.asString().orEmpty()
            logger.warn(className)
            when (className) {
                "android.app.Activity" -> {
                    "targetActivity<${classDeclaration.toFullPath()}>()"
                }

                "androidx.fragment.app.Fragment" -> {
                    "targetFragment<${classDeclaration.toFullPath()}>()"
                }

                else -> null
            }
        }
        requireNotNull(target) {
            "@Sample only supports Fragments, Activities or Composable functions"
        }

//        createModule(classDeclaration, target)
    }

//    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//        require(
//            function.annotations.any { it.shortName.asString() == "Composable" } &&
//                function.parameters.isEmpty()
//        ) {
//            "@Sample must be a in a Composable function with empty parameters"
//        }
//
//        createModule(function, "targetComposable { ${function.toFullPath()}() }")
//    }

    @OptIn(KspExperimental::class)
//    private fun createModule(functionSample: KSDeclaration, target: String) {
//        val filePath = getRelativeFilePath(functionSample)
//        val packageName = functionSample.packageName.asString()
//        val sampleFile = functionSample.simpleName.asString()
//        val sample = functionSample.getAnnotationsByType(Sample::class).first()
//        sample.params.forEach {
//
//        }
//        val sampleSource = sample.sourcePath.ifBlank { filePath }
//        val minSDK = functionSample.getAnnotationsByType(RequiresApi::class).minOfOrNull {
//            it.value
//        } ?: 0
//
//        val file = codeGenerator.createNewFile(
//            dependencies = Dependencies(
//                aggregating = true,
//            ),
//            packageName = packageName,
//            fileName = "${sampleFile}Module"
//        )
//        file.use { stream ->
//            stream.write(
//                sampleTemplate(
//                    sampleFile = sampleFile,
//                    samplePackage = packageName,
//                    sampleName = sample.name,
//                    sampleDescription = sample.description,
//                    sampleTags = sample.tags,
//                    sampleDocs = sample.documentation,
//                    sampleSource = sampleSource,
//                    samplePath = filePath.substringBefore("/src"),
//                    sampleOwners = sample.owners,
//                    sampleTarget = target,
//                    sampleMinSdk = minSDK,
//                    sampleRoute = "$sampleSource-$sampleFile",
//                ).toByteArray()
//            )
//        }
//    }

    private fun KSDeclaration.toFullPath() =
        "${packageName.asString()}.${simpleName.asString()}"

    private fun getRelativeFilePath(declaration: KSDeclaration): String {
        val path = (declaration.location as? FileLocation)?.filePath.orEmpty()
        return path.substringAfterLast("/samples/")
    }
}