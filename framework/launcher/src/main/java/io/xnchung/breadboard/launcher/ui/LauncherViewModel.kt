package io.xnchung.breadboard.launcher.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.converters.ConverterHolder
import io.xnchung.breadboard.launcher.AllSamplesHolder
import io.xnchung.breadboard.launcher.Constants
import io.xnchung.breadboard.model.SampleContract

abstract class LauncherViewModelContract(allExtConverters: Collection<ConverterHolder>) : ViewModel() {
    abstract val stateHandle: SavedStateHandle
    abstract val allSamples: List<SampleContract>
    abstract val targetId: String?

    init {
        ConverterCore.fillDiConverters(allExtConverters)
    }
}

abstract class LauncherViewModel(
    allExtConverters: Collection<ConverterHolder>,
    final override val stateHandle: SavedStateHandle,
    final override val allSamples: List<SampleContract>
) : LauncherViewModelContract(allExtConverters) {
    override val targetId = stateHandle.get<String>(Constants.START_ID)

}

//外部的 converter 已经由插桩预埋好
class LauncherViewModelForInstrumentation(stateHandle: SavedStateHandle) : LauncherViewModel(emptyList(), stateHandle, AllSamplesHolder.allSamples) {
}