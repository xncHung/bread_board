package io.xnchung.breadboard.launcher

import io.xnchung.breadboard.model.Sample

internal object AllSamplesHolder {
    /**
     * it will be filled by gradle plugin
     */
    val allSamples by lazy {
        mutableListOf<Sample>().also {
            fillSamples(it)
        }.toList()
    }

    private fun fillSamples(samples: MutableList<Sample>) {
    }
}
