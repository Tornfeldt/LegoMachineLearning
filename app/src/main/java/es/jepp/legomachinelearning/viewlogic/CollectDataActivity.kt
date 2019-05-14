package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.PictureResult
import es.jepp.legomachinelearning.*
import kotlinx.android.synthetic.main.activity_collect_data.*
import java.io.File

class CollectDataActivity : Activity() {
    private var robotController: RobotController? = null
    private var cameraService: CameraService? = null

    private var isCurrentlyTakingPicture = false
    private var latestSteeringAngle = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_data)

        val actualRobotController = FakeRobotController
        //val actualRobotController = NxtRobotController
        robotController = RobotController(
            actualRobotController,
            object : RobotHasSteeredHandler {
                override fun robotHasSteered(newAngleInPercent: Float) {
                    if (!isCurrentlyTakingPicture) {
                        isCurrentlyTakingPicture = true
                        latestSteeringAngle = newAngleInPercent
                        camera.takePictureSnapshot()
                    }
                }
            })

        collectDataContainer.visibility = View.GONE
        initializeContainer.visibility = View.VISIBLE
        startCollectButton.isEnabled = true
        stopCollectButton.isEnabled = false

        initializeSteeringButton.setOnClickListener { startInitialization() }
        startCollectButton.setOnClickListener { startCollectData() }
        stopCollectButton.setOnClickListener { stopCollectData() }

        camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                cameraService?.onPictureTaken(result)
                isCurrentlyTakingPicture = false
            }
        })
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

            collectDataContainer.visibility = View.VISIBLE
            initializeContainer.visibility = View.GONE
        }

        initializeSteeringButton.isEnabled = true
    }

    private fun startCollectData() {
        startCollectButton.isEnabled = false
        stopCollectButton.isEnabled = true

        cameraService = CameraService(
            camera.width,
            camera.height,
            rectangleDrawView.listPoints(),
            object : ImageDataReadyHandler {
                override fun imageReady(
                    width: Int,
                    height: Int,
                    positionX: Int,
                    positionY: Int,
                    image: Bitmap,
                    grayscalePixels: IntArray
                ) {
                    converted_image.setImageBitmap(image)

                    writePixelsToDataFile(grayscalePixels, positionX, positionY, width, height)

                    val mirroredGrayscalePixels = mirrorPixelArray(grayscalePixels, width, height)
                    writePixelsToDataFile(mirroredGrayscalePixels, positionX, positionY, width, height)
                }
            })

        robotController?.startCollectData()
    }

    private fun writePixelsToDataFile(pixels: IntArray, positionX: Int, positionY: Int, width: Int, height: Int){
        var csvLine = "$positionX,$positionY,$width,$height,$latestSteeringAngle"
        for (pixel in pixels) {
            csvLine += ",$pixel"
        }
        csvLine += "\n"

        addTextToDataFile(csvLine)
    }

    private fun mirrorPixelArray(pixels: IntArray, width: Int, height: Int): IntArray{
        var mirrored = IntArray(pixels.size)
        for (j in 0 until height){
            for (i in 0 until width) {
                var value = pixels[j * height + i]
                mirrored[j * height + (width - (i + 1))] = value
            }
        }
        return mirrored
    }

    private fun stopCollectData() {
        startCollectButton.isEnabled = true
        stopCollectButton.isEnabled = false

        robotController?.stopCollectData()
    }

    private fun addTextToDataFile(text: String){
        getDataFile().appendText(text)
    }

    private fun doesDataFileExist(): Boolean {
        return getDataFile().exists()
    }

    private fun getDataFile(): File {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val modelName = getModelName()
        val fileName = modelName + StaticSettings.DATA_FILE_ENDING
        return File(dataDirectory, fileName)
    }

    private fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }
}
