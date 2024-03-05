package io.xnchung.breadboard.launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.converters.ConverterHolder
import io.xnchung.breadboard.model.SampleContract
import javax.inject.Inject

class BreadBoardLauncherActivity : ComponentActivity() {
    companion object {
        private const val TAG = "BreadBoardLauncher"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) {
            Log.e(TAG, "Application is not debuggable. Breadboard is disabled.")
            finish()
            return
        }
        val isHiltApp = try {
//            hilt is depend with compileOnly,we have to use reflection
            Class.forName("dagger.hilt.internal.GeneratedComponentManager").isInstance(application)
        } catch (e: Exception) {
            false
        }
        val targetClz =
            if (isHiltApp) {
                try {
//                    Even if it is a Hilt app, it is possible that the developer will forcibly specify instrumentation mode
//                    see []
                    Class.forName("io.xnchung.breadboard.launcher.BreadBoardLauncherActivityForDI")
                } catch (e: Exception) {
                    BreadBoardLauncherWithInstrumentationActivity::class.java
                }
            } else {
                BreadBoardLauncherWithInstrumentationActivity::class.java
            }
        Intent(intent).also {
            it.setClass(this, targetClz)
            startActivity(it)
        }
    }
}

class BreadBoardLauncherWithInstrumentationActivity : BaseBreadBoardLauncherActivity() {
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
