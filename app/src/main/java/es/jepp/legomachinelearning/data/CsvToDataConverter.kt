package es.jepp.legomachinelearning.data

import java.io.File

object CsvToDataConverter {
    fun convertDataFromFile(datafile: File): TrainData {
        val y = mutableListOf<Float>()
        val X = mutableListOf<FloatArray>()

        var numberOfFeatures: Int? = null

        datafile.forEachLine {
            val splittedLine = it.split(",")

            var steeringAngle = splittedLine[1].toFloat()
            steeringAngle = (steeringAngle - 50f) / 50f // make sure the steering angle is between -1 and 1
            y.add(steeringAngle)

            val thisNumberOfFeatures = splittedLine.size - 2
            if (numberOfFeatures == null) {
                numberOfFeatures = thisNumberOfFeatures
            }
            else if (numberOfFeatures != thisNumberOfFeatures) {
                throw Exception("All data must have the same number of features.")
            }

            val x = FloatArray(thisNumberOfFeatures + 1)
            x[0] = 1f

            for(i in 1..thisNumberOfFeatures) {
                val feature = splittedLine[i + 1].toFloat()
                x[i] = feature / 255f // make sure the pixel value is normalized to be between 0 and 1
            }

            X.add(x)
        }

        val result = TrainData(X.toTypedArray(), y.toFloatArray())
        return result
    }

    fun generateTrainedModel(datafile: File, theta: FloatArray): TrainedModel {
        var processedImageWidth = 0
        var processedImageHeight = 0
        var sourceImagePositionX = 0
        var sourceImagePositionY = 0
        var sourceImageWidth = 0
        var sourceImageHeight = 0

        datafile.useLines {
            val firstLine = it.first()
            val splittedLine = firstLine.split(",")
            val splittedDataElement = splittedLine[0].split(";")
            processedImageWidth = splittedDataElement[0].toInt()
            processedImageHeight = splittedDataElement[1].toInt()
            sourceImagePositionX = splittedDataElement[2].toInt()
            sourceImagePositionY = splittedDataElement[3].toInt()
            sourceImageWidth = splittedDataElement[4].toInt()
            sourceImageHeight = splittedDataElement[5].toInt()
        }

        var result = TrainedModel(
            theta,
            processedImageWidth,
            processedImageHeight,
            sourceImagePositionX,
            sourceImagePositionY,
            sourceImageWidth,
            sourceImageHeight)

        return result
    }

    fun generateFeaturesFromGrayscalePixels(grayscalePixels: IntArray): FloatArray {
        val numberOfPixels = grayscalePixels.size

        val result = FloatArray(numberOfPixels + 1)
        result[0] = 1f

        for (i in 1..numberOfPixels){
            result[i] = grayscalePixels[i - 1] / 255f
        }

        return result
    }
}