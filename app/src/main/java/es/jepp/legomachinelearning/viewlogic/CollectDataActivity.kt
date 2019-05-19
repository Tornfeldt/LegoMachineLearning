package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.Toast
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.Flash
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

        collectDataContainer.visibility = View.GONE
        initializeContainer.visibility = View.VISIBLE
        startCollectButton.isEnabled = true
        stopCollectButton.isEnabled = false
        continueCollectButton.isEnabled = false
        pauseCollectButton.isEnabled = false

        initializeSteeringButton.setOnClickListener { startInitialization() }
        startCollectButton.setOnClickListener { checkDataFileAndStartCollectData() }
        stopCollectButton.setOnClickListener { stopCollectData() }
        continueCollectButton.setOnClickListener{continueCollectData()}
        pauseCollectButton.setOnClickListener { pauseCollectData() }

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

    private fun checkDataFileAndStartCollectData() {
        if (doesDataFileExist()) {
            val filename = getDataFile().name
            AlertDialog.Builder(this)
                .setTitle("Delete data")
                .setMessage("Data file already exists. Do you want to delete $filename?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, object : DialogInterface.OnClickListener {

                    override fun onClick(dialog: DialogInterface, whichButton: Int) {
                        deleteDataFile()
                        startCollectData()
                    }
                })
                .setNegativeButton(android.R.string.no, null).show()
        } else {
            startCollectData()
        }
    }

    private fun startCollectData() {
        startCollectButton.isEnabled = false
        stopCollectButton.isEnabled = true
        continueCollectButton.isEnabled = false
        pauseCollectButton.isEnabled = true

        cameraService = CameraService(
            camera.width,
            camera.height,
            rectangleDrawView.listPoints(),
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
                    converted_image.setImageBitmap(image)

                    writePixelsToDataFile(grayscalePixels, latestSteeringAngle, processedImageWidth, processedImageHeight, sourceImagePositionX, sourceImagePositionY, sourceImageWidth, sourceImageHeight)

                    if (addMirroredDataCheckBox.isChecked){
                        val latestSteeringAngleMirrored = 100 - latestSteeringAngle
                        val mirroredGrayscalePixels = mirrorPixelArray(grayscalePixels, processedImageWidth, processedImageHeight)
                        writePixelsToDataFile(mirroredGrayscalePixels, latestSteeringAngleMirrored, processedImageWidth, processedImageHeight, sourceImagePositionX, sourceImagePositionY, sourceImageWidth, sourceImageHeight)
                    }
                }
            })

        robotController?.startCollectData()
    }

    private fun continueCollectData(){
        startCollectButton.isEnabled = false
        stopCollectButton.isEnabled = true
        continueCollectButton.isEnabled = false
        pauseCollectButton.isEnabled = true

        robotController?.startCollectData()
    }

    private fun pauseCollectData() {
        startCollectButton.isEnabled = false
        stopCollectButton.isEnabled = true
        continueCollectButton.isEnabled = true
        pauseCollectButton.isEnabled = false

        robotController?.stopCollectData()
    }

    private fun stopCollectData() {
        startCollectButton.isEnabled = true
        stopCollectButton.isEnabled = false
        continueCollectButton.isEnabled = false
        pauseCollectButton.isEnabled = false

        robotController?.stopCollectData()
    }

    private fun writePixelsToDataFile(pixels: IntArray,
                                      steeringAngle: Float,
                                      processedImageWidth: Int,
                                      processedImageHeight: Int,
                                      sourceImagePositionX: Int,
                                      sourceImagePositionY: Int,
                                      sourceImageWidth: Int,
                                      sourceImageHeight: Int){
        var csvLine = "$processedImageWidth;$processedImageHeight;$sourceImagePositionX;$sourceImagePositionY;$sourceImageWidth;$sourceImageHeight,$steeringAngle"
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

    private fun addLogText(logText: String) {
        statusTextView.append("\n" + logText)
    }

    private fun addTextToDataFile(text: String){
        getDataFile().appendText(text)
    }

    private fun doesDataFileExist(): Boolean {
        return getDataFile().exists()
    }

    private fun deleteDataFile(){
        getDataFile().delete()
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
