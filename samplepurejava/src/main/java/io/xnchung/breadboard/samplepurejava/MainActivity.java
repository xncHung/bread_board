package io.xnchung.breadboard.samplepurejava;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

import io.xnchung.breadboard.annotations.CollectionType;
import io.xnchung.breadboard.annotations.Component;
import io.xnchung.breadboard.annotations.Param;
import io.xnchung.breadboard.annotations.TypeToken;

@Component(
        name = "PureJava",
        description = "the MainActivity of sample app",
        documentation = "the prd or design url,",
        owners = {"david", "zhangsan"},
        tags = {"main biz", "home"},
        params = {
                @Param(key = "testString", rawType = String.class, defaultValue = "testStringDefaultValue"),
                @Param(key = "testInt", rawType = int.class, defaultValue = "1"),
                @Param(key = "testFloat", rawType = float.class, defaultValue = "1.0"),
                @Param(key = "testDouble", rawType = double.class, defaultValue = "3.4"),
                @Param(key = "testBoolean", rawType = boolean.class, defaultValue = "true"),
                @Param(key = "testStringArray", rawType = String.class, collectionType = CollectionType.Array, defaultValue = "[\"testStringArrayDefaultValue1\", \"testStringArrayDefaultValue2\"]"),
                @Param(key = "testIntArray", rawType = int.class, collectionType = CollectionType.Array, defaultValue = "[\"1\", 2]"),
                @Param(key = "testFloatArray", rawType = Float.class, collectionType = CollectionType.Array, defaultValue = "[\"1.0\", 2.0f]"),
                @Param(key = "testDoubleArray", rawType = Double.class, collectionType = CollectionType.Array, defaultValue = "[\"1.0\", \"2.0\", 3.0]"),
                @Param(key = "testBooleanArray", rawType = Boolean.class, collectionType = CollectionType.Array, defaultValue = "[\"true\", false, true]"),
                @Param(key = "testStringArrayList", rawType = String.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"testStringArrayListDefaultValue1\", \"testStringArrayListDefaultValue2\"]"),
                @Param(key = "testIntArrayList", rawType = int.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1\", 2]"),
                @Param(key = "testFloatArrayList", rawType = Float.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1.0\", 2.0]"),
                @Param(key = "testDoubleArrayList", rawType = Double.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1.0\", \"2.0\", 3.0]"),
                @Param(key = "testBooleanArrayList", rawType = Boolean.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"true\", false, true]"),
                @Param(key = "testHashMap",
                        rawType = HashMap.class,
                        nullable = true,
                        typeArgs = {@TypeToken(rawType = String.class), @TypeToken(rawType = ArrayList.class, typeArgs = {@TypeToken(rawType = int.class)})}
                )
        }
)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}