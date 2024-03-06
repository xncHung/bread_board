package io.xnchung.breadboard.launcher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import io.xnchung.breadboard.launcher.ui.LauncherViewModel
import io.xnchung.breadboard.launcher.ui.LauncherViewModelForInstrumentation

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
            finish()
        }
    }
}

class BreadBoardLauncherWithInstrumentationActivity : BaseBreadBoardLauncherActivity() {
    override val viewModel: LauncherViewModel by viewModels<LauncherViewModelForInstrumentation>()
}

