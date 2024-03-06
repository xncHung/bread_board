package io.xnchung.breadboard.sample;

import io.xnchung.breadboard.annotations.Component;

public class SampleUtil {
    // TODO: 2024/3/6 singleInstance support, may be via a new annotation like InstanceFunc
    public static SampleUtil getInstance() {
        return new SampleUtil();
    }

    @Component(name = "singleInstance")
    public void test() {

    }
}
