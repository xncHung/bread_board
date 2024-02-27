package io.xnchung.breadboard.sample.ui;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import java.util.ArrayList;
import java.util.HashMap;

import io.xnchung.breadboard.annotations.CollectionType;
import io.xnchung.breadboard.annotations.Component;
import io.xnchung.breadboard.annotations.Param;
import io.xnchung.breadboard.annotations.TypeToken;
import io.xnchung.breadboard.sample.HexInt;
import io.xnchung.breadboard.sample.R;
import io.xnchung.breadboard.sample.bizhome.ColorParam;

@Component(
        name = "homeJava",
        description = "the MainActivity of sample app",
        documentation = "the prd or design url,",
        owners = {"david", "zhangsan"},
        routes = {"the routeUri"},
        tags = {"main biz", "home"},
        params = {
                @Param(key = "tesString", rawType = String.class, defaultValue = "testStringDefaultValue"),
                @Param(key = "testInt", rawType = int.class, defaultValue = "1"),
                @Param(key = "testFloat", rawType = float.class, defaultValue = "1.0"),
                @Param(key = "testDouble", rawType = double.class, defaultValue = "3.4"),
                @Param(key = "testBoolean", rawType = boolean.class, defaultValue = "true"),
                @Param(key = "testHexInt", rawType = int.class, defaultValue = "1a4fdd", qualifier = HexInt.class),
                @Param(key = "testColor", rawType = int.class, defaultValue = "#ff0000", qualifier = ColorParam.class),
                @Param(key = "testStringArray", rawType = String.class, collectionType = CollectionType.Array, defaultValue = "[\"testStringArrayDefaultValue1\", \"testStringArrayDefaultValue2\"]"),
                @Param(key = "testIntArray", rawType = int.class, collectionType = CollectionType.Array, defaultValue = "[\"1\", 2]"),
                @Param(key = "testFloatArray", rawType = Float.class, collectionType = CollectionType.Array, defaultValue = "[\"1.0\", 2.0f]"),
                @Param(key = "testDoubleArray", rawType = Double.class, collectionType = CollectionType.Array, defaultValue = "[\"1.0\", \"2.0\", 3.0]"),
                @Param(key = "testBooleanArray", rawType = Boolean.class, collectionType = CollectionType.Array, defaultValue = "[\"true\", false, true]"),
                @Param(key = "testHexIntArray", rawType = Integer.class, collectionType = CollectionType.Array, defaultValue = "[\"1a4fdd\", \"4df3ee\"]", qualifier = HexInt.class),
                @Param(key = "testColorArray", rawType = int.class, collectionType = CollectionType.Array, defaultValue = "[\"#ff0000\", \"#00ff00\", \"#0000ff\"]", qualifier = ColorParam.class),
                @Param(key = "testStringArrayList", rawType = String.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"testStringArrayListDefaultValue1\", \"testStringArrayListDefaultValue2\"]"),
                @Param(key = "testIntArrayList", rawType = int.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1\", 2]"),
                @Param(key = "testFloatArrayList", rawType = Float.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1.0\", 2.0]"),
                @Param(key = "testDoubleArrayList", rawType = Double.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1.0\", \"2.0\", 3.0]"),
                @Param(key = "testBooleanArrayList", rawType = Boolean.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"true\", false, true]"),
                @Param(key = "testHexIntArrayList", rawType = int.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"1a4fdd\", \"4df3ee\"]", qualifier = HexInt.class),
                @Param(key = "testColorArrayList", rawType = int.class, collectionType = CollectionType.ArrayList, defaultValue = "[\"#ff0000\", \"#00ff00\", \"#0000ff\"]", qualifier = ColorParam.class),
                @Param(key = "testHashMap",
                        rawType = HashMap.class,
                        typeArgs = {@TypeToken(rawType = String.class), @TypeToken(rawType = ArrayList.class, typeArgs = {@TypeToken(rawType = int.class)})}
                )
        }
)

public class SampleActivityWithJava extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_java);
    }
}