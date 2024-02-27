package io.xnchung.breadboard.sample;

import io.xnchung.breadboard.annotations.Component;

public class SampleUtil {
    public static SampleUtil getInstance() {
        return new SampleUtil();
    }

    @Component(name = "singleInstance")
    public void test() {

    }
}
