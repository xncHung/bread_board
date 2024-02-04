package io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation

import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.AllSamplesInstrumentationMethodVisitor
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.AsmMethodInfo
import io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation.ConverterProvidersInstrumentationMethodVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

 class ConverterProvidersInstrumentationVisitor(
    classWriter: ClassWriter,
    private val converterProviderMethodInfoList: List<AsmMethodInfo>,
    private val components: List<AsmMethodInfo>
) : ClassVisitor(Opcodes.ASM9, classWriter) {

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        return when {
            name == "fillConverterFactories" && descriptor == "(Ljava/util/Map;)V" -> {
                ConverterProvidersInstrumentationMethodVisitor(methodVisitor, converterProviderMethodInfoList)
            }

            name == "fillSamples" && descriptor == "(Ljava/util/List;)V" -> {
                AllSamplesInstrumentationMethodVisitor(methodVisitor, components)
            }

            else -> {
                methodVisitor
            }
        }
    }
}

