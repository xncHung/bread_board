package io.xnchung.breadboard.gradle.plugin.instrumentation

import io.xnchung.breadboard.gradle.plugin.accessIsPublic
import io.xnchung.breadboard.gradle.plugin.accessIsStatic
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class CollectConverterProvidersAndComponentVisitor(
    private val sampleConsumer: (AsmMethodInfo) -> Unit,
    private val converterConsumer: (AsmMethodInfo) -> Unit
) : ClassVisitor(Opcodes.ASM9) {

    private lateinit var classFqn: String

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        classFqn = name.orEmpty()
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access.accessIsPublic && access.accessIsStatic && name?.startsWith("_breadboard_provide_") == true) {
            when (descriptor) {
                "()Lio/xnchung/breadboard/model/Sample;" -> {
                    sampleConsumer(AsmMethodInfo(classFqn, name, descriptor))
                }

                "()Lio/xnchung/breadboard/converters/ConverterHolder;" -> {
                    converterConsumer(AsmMethodInfo(classFqn, name, descriptor))
                }

                else -> {}
            }

        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}