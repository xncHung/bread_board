package io.xnchung.breadboard.sample.bizhome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.xnchung.breadboard.annotations.Component
import io.xnchung.breadboard.annotations.Param

@Component(
    name = "home",
    description = "the MainActivity of sample app",
    params = [
        Param("test", String::class)
    ]
)
class BizHomeMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biz_home_main)
    }
}


