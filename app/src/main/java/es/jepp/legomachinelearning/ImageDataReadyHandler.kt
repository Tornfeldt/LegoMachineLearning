package es.jepp.legomachinelearning

import android.graphics.Bitmap

interface ImageDataReadyHandler {
    fun imageReady(width: Int, height: Int, positionX: Int, positionY: Int, image: Bitmap, grayscalePixels: IntArray)
}