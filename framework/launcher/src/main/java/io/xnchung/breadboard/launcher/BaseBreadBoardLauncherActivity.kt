package io.xnchung.breadboard.launcher

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.launcher.ui.CatalogSettings
import io.xnchung.breadboard.launcher.ui.ComponentCatalogNavigation
import io.xnchung.breadboard.launcher.ui.LauncherViewModelContract
import io.xnchung.breadboard.launcher.ui.LocalWindowSize
import io.xnchung.breadboard.launcher.ui.theme.BreadBoardTheme

abstract class BaseBreadBoardLauncherActivity : AppCompatActivity() {
    companion object {
    }

    private val TAG = this::class.java.simpleName
    protected abstract val viewModel: LauncherViewModelContract

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate: samplers=${viewModel.allSamples}")
        Log.v(TAG, "onCreate: converters=${ConverterCore.allExtConverterFactories}")
        Log.d(TAG, "onCreate: targetId=${viewModel.targetId}")
        setContent {
            BreadBoardTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val sizeClass = calculateWindowSizeClass(this)
                    CompositionLocalProvider(LocalWindowSize provides sizeClass) {
                        ComponentCatalogNavigation(
                            startDestination = viewModel.targetId ?: Constants.CATALOG_HOME,
                            samples = viewModel.allSamples,
                            settings = CatalogSettings(true),
                            fragmentManager = supportFragmentManager
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BreadBoardTheme {
        Greeting("Android")
    }
}