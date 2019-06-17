package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.gson.Gson
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.Flash
import com.otaliastudios.cameraview.PictureResult
import es.jepp.legomachinelearning.*
import es.jepp.legomachinelearning.data.CsvToDataConverter
import es.jepp.legomachinelearning.data.LinearRegressionTools
import es.jepp.legomachinelearning.data.TrainedModel
import es.jepp.legomachinelearning.imagelogic.CameraService
import es.jepp.legomachinelearning.imagelogic.ImageDataReadyHandler
import es.jepp.legomachinelearning.robotlogic.FakeRobotController
import es.jepp.legomachinelearning.robotlogic.NxtRobotController
import es.jepp.legomachinelearning.robotlogic.RobotController
import es.jepp.legomachinelearning.robotlogic.RobotHasSteeredHandler
import kotlinx.android.synthetic.main.activity_drive.*
import java.io.File

class DriveActivity : Activity() {
    private var robotController: RobotController? = null
    private var cameraService: CameraService? = null

    private var trainedModel: TrainedModel? = null

    private var isDriving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drive)

        driveContainer.visibility = View.GONE
        initializeContainer.visibility = View.VISIBLE

        stopDrivingButton.isEnabled = false

        setTrainedModel()

        movableLine.setIsHorizontal(true)
        movableLine.setCanMove(false)
        movableLine.setDistanceInPixelsFromTopOrLeft(trainedModel!!.sourceImagePositionY)

        steeringLine.setIsHorizontal(false)
        steeringLine.setCanMove(false)
        steeringLine.setDistanceInPercentFromTopOrLeft(50f)
        steeringLine.setColor(Color.CYAN)

        //val actualRobotController = FakeRobotController
        val actualRobotController = NxtRobotController
        robotController = RobotController(
            actualRobotController,
            object : RobotHasSteeredHandler {
                override fun robotHasSteered(newAngleInPercent: Float) {}
            })

        camera.addCameraListener(object: CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                cameraService?.onPictureTaken(result)
            }
        })

        cameraFlashOnCheckBox.setOnCheckedChangeListener(object: RadioGroup.OnCheckedChangeListener,
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    camera.flash = Flash.TORCH
                } else {
                    camera.flash = Flash.OFF
                }
            }

            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) { }
        })

        initializeSteeringButton.setOnClickListener { startInitialization() }
        startDriveButton.setOnClickListener { startDriving() }
        stopDrivingButton.setOnClickListener { stopDriving() }
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

            driveContainer.visibility = View.VISIBLE
            initializeContainer.visibility = View.GONE
        }

        initializeSteeringButton.isEnabled = true
    }

    private fun startDriving() {
        startDriveButton.isEnabled = false

        isDriving = true
        robotController!!.startDriving()

        cameraService = CameraService(
            camera.width,
            camera.height,
            arrayOf(
                Point(trainedModel!!.sourceImagePositionX, trainedModel!!.sourceImagePositionY),
                Point(
                    trainedModel!!.sourceImagePositionX + trainedModel!!.sourceImageWidth,
                    trainedModel!!.sourceImagePositionY
                ),
                Point(
                    trainedModel!!.sourceImagePositionX,
                    trainedModel!!.sourceImagePositionY + trainedModel!!.sourceImageHeight
                ),
                Point(
                    trainedModel!!.sourceImagePositionX + trainedModel!!.sourceImageWidth,
                    trainedModel!!.sourceImagePositionY + trainedModel!!.sourceImageHeight
                )
            ),
            object : ImageDataReadyHandler {
                override fun imageReady(
                    processedImageWidth: Int,
                    processedImageHeight: Int,
                    sourceImagePositionX: Int,
                    sourceImagePositionY: Int,
                    sourceImageWidth: Int,
                    sourceImageHeight: Int,
                    image: Bitmap,
                    grayscalePixels: IntArray
                ) {
                    if (isDriving) {
                        converted_image.setImageBitmap(image)
                        steerCar(grayscalePixels)
                    }
                }
            })

        camera.takePictureSnapshot()

        stopDrivingButton.isEnabled = true
    }

    private fun stopDriving() {
        stopDrivingButton.isEnabled = false

        isDriving = false
        robotController!!.stopDriving()

        startDriveButton.isEnabled = true
    }

    private fun steerCar(grayscalePixels: IntArray){
        var features = CsvToDataConverter.generateFeaturesFromGrayscalePixels(grayscalePixels)
        val h = LinearRegressionTools.computeHypothesis(features, trainedModel!!.theta)
        var steeringAngle = h * 50f + 50f

        if (isDriving) {
            robotController?.steer(steeringAngle)
            steeringLine.setDistanceInPercentFromTopOrLeft(100 - steeringAngle)

            if (isDriving) {
                camera.takePictureSnapshot()
            }
        }
    }

    private fun setTrainedModel(){
        val file = getDataFile()
        val content = file.readText()
        trainedModel = Gson().fromJson(content, TrainedModel::class.java)
    }

    private fun getDataFile(): File {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val modelName = getModelName()
        val fileName = modelName + StaticSettings.TRAINED_MODEL_FILE_ENDING
        return File(dataDirectory, fileName)
    }

    private fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }
}
