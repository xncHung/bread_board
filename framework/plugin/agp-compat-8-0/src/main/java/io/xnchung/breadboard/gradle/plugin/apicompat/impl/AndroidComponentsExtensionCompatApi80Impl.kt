package io.xnchung.breadboard.gradle.plugin.apicompat.impl

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTest
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.BaseExtension
import io.xnchung.breadboard.gradle.plugin.apicompat.AndroidComponentsExtensionCompat
import io.xnchung.breadboard.gradle.plugin.apicompat.BreadBoardExtension
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat
import io.xnchung.breadboard.gradle.plugin.apicompat.toCapitalize
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class AndroidComponentsExtensionCompatApi80Impl(private val project: Project) : AndroidComponentsExtensionCompat {
    override fun onAllVariants(block: ComponentCompat.() -> Unit) {
        val actual = project.extensions.getByType(AndroidComponentsExtension::class.java)
        actual.onVariants { variant ->
            ComponentCompatApi80Impl(variant).block()
            (variant as? HasAndroidTest)?.androidTest?.let { ComponentCompatApi80Impl(it).block() }

        }
    }

    override fun collectAndInjectConverterProvidersAndComponents(
        project: Project,
        breadBoardExtension: BreadBoardExtension
    ) {
        onAllVariants {
            val baseExtension =
                requireNotNull(project.extensions.findByType(BaseExtension::class.java)) { "Android BaseExtension not found." }
            if (baseExtension.buildTypes.findByName(buildType!!)?.isDebuggable == true) {
                val taskProvider: TaskProvider<TransformTask> = project.tasks.register(
                    "CollectBreadboardComponentsFor${name.toCapitalize()}",
                    TransformTask::class.java
                )
                artifacts
                    .forScope(ScopedArtifacts.Scope.ALL)
                    .use(taskProvider)
                    .toTransform(
                        ScopedArtifact.CLASSES,
                        TransformTask::allJars,
                        TransformTask::allDirectories,
                        TransformTask::output
                    )
            }
        }
    }
}