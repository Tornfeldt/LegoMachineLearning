package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.os.Bundle
import es.jepp.legomachinelearning.R
import es.jepp.legomachinelearning.StaticSettings
import kotlinx.android.synthetic.main.activity_train.*

class TrainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)
    }
}
