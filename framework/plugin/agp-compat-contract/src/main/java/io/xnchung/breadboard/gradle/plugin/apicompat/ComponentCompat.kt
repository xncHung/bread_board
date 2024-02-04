package io.xnchung.breadboard.gradle.plugin.apicompat

import com.android.build.api.artifact.Artifacts
import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.Component

abstract class ComponentCompat {

    abstract val name: String

    /** Redeclaration of [com.android.build.api.variant.Component.buildType] */
    abstract val buildType: String?
    abstract val artifacts: Artifacts

}
