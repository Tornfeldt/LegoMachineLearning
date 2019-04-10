package es.jepp.legomachinelearning

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.activity_train.*

class TrainActivity : Activity() {
    private var robotController: RobotController? = null
    private var cameraService: CameraService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        //robotController = RobotController(NxtRobotController)
        robotController = RobotController(FakeRobotController)

        trainContainer.visibility = View.GONE
        initializeContainer.visibility = View.VISIBLE
        startTrainingButton.isEnabled = true
        stopTrainingButton.isEnabled = false

        initializeSteeringButton.setOnClickListener { startInitialization() }
        startTrainingButton.setOnClickListener { startTraining() }
        stopTrainingButton.setOnClickListener { stopTraining() }

        camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                cameraService?.onPictureTaken(result)
            }
        })

        button.setOnClickListener {
            camera.takePictureSnapshot()
        }
    }

    override fun onResume() {
        super.onResume()
        camera.open()
    }

    override fun onPause() {
        super.onPause()
        camera.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        robotController?.disconnect()
        camera.destroy()
    }

    private fun startInitialization() {
        initializeSteeringButton.isEnabled = false

        val connected = robotController!!.tryConnect()

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

        cameraService = CameraService(camera.width, camera.height, rectangleDrawView.listPoints(), object: ImageDataReadyHandler{
            override fun imageReady(image: Bitmap, grayscalePixels: IntArray) {
                converted_image.setImageBitmap(image)
            }
        })

        robotController?.startTraining()
    }

    private fun stopTraining() {
        startTrainingButton.isEnabled = true
        stopTrainingButton.isEnabled = false

        robotController?.stopTraining()
    }

    fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }
}
