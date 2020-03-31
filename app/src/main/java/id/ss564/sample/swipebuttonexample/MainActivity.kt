package id.ss564.sample.swipebuttonexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        slidingButton.setOnStatusChangeListener {
            Toast.makeText(this, "Status : $it", Toast.LENGTH_SHORT).show()
        }
    }
}
