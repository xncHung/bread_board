package io.xnchung.breadboard.gradle.plugin.task

import com.google.devtools.ksp.gradle.KspTaskJvm
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.internal.KaptTask

internal fun Project.addKspTaskProcessorOptions(
    variant: ComponentCompat,
    produceArgProvider: (Task) -> CommandLineArgumentProvider
) = plugins.withId("com.google.devtools.ksp") {
    checkClass("com.google.devtools.ksp.gradle.KspTaskJvm") {
        """
    The KSP plugin was detected to be applied but its task class could not be found.

    This is an indicator that the Breadboard Gradle Plugin is using a different class loader because
    it was declared at the root while KSP was declared in a sub-project. To fix this, declare
    both plugins in the same scope, i.e. either at the root (without applying them) or at the
    sub-projects.
    """.trimIndent()
    }
    tasks.withType(KspTaskJvm::class.java) { task ->
        if (task.name == "ksp${variant.name.capitalized()}Kotlin" ||
            // Task names in shared/src/AndroidMain in KMP projects has a platform suffix.
            task.name == "ksp${variant.name.capitalized()}KotlinAndroid"
        ) {
            task.commandLineArgumentProviders.add(produceArgProvider.invoke(task))
        }
    }
}

internal fun Project.addJavaTaskProcessorOptions(
    component: ComponentCompat,
    produceArgProvider: (Task) -> CommandLineArgumentProvider
) = tasks.withType(JavaCompile::class.java) { task ->
    if (task.name == "compile${component.name.capitalized()}JavaWithJavac") {
        task.options.compilerArgumentProviders.add(produceArgProvider.invoke(task))
    }
}

internal fun Project.addKaptTaskProcessorOptions(
    component: ComponentCompat,
    produceArgProvider: (Task) -> CommandLineArgumentProvider
) = plugins.withId("kotlin-kapt") {
    checkClass("org.jetbrains.kotlin.gradle.internal.KaptTask") {
        """
    The KAPT plugin was detected to be applied but its task class could not be found.

    This is an indicator that the Hilt Gradle Plugin is using a different class loader because
    it was declared at the root while KAPT was declared in a sub-project. To fix this, declare
    both plugins in the same scope, i.e. either at the root (without applying them) or at the
    sub-projects.
    """.trimIndent()
    }
    tasks.withType(KaptTask::class.java) { task ->
        if (task.name == "kapt${component.name.capitalized()}Kotlin" ||
            // Task names in shared/src/AndroidMain in KMP projects has a platform suffix.
            task.name == "kapt${component.name.capitalized()}KotlinAndroid"
        ) {
            val argProvider = produceArgProvider.invoke(task)
            // TODO: Update once KT-58009 is fixed.
            try {
                // Because of KT-58009, we need to add a `listOf(argProvider)` instead
                // of `argProvider`.
                task.annotationProcessorOptionProviders.add(listOf(argProvider))
            } catch (e: Throwable) {
                // Once KT-58009 is fixed, adding `listOf(argProvider)` will fail, we will
                // pass `argProvider` instead, which is the correct way.
                task.annotationProcessorOptionProviders.add(argProvider)
            }
        }
    }
}


private inline fun checkClass(fqn: String, msg: () -> String) {
    try {
        Class.forName(fqn)
    } catch (ex: ClassNotFoundException) {
        throw IllegalStateException(msg.invoke(), ex)
    }
}

internal fun Task.isKspTask(): Boolean = try {
    val kspTaskClass = Class.forName("com.google.devtools.ksp.gradle.KspTask")
    kspTaskClass.isAssignableFrom(this::class.java)
} catch (ex: ClassNotFoundException) {
    false
}