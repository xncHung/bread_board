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
import io.xnchung.breadboard.converters.ConverterHolder
import io.xnchung.breadboard.launcher.ui.theme.BreadBoardTheme
import io.xnchung.breadboard.model.SampleContract

abstract class BaseBreadBoardLauncherActivity : ComponentActivity() {
    companion object {
        private const val TAG = "BreadBoardLauncher"
    }

    protected abstract val allSamples: List<SampleContract>
    protected abstract val allExtConverters: Map<String, List<ConverterHolder>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: samplers=$allSamples")
        Log.d(TAG, "onCreate: converters=${allExtConverters}")
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