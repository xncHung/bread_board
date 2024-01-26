package io.xnchung.breadboard.gradle.plugin.apicompat

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope

abstract class ComponentCompat {

    abstract val name: String

    /** Redeclaration of [com.android.build.api.variant.Component.buildType] */
    abstract val buildType: String?

    /** Redeclaration of [com.android.build.api.variant.Component.transformClassesWith] */
    abstract fun <T : InstrumentationParameters> transformClassesWith(
        classVisitorFactoryImplClass: Class<out AsmClassVisitorFactory<T>>,
        scope: InstrumentationScope,
        instrumentationParamsConfig: (T) -> Unit
    )

    /** Redeclaration of [com.android.build.api.variant.Component.setAsmFramesComputationMode] */
    abstract fun setAsmFramesComputationMode(mode: FramesComputationMode)

}
