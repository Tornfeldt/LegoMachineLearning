package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import es.jepp.legomachinelearning.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        collectDataButton.setOnClickListener {
            val intent = Intent(this, SaveModelNameActivity::class.java)
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
        }
    }
}
