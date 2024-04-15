package io.xnchung.breadboard.sample.bizhome;

import android.graphics.Color;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;

import io.xnchung.breadboard.annotations.ConverterProvider;
import kotlin.jvm.functions.Function2;

public class ConverterInJava {
    @ConverterProvider
    @ColorParam
    public static Function2<String, Type, Integer> provideJavaConverter() {
        return (s, type) -> {
            try {
                return Color.parseColor(s);
            } catch (Exception e) {
                return null;
            }
        };
    }

    @ConverterProvider
    @ColorParam
    public static Function2<String, Type, Integer[]> provideJavaColorArrayConverter() {
        return (s, type) -> {
            try {
                Class<?> rawType = ConverterInKotlinKt.getRawType(type);
                if (rawType.isArray()) {

                    JSONArray jsonArray = new JSONArray(s);
                    Integer[] result = new Integer[jsonArray.length()];
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result[i] = Color.parseColor(jsonArray.getString(i));
                    }
                    return result;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        };
    }

    @ConverterProvider
    @ColorParam
    public static Function2<String, Type, ArrayList<Integer>> provideJavaColorArrayListConverter() {
        return (s, type) -> {
            try {
                Class<?> rawType = ConverterInKotlinKt.getRawType(type);
                if (rawType.isAssignableFrom(ArrayList.class)) {

                    JSONArray jsonArray = new JSONArray(s);
                    ArrayList<Integer> result = new ArrayList();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        result.add(Color.parseColor(jsonArray.getString(i)));
                    }
                    return result;
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        };
    }
}
