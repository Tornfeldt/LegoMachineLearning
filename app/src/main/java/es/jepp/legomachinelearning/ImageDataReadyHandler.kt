package es.jepp.legomachinelearning

import android.graphics.Bitmap

interface ImageDataReadyHandler {
    fun imageReady(image: Bitmap, grayscalePixels: IntArray)
}