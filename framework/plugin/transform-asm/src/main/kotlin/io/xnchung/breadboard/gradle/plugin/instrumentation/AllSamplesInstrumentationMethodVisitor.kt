package io.xnchung.breadboard.io.xnchung.breadboard.gradle.plugin.instrumentation

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

 class AllSamplesInstrumentationMethodVisitor(
    private val methodVisitor: MethodVisitor,
    private val sampleProviderMethodInfos: List<AsmMethodInfo>
) : MethodVisitor(Opcodes.ASM9, methodVisitor) {
    override fun visitInsn(opcode: Int) {
        if (opcode in Opcodes.IRETURN..Opcodes.RETURN) {
            sampleProviderMethodInfos.forEach {
                methodVisitor.apply {
                    visitVarInsn(Opcodes.ALOAD, 1)
                    visitMethodInsn(Opcodes.INVOKESTATIC, it.owner, it.name, it.descriptor, false)
                    visitMethodInsn(
                        Opcodes.INVOKEINTERFACE,
                        "java/util/List",
                        "add",
                        "(Ljava/lang/Object;)Z",
                        true
                    )
                    visitInsn(Opcodes.POP)
                }
            }
        }
        super.visitInsn(opcode)
    }
}