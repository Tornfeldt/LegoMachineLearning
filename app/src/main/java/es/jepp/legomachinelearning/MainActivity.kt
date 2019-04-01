package es.jepp.legomachinelearning

import android.app.Activity
import android.content.Intent
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        trainButton.setOnClickListener {
            val intent = Intent(this, SaveTrainNameActivity::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
        }
    }
}
