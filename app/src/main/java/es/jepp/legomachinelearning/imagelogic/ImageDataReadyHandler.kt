package es.jepp.legomachinelearning.imagelogic

import android.graphics.Bitmap

interface ImageDataReadyHandler {
    fun imageReady(
        processedImageWidth: Int,
        processedImageHeight: Int,
        sourceImagePositionX: Int,
        sourceImagePositionY: Int,
        sourceImageWidth: Int,
        sourceImageHeight: Int,
        image: Bitmap, grayscalePixels: IntArray)
}