package io.xnchung.breadboard.launcher

import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.converters.ConverterHolder
import io.xnchung.breadboard.model.SampleContract
import javax.inject.Inject

class BreadBoardLauncherActivity : BaseBreadBoardLauncherActivity() {
    override val allSamples by lazy {
        AllSamplesHolder.allSamples
    }
    override val allExtConverters by lazy {
        ConverterCore.allExtConverterFactories
    }
}

abstract class BreadBoardLauncherActivityWithDIDeclaration : BaseBreadBoardLauncherActivity() {

    @Inject
    lateinit var injectedSamples: Set<@JvmSuppressWildcards SampleContract>

    @Inject
    lateinit var injectedConverters: Set<@JvmSuppressWildcards ConverterHolder>
    override val allSamples: List<SampleContract> by lazy {
        injectedSamples.toList()
    }
    override val allExtConverters by lazy {
        ConverterCore.fillDiConverters(injectedConverters)
        ConverterCore.allExtConverterFactories
    }
}
