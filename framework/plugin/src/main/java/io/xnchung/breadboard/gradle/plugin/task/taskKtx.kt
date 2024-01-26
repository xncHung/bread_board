package io.xnchung.breadboard.gradle.plugin.task

import com.google.devtools.ksp.gradle.KspTaskJvm
import io.xnchung.breadboard.gradle.plugin.apicompat.ComponentCompat
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.process.CommandLineArgumentProvider
import java.util.Locale

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
        if (task.name == "ksp${variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}Kotlin" ||
            // Task names in shared/src/AndroidMain in KMP projects has a platform suffix.
            task.name == "ksp${variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}KotlinAndroid"
        ) {
            task.commandLineArgumentProviders.add(produceArgProvider.invoke(task))
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