package io.xnchung.breadboard.launcher.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AdaptivePane(
    paddingValues: PaddingValues,
    start: @Composable (PaddingValues) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        start(paddingValues)
    }
}
