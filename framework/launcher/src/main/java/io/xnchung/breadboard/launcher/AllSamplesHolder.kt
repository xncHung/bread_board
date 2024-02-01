package io.xnchung.breadboard.launcher

import io.xnchung.breadboard.model.SampleContract

internal object AllSamplesHolder {
    /**
     * it will be filled by gradle plugin
     */
    val allSamples by lazy {
        mutableListOf<SampleContract>().also {
            fillSamples(it)
        }.toList()
    }

    private fun fillSamples(samples: MutableList<SampleContract>) {
    }
}
