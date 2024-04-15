package io.xnchung.breadboard.launcher.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import io.xnchung.breadboard.model.ParamHolderContract

@Stable
internal data class ParamDraft<T>(
    val holder: ParamHolderContract<T>,
    val input: MutableState<String> = mutableStateOf(holder.defaultValue)
) : ParamHolderContract<T> by holder
