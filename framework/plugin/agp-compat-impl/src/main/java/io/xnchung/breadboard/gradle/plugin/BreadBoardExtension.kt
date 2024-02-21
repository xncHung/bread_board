package io.xnchung.breadboard.gradle.plugin

import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.apicompat.CollectDataStrategy


open class BreadBoardExtensionImpl : BreadBoardExtension {
    override var excludeExternalLib: Boolean = true
}