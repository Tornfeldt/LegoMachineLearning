package es.jepp.legomachinelearning.data

import java.io.File

object CsvToDataConverter {
    fun convertDataFromFile(datafile: File): TrainData {
        val y = mutableListOf<Float>()
        val X = mutableListOf<FloatArray>()

        var numberOfFeatures: Int? = null

        datafile.forEachLine {
            val splittedLine = it.split(",")

            var steeringAngle = splittedLine[4].toFloat()
            steeringAngle = (steeringAngle - 50f) / 50f // make sure the steering angle is between -1 and 1
            y.add(steeringAngle)

            val thisNumberOfFeatures = splittedLine.size - 5
            if (numberOfFeatures == null) {
                numberOfFeatures = thisNumberOfFeatures
            }
            else if (numberOfFeatures != thisNumberOfFeatures) {
                throw Exception("All data must have the same number of features.")
            }

            val x = FloatArray(thisNumberOfFeatures + 1)
            x[0] = 1f

            for(i in 1..thisNumberOfFeatures) {
                val feature = splittedLine[i + 4].toFloat()
                x[i] = feature / 255f // make sure the pixel value is normalized to be between 0 and 1
            }

            X.add(x)
        }

        val result = TrainData(X.toTypedArray(), y.toFloatArray())
        return result
    }

    fun generateTrainedModel(datafile: File, theta: FloatArray): TrainedModel {
        var positionX = 0
        var positionY = 0
        var width = 0
        var height = 0

        datafile.useLines {
            val firstLine = it.first()
            val splittedLine = firstLine.split(",")
            positionX = splittedLine[0].toInt()
            positionY = splittedLine[1].toInt()
            width = splittedLine[2].toInt()
            height = splittedLine[3].toInt()
        }

        var result = TrainedModel(positionX, positionY, width, height, theta)
        return result
    }
}