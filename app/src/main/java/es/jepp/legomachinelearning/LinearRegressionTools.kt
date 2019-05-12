package es.jepp.legomachinelearning

import java.lang.IllegalArgumentException
import kotlin.math.pow


object LinearRegressionTools {
    fun computeCost(X: Array<FloatArray>, y: FloatArray, theta: FloatArray): Float {
        if (X.size != y.size) {
            throw IllegalArgumentException("X and y must have the same length.");
        }

        var result = 0f
        val m = y.size
        for (i in 0 until m) {
            val h = computeHypothesis(X[i], theta)
            result += (h - y[i]).pow(2f) / (2 * m)
        }

        return result
    }

    fun computeHypothesis(x: FloatArray, theta: FloatArray): Float {
        if (x.size != theta.size) {
            throw IllegalArgumentException("x and theta must have the same length.")
        }

        var result = 0f
        val n = theta.size
        for (i in 0 until n) {
            result += theta[i] * x[i]
        }

        return result;
    }
}