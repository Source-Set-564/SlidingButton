package id.ss564.sample.swipebuttonexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slidingButton.setOnStateChangeListener {
            if(it){
                Handler().postDelayed({
                    slidingButton.changeState(false,true)
                },3000L)
            }
        }
    }
}
