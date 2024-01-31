package io.xnchung.breadboard.gradle.plugin.instrumentation

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.internal.cxx.io.hardLinkOrCopyFile
import com.android.build.gradle.internal.pipeline.TransformManager
import io.xnchung.breadboard.gradle.plugin.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.ClassNames
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class CollectConverterFactoriesAndSamplesTransform(
    private val project: Project,
    private val breadBoardExtension: BreadBoardExtension,
) : Transform() {
    val cacheDir = project.buildDir.resolve("intermediates/incremental/breadboardClassesCache")
    override fun getName(): String {
        return "BreadboardCollectComponentsAndConverterFactories"
    }

    override fun applyToVariant(variant: VariantInfo): Boolean {
        return variant.isDebuggable
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


    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        Logging.getLogger("collect").warn("collect transform start")
        requireNotNull(inputs)
        requireNotNull(outputProvider)
        val converterProviders = mutableListOf<AsmMethodInfo>()
        val components = mutableListOf<AsmMethodInfo>()
        var componentsJar: Pair<File, File>? = null
        var converterProvidersJar: Pair<File, File>? = null

        val visitor = CollectConverterProvidersAndComponentVisitor({
            components.add(it)
        }) {
            converterProviders.add(it)
        }
        inputs.forEach { transformInput ->
            for (jarInput in transformInput.jarInputs) {
                val source = jarInput.file
                val name = jarInput.name
                val jarFile = JarFile(source)
                var findHookHandle = false
                val destName = name.substringBeforeLast(".jar")
                val dest = outputProvider.getContentLocation(destName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (componentsJar == null) {
                    val samplesHolders = jarFile.getJarEntry(ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME)
                    if (samplesHolders != null) {
                        componentsJar = source to dest
                        findHookHandle = true
                    }
                }
                if (converterProvidersJar == null) {
                    val converterCore = jarFile.getJarEntry(ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME)
                    if (converterCore != null) {
                        converterProvidersJar = source to dest
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

        if (converterProviders.isNotEmpty()) {
            val converterProvidersJarFile = requireNotNull(converterProvidersJar) {
                "can't find the ConverterCore.class,check the dependency of breadboard-base lib"
            }
            converterProvidersJarFile.also {
                val (source, dest) = it
                instrumentationConverterAndSamplerProviders(dest, source, converterProviders)
            }
        } else {
            converterProvidersJar?.apply {
                hardLinkOrCopyFile(first, second)
            }
        }

        if (componentsJar == converterProvidersJar && converterProviders.isNotEmpty()) {
//            in most cases,componentsJar and converterProvidersJar is same Jar,we don't need to transform once again
            return
        }
        if (components.isNotEmpty()) {
            val componentJarFile = requireNotNull(componentsJar) {
                "can't find the AllSamplesHolder.class,check the dependency of breadboard-launcher lib"
            }
            componentJarFile.also {
                val (source, dest) = it
                instrumentationConverterAndSamplerProviders(dest, source, components)
            }
        } else {
            componentsJar?.apply {
                hardLinkOrCopyFile(first, second)
            }
        }
    }

    private fun instrumentationConverterAndSamplerProviders(dest: File, source: File, converterProviders: MutableList<AsmMethodInfo>) {
        transformJar(
            dest,
            source,
            { name == ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME || name == ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME }
        ) { ConverterProvidersInstrumentationVisitor(this, converterProviders) }
    }


    private fun transformJar(
        dest: File,
        source: File,
        jarEntrySelector: JarEntry.() -> Boolean,
        visitorAdapterFunc: ClassWriter.() -> ClassVisitor
    ) {
        JarOutputStream(dest.outputStream()).use { outJar ->
            val jarFile = JarFile(source)
            jarFile.entries().asSequence().forEach { jarEntry ->
                val inputStream = jarFile.getInputStream(jarEntry)
                inputStream.use { jarEntryInput ->
                    if (jarEntry.jarEntrySelector()) {
                        val newEntry = JarEntry(jarEntry.name)
                        outJar.putNextEntry(newEntry)
                        val classReader = ClassReader(jarEntryInput)
                        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
                        val instrumentationVisitor = classWriter.visitorAdapterFunc()
                        classReader.accept(instrumentationVisitor, ClassReader.SKIP_FRAMES)
                        outJar.write(classWriter.toByteArray())
                    } else {
                        outJar.putNextEntry(jarEntry)
                        outJar.write(jarEntryInput.readBytes())
                    }
                    outJar.closeEntry()
                }
            }
        }
    }
}