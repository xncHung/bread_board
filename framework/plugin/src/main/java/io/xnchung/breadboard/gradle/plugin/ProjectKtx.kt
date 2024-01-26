package io.xnchung.breadboard.gradle.plugin

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.BaseExtension
import org.gradle.api.Project

val Project.currentAGPVersion get() = extensions.getByType(AndroidComponentsExtension::class.java).pluginVersion
fun Project.baseExtension(): BaseExtension = requireNotNull(extensions.findByType(BaseExtension::class.java)) { "Android BaseExtension not found." }
fun Project.androidComponentsExtension(): AndroidComponentsExtension<*, *, *> = extensions.getByType(AndroidComponentsExtension::class.java)