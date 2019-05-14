package es.jepp.legomachinelearning.viewlogic

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.gson.Gson
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.DataPointInterface
import es.jepp.legomachinelearning.LinearRegression
import es.jepp.legomachinelearning.R
import es.jepp.legomachinelearning.StaticSettings
import es.jepp.legomachinelearning.data.CsvToDataConverter
import es.jepp.legomachinelearning.data.TrainedModel
import kotlinx.android.synthetic.main.activity_train.*
import java.io.File
import com.jjoe64.graphview.series.LineGraphSeries



class TrainActivity : Activity(), LinearRegression.LinearRegressionIterationHandler {

    private var linearRegression: LinearRegression? = null
    private var graphSeries: LineGraphSeries<DataPoint>? = null

    private var uiHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_train)

        uiHandler = Handler()

        if (doesTrainedModelExist()) {
            deleteOldTrainedModelContainer.visibility = View.VISIBLE
            trainContainer.visibility = View.GONE
        }
        else {
            deleteOldTrainedModelContainer.visibility = View.GONE
            trainContainer.visibility = View.VISIBLE
        }

        deleteOldTrainedModelButton.setOnClickListener {
            deleteTrainedModel()
            deleteOldTrainedModelContainer.visibility = View.GONE
            trainContainer.visibility = View.VISIBLE
        }

        stopTrainButton.setOnClickListener {
            stopTrainButton.isEnabled = false
            linearRegression?.stopGradientDescent()
        }

        startTrainButton.setOnClickListener {
            startTrainButton.isEnabled = false
            stopTrainButton.isEnabled = true

            val numberOfIterations = numberOfIterationsEditText.text.toString().toInt()
            val learningRate = learningRateEditText.text.toString().toFloat()
            val trainData = CsvToDataConverter.convertDataFromFile(getTrainDataFile())

            setupGraph(numberOfIterations)

            linearRegression = LinearRegression(trainData.X, trainData.y, learningRate, numberOfIterations)

            linearRegression!!.generateTheta()
            linearRegression!!.setIterationHandler(this)

            linearRegression!!.doGradientDescent()
        }
    }

    private fun setupGraph(totalNumberOfIterations: Int) {
        // Reset the graph before setup
        graph.removeAllSeries()

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.toDouble())
        graph.viewport.setMaxX(totalNumberOfIterations.toDouble())

        graph.viewport.isYAxisBoundsManual = false

        // first mSeries is a line
        graphSeries = LineGraphSeries()
        graph.addSeries(graphSeries)
    }

    private fun doesTrainedModelExist(): Boolean {
        return getTrainedModelFile().exists()
    }

    private fun getTrainedModelFile(): File {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val modelName = getModelName()
        val fileName = modelName + StaticSettings.TRAINED_MODEL_FILE_ENDING
        return File(dataDirectory, fileName)
    }

    private fun getModelName() : String {
        return intent.getStringExtra("ModelName")
    }

    private fun deleteTrainedModel() {
        if (doesTrainedModelExist()) {
            getTrainedModelFile().delete()
        }
    }

    private fun getTrainDataFile(): File {
        val dataDirectory = getExternalFilesDir(StaticSettings.BASE_FOLDER_NAME)
        val modelName = getModelName()
        val fileName = modelName + StaticSettings.DATA_FILE_ENDING
        return File(dataDirectory, fileName)
    }

    private fun writeTrainedModelFile(trainedModel: TrainedModel){
        // Make sure the file is deleted before
        deleteTrainedModel()

        var file = getTrainedModelFile()
        val json = Gson().toJson(trainedModel)
        file.writeText(json)
    }

    override fun afterEachIteration(totalNumberOfIterations: Int, currentIteration: Int, currentTrainCost: Float) {
        val percentageDone = (100 * currentIteration) / totalNumberOfIterations

        uiHandler!!.post {
            percentageDoneTextView.text = percentageDone.toString()

            graphSeries!!.appendData(
                DataPoint(currentIteration.toDouble(), currentTrainCost.toDouble()),
                false,
                totalNumberOfIterations
            )
        }
    }

    override fun afterAllIterations(theta: FloatArray) {
        var trainedModel = CsvToDataConverter.generateTrainedModel(getTrainDataFile(), theta)
        writeTrainedModelFile(trainedModel)

        uiHandler!!.post{
            startTrainButton.isEnabled = true
            stopTrainButton.isEnabled = false
        }
    }
}
