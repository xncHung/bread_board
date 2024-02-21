package io.xnchung.breadboard.gradle.plugin.instrumentation

import com.android.build.api.transform.Context
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.api.variant.VariantInfo
import com.android.build.gradle.internal.pipeline.TransformManager
import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension
import org.gradle.api.Project
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import io.xnchung.breadboard.gradle.plugin.ClassNames
import io.xnchung.breadboard.gradle.plugin.apicompat.CollectDataStrategy
import io.xnchung.breadboard.gradle.plugin.apicompat.collectDataStrategy
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.AsmMethodInfo
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.CollectConverterProvidersAndComponentVisitor
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.ConverterProvidersInstrumentationVisitor
import org.gradle.configurationcache.extensions.capitalized

class CollectConverterFactoriesAndSamplesTransform(
    private val project: Project,
    private val breadBoardExtension: BreadBoardExtension,
    private val copyFileHandle: (File, File) -> Unit
) : Transform() {
    private val targetClassNames = listOf(
        ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME,
        ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME,
    )
    private val methodsContainers = mutableMapOf<String, MutableList<AsmMethodInfo>>()
    override fun getName(): String {
        return "BreadboardCollectData"
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
        return false
    }


    override fun transform(
        context: Context?,
        inputs: MutableCollection<TransformInput>?,
        referencedInputs: MutableCollection<TransformInput>?,
        outputProvider: TransformOutputProvider?,
        isIncremental: Boolean
    ) {
        requireNotNull(inputs)
        requireNotNull(outputProvider)
        val targetJars = mutableMapOf<String, Pair<File, File>>()
        val visitor = CollectConverterProvidersAndComponentVisitor({
            methodsContainers.getOrPut(ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME) { mutableListOf() }.add(it)
        }) {
            methodsContainers.getOrPut(ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME) { mutableListOf() }.add(it)
        }
        val targetNames = targetClassNames.asSequence()
        inputs.forEach { transformInput ->
            for (jarInput in transformInput.jarInputs) {
                val source = jarInput.file
                val name = jarInput.name
                val jarFile = JarFile(source)
                var findHookHandle = false
                val destName = name.substringBeforeLast(".jar")
                val dest =
                    outputProvider.getContentLocation(destName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                targetNames.filter {
                    it !in targetJars
                }.forEach {
                    val samplesHolders = jarFile.getJarEntry(it)
                    if (samplesHolders != null) {
                        targetJars[it] = source to dest
                        findHookHandle = true
                    }
                }

//                        we do not need to scan external lib in most cases
                if (QualifiedContent.Scope.SUB_PROJECTS in jarInput.scopes || QualifiedContent.Scope.PROJECT in jarInput.scopes || !breadBoardExtension.excludeExternalLib) {
                    jarFile.entries().asSequence().filter { !it.isDirectory && it.name.endsWith(".class") }
                        .forEach {
                            val inputStream = jarFile.getInputStream(it)
                            ClassReader(inputStream).accept(visitor, ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE))
                        }
                }
                if (!findHookHandle) {
                    //launcher or base lib will be instrumentation,can't be hardlink or copy
                    copyFileHandle(source, dest)
                }
            }
            transformInput.directoryInputs.forEach { directoryInput ->
                val dest =
                    outputProvider.getContentLocation(
                        directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY
                    )
                directoryInput.file.walk().filter { !it.isDirectory }.forEach {
                    if (it.name.endsWith(".class")) {
                        val inputStream = it.inputStream()
                        ClassReader(inputStream).accept(visitor, ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE))
                    }
                    val destination = File(dest, it.toRelativeString(directoryInput.file))
                    copyFileHandle(it, destination)
                }
            }
        }
        targetJars.values.toSet().forEach {
            val (source, dest) = it
            instrumentationConverterAndSamplerProviders(
                dest,
                source,
                methodsContainers[ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME] ?: emptyList(),
                methodsContainers[ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME] ?: emptyList(),
            )
        }
    }

    private fun instrumentationConverterAndSamplerProviders(
        dest: File,
        source: File,
        converterProviders: List<AsmMethodInfo>,
        components: List<AsmMethodInfo>
    ) {
        transformJar(
            dest,
            source,
            { name in targetClassNames }
        ) { ConverterProvidersInstrumentationVisitor(this, converterProviders, components) }
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
                    val newEntry = JarEntry(jarEntry.name)
                    outJar.putNextEntry(newEntry)
                    if (jarEntry.jarEntrySelector()) {
                        val classReader = ClassReader(jarEntryInput)
                        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
                        val instrumentationVisitor = classWriter.visitorAdapterFunc()
                        classReader.accept(instrumentationVisitor, ClassReader.SKIP_FRAMES)
                        outJar.write(classWriter.toByteArray())
                    } else {
                        outJar.write(jarEntryInput.readBytes())
                    }
                    outJar.closeEntry()
                }
            }
        }
    }
}