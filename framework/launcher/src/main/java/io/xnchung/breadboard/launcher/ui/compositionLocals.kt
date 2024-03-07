package io.xnchung.breadboard.launcher.ui

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSize = compositionLocalOf<WindowSizeClass> { error("No window size calculated") }
