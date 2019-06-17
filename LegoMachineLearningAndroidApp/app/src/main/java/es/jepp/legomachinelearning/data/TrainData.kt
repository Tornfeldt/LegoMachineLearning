package es.jepp.legomachinelearning.data

class TrainData {
    val X: Array<FloatArray>
    val y: FloatArray

    constructor(X: Array<FloatArray>, y: FloatArray){
        this.X = X
        this.y = y
    }
}