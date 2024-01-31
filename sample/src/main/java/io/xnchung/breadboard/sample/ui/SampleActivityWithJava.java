package io.xnchung.breadboard.sample.ui;

import android.os.Bundle;

import androidx.activity.ComponentActivity;

import io.xnchung.breadboard.annotations.Component;
import io.xnchung.breadboard.sample.R;

@Component(name = "activity with Java")
public class SampleActivityWithJava extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_with_java);
    }
}