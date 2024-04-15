package io.xnchung.breadboard.converters

import io.xnchung.breadboard.model.ParamHolderContract
import io.xnchung.breadboard.model.SampleContract

internal object ConverterRepo {
    private val resultMap = mutableMapOf<SampleArgPair, Any?>()

    suspend fun <T> parseArg(sample: SampleContract, paramHolder: ParamHolderContract<T>, input: String) {
        ConverterCore.convertParam(paramHolder, input).also {
            resultMap[SampleArgPair(sample.id, paramHolder.key, input)] = it
        }
    }

    fun <T> getAndRemoveArg(sample: SampleContract, paramHolder: ParamHolderContract<T>, input: String): T? {
        return resultMap.remove(SampleArgPair(sample.id, paramHolder.key, input)) as? T
    }
}

internal data class SampleArgPair(
    val sampleId: String,
    val argName: String,
    val input: String
)