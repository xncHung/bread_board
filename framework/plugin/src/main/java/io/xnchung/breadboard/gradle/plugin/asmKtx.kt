package io.xnchung.breadboard.gradle.plugin

import org.objectweb.asm.Opcodes

internal val Int.accessIsPublic: Boolean get() = this.and(Opcodes.ACC_PUBLIC) != 0
internal val Int.accessIsStatic: Boolean get() = this.and(Opcodes.ACC_STATIC) != 0
