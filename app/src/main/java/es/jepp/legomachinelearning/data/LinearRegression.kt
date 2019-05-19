package es.jepp.legomachinelearning.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class LinearRegression {
    private val X: Array<FloatArray>
    private val y: FloatArray
    private val learningRate: Float
    private val numberOfIterations: Int
    private var theta : FloatArray? = null
    private var gradientDescentIsRunning = true

    private var iterationHandler: LinearRegressionIterationHandler? = null

    constructor(X: Array<FloatArray>, y: FloatArray, learningRate: Float, numberOfIterations: Int){
        this.X = X
        this.y = y
        this.learningRate = learningRate
        this.numberOfIterations = numberOfIterations
    }

    fun setTheta(theta: FloatArray) {
        if (X[0].size != theta.size)
            throw IllegalArgumentException("Theta must have the same length as X's element.")

        this.theta = theta
    }

    fun generateTheta() {
        theta = FloatArray(X[0].size)
    }

    fun doGradientDescent() {
        if (theta == null)
            throw Exception("Theta must be set before calling doGradientDescent")

        gradientDescentIsRunning = true

        GlobalScope.launch {
            for (iteration in 1..numberOfIterations) {
                if (!gradientDescentIsRunning) {
                    break
                }

                gradientDescentSingleIteration()

                val cost = LinearRegressionTools.computeCost(X, y, theta!!)
                iterationHandler?.afterEachIteration(numberOfIterations, iteration, cost)
            }

            iterationHandler?.afterAllIterations(theta!!)
        }.start()
    }

    fun stopGradientDescent() {
        gradientDescentIsRunning = false
    }

    private fun gradientDescentSingleIteration() {
        if (X.size != y.size)
            throw IllegalArgumentException("X and y must have the same length.");

        var m = y.size;
        var n = theta!!.size;

        for (j in 0 until n) {
            var sumResult = 0f

            for (i in 0 until m) {
                val h = LinearRegressionTools.computeHypothesis(X[i], theta!!)
                sumResult += (learningRate / m) * (h - y[i]) * X[i][j]
            }

            theta!![j] = theta!![j] - sumResult.toFloat()
        }
    }

    fun setIterationHandler(iterationHandler: LinearRegressionIterationHandler){
        this.iterationHandler = iterationHandler
    }

    interface LinearRegressionIterationHandler {
        fun afterEachIteration(totalNumberOfIterations: Int, currentIteration: Int, currentTrainCost: Float)
        fun afterAllIterations(theta: FloatArray)
    }
}