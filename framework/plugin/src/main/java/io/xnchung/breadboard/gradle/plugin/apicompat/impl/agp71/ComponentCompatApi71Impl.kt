package io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp71

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.FramesComputationMode
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.Component
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat

internal class ComponentCompatApi71Impl(private val component: Component) : ComponentCompat() {

    override val name: String = component.name
    override val buildType: String? = component.buildType
    override fun <T : InstrumentationParameters> transformClassesWith(
        classVisitorFactoryImplClass: Class<out AsmClassVisitorFactory<T>>,
        scope: InstrumentationScope,
        instrumentationParamsConfig: (T) -> Unit
    ) {
        component.transformClassesWith(classVisitorFactoryImplClass, scope, instrumentationParamsConfig)
    }

    override fun setAsmFramesComputationMode(mode: FramesComputationMode) {
        component.setAsmFramesComputationMode(mode)
    }
}
