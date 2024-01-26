package io.xnchung.breadboard.gradle.plugin

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.cxx.io.hardLinkOrCopyFile
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassReader
import java.io.File
import java.util.jar.JarFile

class CollectConverterFactoriesAndSamplesTransform(
    private val project: Project,
    private val breadBoardExtension: BreadBoardExtension,
) : Transform() {
    val cacheDir = project.buildDir.resolve("intermediates/incremental/breadboardClassesCache")
    override fun getName(): String {
        return "BreadboardCollectComponentsAndConverterFactories"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return breadBoardExtension.incremental
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
    }

    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        val logger = Logging.getLogger("breadboard")
        requireNotNull(inputs)
        requireNotNull(outputProvider)
        val components = mutableListOf<String>()
        val converterProviders = mutableListOf<String>()
        var componentsJar: File? = null
        var converterProvidersJar: File? = null

        val visitor = CollectConverterProvidersAndComponentVisitor({

        }) {}
        inputs.forEach { transformInput ->
            for (jarInput in transformInput.jarInputs) {
                val source = jarInput.file
                val name = jarInput.name
                val jarFile = JarFile(source)
                var findHookHandle = false
                val destName = name.substringBeforeLast(".jar")
                val dest = outputProvider.getContentLocation(destName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (componentsJar == null) {
                    val samplesHolders = jarFile.getJarEntry("io/xnchung/breadboard/launcher/AllSamplesHolder.class")
                    if (samplesHolders != null) {
                        copyFile(source, dest)
                        componentsJar = dest
                        findHookHandle = true
                    }
                }
                if (converterProvidersJar == null) {
                    val converterCore = jarFile.getJarEntry("io/xnchung/breadboard/converters/ConverterCore.class")
                    if (converterCore != null) {
                        copyFile(source, dest)
                        converterProvidersJar = dest
                        findHookHandle = true
                    }
                }
                if (!findHookHandle) {//not launcher or base lib
//                        we do not need to scan external lib in most cases
                    if (QualifiedContent.Scope.SUB_PROJECTS in jarInput.scopes || QualifiedContent.Scope.PROJECT in jarInput.scopes || !breadBoardExtension.excludeExternalLib) {
                        jarFile.entries().asSequence().filter { !it.isDirectory && it.name.endsWith(".class") }
                            .forEach {
                                val inputStream = jarFile.getInputStream(it)
                                ClassReader(inputStream).accept(visitor, ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE))
                            }
                    }
                    hardLinkOrCopyFile(source, dest)
                }
            }
            transformInput.directoryInputs.forEach { directoryInput ->
                val dest =
                    outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                directoryInput.file.walk().filter { !it.isDirectory }.forEach {
                    if (it.name.endsWith(".class")) {
                        val inputStream = it.inputStream()
                        ClassReader(inputStream).accept(visitor, ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE))
                    }
                    val destination = File(dest, it.toRelativeString(directoryInput.file))
                    hardLinkOrCopyFile(it, destination)
                }
            }
        }
        val componentJarFile = requireNotNull(componentsJar) {
            // TODO: components error msg
        }

        val converterProvidersJarFile = requireNotNull(converterProvidersJar) {
            // TODO: components error msg
        }

    }

    private fun copyFile(source: File, dest: File) {
        source.inputStream().use { input ->
            dest.outputStream().use {
                input.copyTo(it)
                it.flush()
            }
        }
    }

}