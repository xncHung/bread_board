package io.xnchung.breadboard.gradle.plugin

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class CollectConverterProvidersAndComponentVisitor(val sampleConsumer: () -> Unit, val converterConsumer: () -> Unit) : ClassVisitor(Opcodes.ASM9) {
    private lateinit var classFqn: String

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        classFqn = name.orEmpty()
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, descriptor: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        if (access.accessIsPublic && access.accessIsStatic) {
            when (descriptor) {
                "()Lio/xnchung/breadboard/model/Sample;" -> {
                    Logging.getLogger("collect").log(
                        LogLevel.WARN,
                        "collect sample: access = $access, name = $classFqn.$name, descriptor = $descriptor, signature = $signature, exceptions = $exceptions"
                    )
                }

                "()Lio/xnchung/breadboard/converters/ConverterHolder;" -> {
                    Logging.getLogger("collect").log(
                        LogLevel.WARN,
                        "collect convert holder: access = $access, name = $classFqn.$name, descriptor = $descriptor, signature = $signature, exceptions = $exceptions"
                    )
                }

                else -> {}
            }

        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}