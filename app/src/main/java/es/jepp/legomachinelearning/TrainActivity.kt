package es.jepp.legomachinelearning

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_train.*

class TrainActivity : Activity() {
    private val isInitialized = false
    private var robotController: RobotController? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        robotController = RobotController(BasicRobotController)

        trainContainer.visibility = View.GONE
        initializeContainer.visibility = View.VISIBLE
        startTrainingButton.isEnabled = true
        stopTrainingButton.isEnabled = false

        initializeSteeringButton.setOnClickListener { startInitialization() }
        startTrainingButton.setOnClickListener { startTraining() }
        stopTrainingButton.setOnClickListener { stopTraining() }
    }

    private fun startInitialization() {
        initializeSteeringButton.isEnabled = false

        val connected = robotController?.tryConnect()!!

        if (!connected) {
            Toast.makeText(this, "Unable to connect to robot", Toast.LENGTH_LONG).show()
        } else {
            robotController?.initializeSteering()

            trainContainer.visibility = View.VISIBLE
            initializeContainer.visibility = View.GONE
        }

        initializeSteeringButton.isEnabled = true
    }

    private fun startTraining() {
        startTrainingButton.isEnabled = false
        stopTrainingButton.isEnabled = true

        robotController?.startTraining()
    }

    private fun stopTraining() {
        startTrainingButton.isEnabled = true
        stopTrainingButton.isEnabled = false

        robotController?.stopTraining()
    }

    override fun onDestroy() {
        super.onDestroy()

        robotController?.disconnect()
    }

    fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }
}
