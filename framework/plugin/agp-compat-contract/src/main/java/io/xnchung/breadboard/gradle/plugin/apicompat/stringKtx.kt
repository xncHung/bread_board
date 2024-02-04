package io.xnchung.breadboard.gradle.plugin.apicompat

import java.util.*

fun String.toCapitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }