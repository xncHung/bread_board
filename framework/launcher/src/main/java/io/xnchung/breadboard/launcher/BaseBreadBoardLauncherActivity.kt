package io.xnchung.breadboard.launcher

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.xnchung.breadboard.converters.ConverterCore
import io.xnchung.breadboard.launcher.ui.LauncherViewModelContract
import io.xnchung.breadboard.launcher.ui.theme.BreadBoardTheme

abstract class BaseBreadBoardLauncherActivity : ComponentActivity() {
    companion object {
    }

    private val TAG = this::class.java.simpleName
    protected abstract val viewModel: LauncherViewModelContract
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate: samplers=${viewModel.allSamples}")
        Log.v(TAG, "onCreate: converters=${ConverterCore.allExtConverterFactories}")
        Log.d(TAG, "onCreate: targetId=${viewModel.targetId}")
        setContent {
            BreadBoardTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
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