package io.xnchung.breadboard.gradle.plugin

/**
 * configuration for BreadBoard plugin
 */
interface BreadBoardExtension {
    var incremental: Boolean
    var excludeExternalLib: Boolean
}

internal open class BreadBoardExtensionImpl : BreadBoardExtension {
    override var incremental: Boolean = false
    override var excludeExternalLib: Boolean = true
}