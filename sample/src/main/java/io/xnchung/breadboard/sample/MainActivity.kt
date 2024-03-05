package io.xnchung.breadboard.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.xnchung.breadboard.annotations.CollectionType
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.Param
import io.xnchung.breadboard.annotations.TypeToken
import io.xnchung.breadboard.sample.bizhome.ColorParam
import io.xnchung.breadboard.sample.ui.theme.BreadBoardTheme

@Component(
    name = "home",
    description = "the MainActivity of sample app",
    documentation = """the prd or design url"""",
    owners = ["david", "zhangsan"],
    routes = ["the routeUri"],
    tags = ["main biz", "home"],
    params = [
        Param(key = "testString", rawType = String::class, defaultValue = "testStringDefaultValue"),
        Param(key = "testInt", rawType = Int::class, defaultValue = "1"),
        Param(key = "testFloat", rawType = Float::class, defaultValue = "1.0"),
        Param(key = "testDouble", rawType = Double::class, defaultValue = "3.4"),
        Param(key = "testBoolean", rawType = Boolean::class, defaultValue = "true"),
        Param(key = "testHexInt", rawType = Int::class, defaultValue = "1a4fdd", qualifier = HexInt::class),
        Param(key = "testColor", rawType = Int::class, defaultValue = "#ff0000", qualifier = ColorParam::class),
        Param(
            key = "testStringArray", rawType = String::class, collectionType = CollectionType.Array, defaultValue = """
           ["testStringArrayDefaultValue1","testStringArrayDefaultValue2"]
        """
        ),
        Param(
            key = "testIntArray", rawType = Int::class, collectionType = CollectionType.Array, defaultValue = """
           ["1",2]
        """
        ),
        Param(
            key = "testFloatArray", rawType = Float::class, collectionType = CollectionType.Array, defaultValue = """
           ["1.0",2.0]
        """
        ),
        Param(
            key = "testDoubleArray", rawType = Double::class, collectionType = CollectionType.Array, defaultValue = """
           ["1.0","2.0",3.0]
        """
        ),
        Param(
            key = "testBooleanArray", rawType = Boolean::class, collectionType = CollectionType.Array, defaultValue = """
           ["true",false,true]
        """
        ),
        Param(
            key = "testHexIntArray", rawType = Int::class, collectionType = CollectionType.Array, defaultValue = """
           ["1a4fdd","4df3ee"] 
        """, qualifier = HexInt::class
        ),
        Param(
            key = "testColorArray", rawType = Int::class, collectionType = CollectionType.Array, defaultValue = """
            ["#ff0000","#00ff00","#0000ff"]
        """, qualifier = ColorParam::class
        ),

        Param(
            key = "testStringArrayList", rawType = String::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["testStringArrayListDefaultValue1","testStringArrayListDefaultValue2"]
        """
        ),
        Param(
            key = "testIntArrayList", rawType = Int::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["1",2]
        """
        ),
        Param(
            key = "testFloatArrayList", rawType = Float::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["1.0",2.0]
        """
        ),
        Param(
            key = "testDoubleArrayList", rawType = Double::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["1.0","2.0",3.0]
        """
        ),
        Param(
            key = "testBooleanArrayList", rawType = Boolean::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["true",false,true]
        """
        ),
        Param(
            key = "testHexIntArrayList", rawType = Int::class, collectionType = CollectionType.ArrayList, defaultValue = """
           ["1a4fdd","4df3ee"] 
        """, qualifier = HexInt::class
        ),
        Param(
            key = "testColorArrayList", rawType = Int::class, collectionType = CollectionType.ArrayList, defaultValue = """
            ["#ff0000","#00ff00","#0000ff"]
        """, qualifier = ColorParam::class
        ),
        Param(
            key = "testHashMap",
            rawType = HashMap::class,
            typeArgs = [TypeToken(rawType = String::class, nullable = true), TypeToken(
                rawType = ArrayList::class,
                typeArgs = [TypeToken(rawType = Int::class)]
            )],
            nullable = true
        )
    ]
)
//@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Component("test compose")
@Composable
@Preview(showBackground = true)
fun Greeting(name: String = "hahah", modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

class Test {
    @Component("test")
    fun test(x: Any = "defaultArg"): Unit {


    }

    @Component("test")
    fun test(x: Any? = "defaultArg", y: String): Unit {


    }

    @Component("test")
    fun test(x: Array<String>): Unit {
    }

    @Component("test")
    fun test(x: Array<Int>): Unit {

    }

    @Component("test")
    fun test(x: Array<Array<String>>): Unit {
    }

    @Component("test")
    fun test(x: Array<Array<Int>>): Unit {

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BreadBoardTheme {
        Greeting("Android")
    }
}
