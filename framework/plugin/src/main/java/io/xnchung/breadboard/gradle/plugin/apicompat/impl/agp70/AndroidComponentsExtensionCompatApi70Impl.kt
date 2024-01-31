package io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp70

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.android.build.api.variant.LibraryVariant
import com.android.build.gradle.AppExtension
import io.xnchung.breadboard.gradle.plugin.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.apicompat.AndroidComponentsExtensionCompat
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat
import io.xnchung.breadboard.gradle.plugin.instrumentation.CollectConverterFactoriesAndSamplesTransform
import org.gradle.api.Project

class AndroidComponentsExtensionCompatApi70Impl(private val project: Project) : AndroidComponentsExtensionCompat {
    override fun onAllVariants(block: ComponentCompat.() -> Unit) {
        val actual = project.extensions.getByType(AndroidComponentsExtension::class.java)
        actual.onVariants { variant ->
            ComponentCompatApi70Impl(variant).block()
            when (variant) {
                is ApplicationVariant -> variant.androidTest
                is LibraryVariant -> variant.androidTest
                else -> null
            }?.let { ComponentCompatApi70Impl(it).block() }
        }
    }

    override fun collectAndInjectConverterProvidersAndComponents(project: Project, breadBoardExtension: BreadBoardExtension) {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(CollectConverterFactoriesAndSamplesTransform(project, breadBoardExtension))
    }
}
