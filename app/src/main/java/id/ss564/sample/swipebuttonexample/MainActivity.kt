package id.ss564.sample.swipebuttonexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.ss564.lib.slidingbutton.SlidingButton

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val slidingButton = findViewById<SlidingButton>(R.id.slidingButton)
        slidingButton.setOnStateChangeListener {
            if (it) {
                slidingButton.postDelayed({
                    slidingButton.changeState(false, true)
                }, 3000L)
            }
        }
    }
}
