package io.xnchung.breadboard.gradle.plugin

import org.objectweb.asm.Opcodes

val Int.accessIsPublic: Boolean get() = this.and(Opcodes.ACC_PUBLIC) != 0
val Int.accessIsStatic: Boolean get() = this.and(Opcodes.ACC_STATIC) != 0
