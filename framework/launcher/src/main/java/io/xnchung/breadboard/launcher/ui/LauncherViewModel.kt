package io.xnchung.breadboard.launcher.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.converters.ConverterHolder
import io.xnchung.breadboard.launcher.AllSamplesHolder
import io.xnchung.breadboard.launcher.Constants
import io.xnchung.breadboard.model.SampleContract

abstract class LauncherViewModelContract : ViewModel() {
    abstract val stateHandle: SavedStateHandle
    abstract val allSamples: List<SampleContract>
    abstract val allExtConverters: Collection<ConverterHolder>
    val targetId by lazy {
        stateHandle.get<String>(Constants.TARGET_ID)
    }
}

abstract class LauncherViewModel(
    final override val allExtConverters: Collection<ConverterHolder>,
    final override val stateHandle: SavedStateHandle,
    final override val allSamples: List<SampleContract>
) : LauncherViewModelContract() {

    init {
        ConverterCore.fillDiConverters(allExtConverters)
    }
}

//外部的 converter 已经由插桩预埋好
class LauncherViewModelForInstrumentation(stateHandle: SavedStateHandle) : LauncherViewModel(emptyList(), stateHandle, AllSamplesHolder.allSamples) {
}