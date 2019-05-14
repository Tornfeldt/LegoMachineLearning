package es.jepp.legomachinelearning.data

class TrainedModel {
    val processedImageWidth: Int
    val processedImageHeight: Int
    val sourceImagePositionX: Int
    val sourceImagePositionY: Int
    val sourceImageWidth: Int
    val sourceImageHeight: Int
    val theta: FloatArray

    constructor(theta: FloatArray,
                processedImageWidth: Int,
                processedImageHeight: Int,
                sourceImagePositionX: Int,
                sourceImagePositionY: Int,
                sourceImageWidth: Int,
                sourceImageHeight: Int) {
        this.theta = theta
        this.processedImageWidth = processedImageWidth
        this.processedImageHeight = processedImageHeight
        this.sourceImagePositionX = sourceImagePositionX
        this.sourceImagePositionY = sourceImagePositionY
        this.sourceImageWidth = sourceImageWidth
        this.sourceImageHeight = sourceImageHeight
    }
}