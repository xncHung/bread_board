package io.xnchung.breadboard.gradle.plugin.apicompat.impl.agp71

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTest
import com.android.build.gradle.AppExtension
import io.xnchung.breadboard.gradle.plugin.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.CollectConverterFactoriesAndSamplesTransform
import io.xnchung.breadboard.gradle.plugin.apicompat.AndroidComponentsExtensionCompat
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat
import org.gradle.api.Project

class AndroidComponentsExtensionCompatApi71Impl(private val project: Project) : AndroidComponentsExtensionCompat {
    override fun onAllVariants(block: ComponentCompat.() -> Unit) {
        val actual = project.extensions.getByType(AndroidComponentsExtension::class.java)
        actual.onVariants { variant ->
            ComponentCompatApi71Impl(variant).block()
            (variant as? HasAndroidTest)?.androidTest?.let { ComponentCompatApi71Impl(it).block() }

        }
    }

    override fun collectAndInjectConverterProvidersAndComponents(project: Project, breadBoardExtension: BreadBoardExtension) {
        val appExtension = project.extensions.getByType(AppExtension::class.java)
        appExtension.registerTransform(CollectConverterFactoriesAndSamplesTransform(project, breadBoardExtension))
    }
}
