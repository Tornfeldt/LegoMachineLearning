package es.jepp.legomachinelearning.data

class TrainedModel {
    var positionX: Int
    var positionY: Int
    var width: Int
    var height: Int
    var theta: FloatArray

    constructor(positionX: Int, positionY: Int, width: Int, height: Int, theta: FloatArray) {
        this.positionX = positionX
        this.positionY = positionY
        this.width = width
        this.height = height
        this.theta = theta
    }
}