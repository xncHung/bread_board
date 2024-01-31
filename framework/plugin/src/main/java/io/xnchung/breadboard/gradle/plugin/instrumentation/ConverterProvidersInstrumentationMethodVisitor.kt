package io.xnchung.breadboard.gradle.plugin.instrumentation

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class ConverterProvidersInstrumentationMethodVisitor(
    private val methodVisitor: MethodVisitor,
    private val converterProviderMethodInfoList: List<AsmMethodInfo>
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitInsn(opcode: Int) {
        if (opcode in Opcodes.IRETURN..Opcodes.RETURN) {
            converterProviderMethodInfoList.forEach {
                methodVisitor.apply {
                    visitVarInsn(Opcodes.ALOAD, 0)
                    visitVarInsn(Opcodes.ALOAD, 1)
                    visitMethodInsn(Opcodes.INVOKESTATIC, it.owner, it.name, it.descriptor, false)
                    visitMethodInsn(
                        Opcodes.INVOKESPECIAL,
                        "io/xnchung/breadboard/converters/ConverterCore",
                        "newConverterHolder",
                        "(Ljava/util/Map;Lio/xnchung/breadboard/converters/ConverterHolder;)V",
                        false
                    )
                }
            }
        }
        super.visitInsn(opcode)
    }
}