package io.xnchung.breadboard.samplepurejava;

import android.graphics.Color;

import java.lang.reflect.Type;

import io.xnchung.breadboard.annotations.ConverterProvider;
import kotlin.jvm.functions.Function2;

public class ConverterInJava {
    @ConverterProvider
    @BinaryInt
    public static Function2<String, Type, Integer> provideJavaConverter() {
        return (Function2<String, Type, Integer>) (s, type) -> Color.parseColor(s);
    }
}
