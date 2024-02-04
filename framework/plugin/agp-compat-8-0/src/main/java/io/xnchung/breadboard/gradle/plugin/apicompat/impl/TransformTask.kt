package io.xnchung.breadboard.gradle.plugin.apicompat.impl

import com.android.builder.packaging.JarFlinger
import io.xnchung.breadboard.gradle.plugin.ClassNames
import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.AsmMethodInfo
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.CollectConverterProvidersAndComponentVisitor
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.ConverterProvidersInstrumentationVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import java.io.File
import java.util.jar.JarEntry
import java.util.jar.JarFile

@CacheableTask
abstract class TransformTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allJars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allDirectories: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    private val breadBoardExtension by lazy {
        project.extensions.getByType(BreadBoardExtension::class.java)
    }
    private val targetClassNames = listOf(
        ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME,
        ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME,
    )
    private val methodsContainers = mutableMapOf<String, MutableList<AsmMethodInfo>>()

    @TaskAction
    fun taskAction() {

        val logger = Logging.getLogger("Breadboard")
        val res = mutableSetOf<String>()
        val jarFlinger = JarFlinger(
            output.get().asFile.toPath()
        ) { entryName ->
            res.add(entryName).also {
                if (!it) {
                    logger.info("duplicate entry:$entryName")
                }
            }

        }
        val targetJars = mutableMapOf<String, File>()
        val visitor = CollectConverterProvidersAndComponentVisitor({
            methodsContainers.getOrPut(ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME) { mutableListOf() }
                .add(it)
        }) {
            methodsContainers.getOrPut(ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME) { mutableListOf() }.add(it)
        }
        val targetNames = targetClassNames.asSequence()
        for (jarInput in allJars.get()) {
            val source = jarInput.asFile
            val jarFile = JarFile(source)
            var findHookHandle = false
            targetNames.filter {
                it !in targetJars
            }.forEach {
                val samplesHolders = jarFile.getJarEntry(it)
                if (samplesHolders != null) {
                    targetJars[it] = source
                    findHookHandle = true
                }
            }

//                        we do not need to scan external lib in most cases
            val sourcePath = source.absolutePath
            val needToScan = (!breadBoardExtension.excludeExternalLib
                    || sourcePath.startsWith(project.buildDir.absolutePath)
                    || project.subprojects.any { sourcePath.startsWith(it.buildDir.absolutePath) })

            jarFile.entries().asSequence()
                .forEach { jarEntry ->
                    val inputStream = jarFile.getInputStream(jarEntry)
                    val name = jarEntry.name
                    if (needToScan && !jarEntry.isDirectory && name.endsWith(".class")) {
                        ClassReader(inputStream).accept(
                            visitor,
                            ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE)
                        )
                    }
                }
            jarFile.close()
            if (!findHookHandle) {
                jarFlinger.addJar(source.toPath())
            }
        }
        allDirectories.get().forEach { directory ->
            directory.asFile.walk().filter { it.isFile }.forEach { file: File ->
                if (file.name.endsWith(".class")) {
                    val inputStream = file.inputStream()
                    ClassReader(inputStream).accept(visitor, ClassReader.SKIP_FRAMES.or(ClassReader.SKIP_CODE))
                }
                jarFlinger.addDirectory(directory.asFile.toPath())
            }
        }
        targetJars.values.toSet().forEach {
            transformJar(
                jarFlinger,
                it,
                { name in targetClassNames }
            ) {
                ConverterProvidersInstrumentationVisitor(
                    this,
                    methodsContainers[ClassNames.CONVERTER_CORE_CLASS_ENTRY_NAME] ?: emptyList(),
                    methodsContainers[ClassNames.ALL_SAMPLES_HOLDER_CLASS_ENTRY_NAME] ?: emptyList(),
                )
            }
        }
        jarFlinger.close()
    }

    private fun transformJar(
        jarFlinger: JarFlinger,
        source: File,
        jarEntrySelector: JarEntry.() -> Boolean,
        visitorAdapterFunc: ClassWriter.() -> ClassVisitor
    ) {
        val jarFile = JarFile(source)
        jarFile.entries().asSequence().forEach { jarEntry ->
            jarEntry.isDirectory
            val inputStream = jarFile.getInputStream(jarEntry)
            inputStream.use { jarEntryInput ->
                if (jarEntry.jarEntrySelector()) {
                    val classReader = ClassReader(jarEntryInput)
                    val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES)
                    val instrumentationVisitor = classWriter.visitorAdapterFunc()
                    classReader.accept(instrumentationVisitor, ClassReader.SKIP_FRAMES)
                    jarFlinger.addEntry(jarEntry.name, classWriter.toByteArray().inputStream())
                } else {
                    jarFlinger.addEntry(jarEntry.name, jarEntryInput.readBytes().inputStream())
                }
            }
        }
    }
}