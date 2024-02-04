package io.xnchung.breadboard.gradle.plugin

import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension


open class BreadBoardExtensionImpl : BreadBoardExtension {
    override var excludeExternalLib: Boolean = true
}