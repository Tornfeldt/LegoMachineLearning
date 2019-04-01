package es.jepp.legomachinelearning

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_train.*

class TrainActivity : Activity() {
    private val isInitialized = false
    private var robotController: RobotController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        robotController = RobotController(BasicRobotController)
    }

    override fun onDestroy() {
        super.onDestroy()

        robotController?.disconnect()
    }

    fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }
}
