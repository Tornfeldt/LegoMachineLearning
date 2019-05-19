package es.jepp.legomachinelearning.imagelogic

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import com.otaliastudios.cameraview.PictureResult
import java.nio.IntBuffer

class CameraService {
    private val imageDataReadyHandler: ImageDataReadyHandler

    private val cameraViewWidth: Int
    private val cameraViewHeight: Int

    private val resultingBitmapMaxWidth: Int = 200
    private val resultingBitmapMaxHeight: Int = 200

    private val minimumXSurroundingWantedPixels: Int
    private val maximumXSurroundingWantedPixels: Int
    private val minimumYSurroundingWantedPixels: Int
    private val maximumYSurroundingWantedPixels: Int

    constructor(cameraViewWidth: Int, cameraViewHeight: Int, points: Array<Point>, imageDataReadyHandler: ImageDataReadyHandler) {
        this.cameraViewHeight = cameraViewHeight
        this.cameraViewWidth = cameraViewWidth
        this.imageDataReadyHandler = imageDataReadyHandler

        var minX = cameraViewWidth
        var maxX = 0
        var minY = cameraViewHeight
        var maxY = 0
        for (point in points) {
            if (point.x in 0..minX) {
                minX = point.x
            }
            if (point.x in maxX..cameraViewWidth) {
                maxX = point.x
            }
            if (point.y in 0..minY) {
                minY = point.y
            }
            if (point.y in maxY..cameraViewHeight) {
                maxY = point.y
            }
        }

        minimumXSurroundingWantedPixels = minX
        maximumXSurroundingWantedPixels = maxX
        minimumYSurroundingWantedPixels = minY
        maximumYSurroundingWantedPixels = maxY
    }

    fun onPictureTaken(result: PictureResult) {
        // Convert to bitmap to easily get the pixels we want
        result.toBitmap(resultingBitmapMaxWidth, resultingBitmapMaxHeight) { resultingBitmap ->
            val scale = cameraViewWidth.toFloat() / resultingBitmap!!.width
            val minXImage = (minimumXSurroundingWantedPixels / scale).toInt()
            val maxXImage = (maximumXSurroundingWantedPixels / scale).toInt()
            val minYImage = (minimumYSurroundingWantedPixels / scale).toInt()
            val maxYImage = (maximumYSurroundingWantedPixels / scale).toInt()
            var height = maxYImage - minYImage
            val width = maxXImage - minXImage

            height = 1; // Always take just one row of pixels

            val wantedPixels = IntArray(width * height)
            resultingBitmap?.getPixels(wantedPixels, 0, width, minXImage, minYImage, width, height)

            convertRgbPixelsToGrayscaleRgbValues(wantedPixels)
            var newImagePixels = wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels + wantedPixels;
            val newBitmap = Bitmap.createBitmap(width, height * 10, resultingBitmap.config)
            newBitmap.copyPixelsFromBuffer(IntBuffer.wrap(newImagePixels))

            convertRgbPixelsToGrayscaleValues(wantedPixels)

            imageDataReadyHandler.imageReady(
                width,
                height,
                minimumXSurroundingWantedPixels,
                minimumYSurroundingWantedPixels,
                maximumXSurroundingWantedPixels - minimumXSurroundingWantedPixels,
                maximumYSurroundingWantedPixels - minimumYSurroundingWantedPixels,
                newBitmap,
                wantedPixels)
        }
    }

    fun convertRgbPixelsToGrayscaleValues(pixels: IntArray) {
        for (i in pixels.indices) {
            var from = pixels[i]
            var red = Color.red(from)
            var green = Color.green(from)
            var blue = Color.blue(from)

            var gray = (red + green + blue) / 3

            pixels[i] = gray
        }
    }

    fun convertRgbPixelsToGrayscaleRgbValues(pixels: IntArray) {
        for (i in pixels.indices) {
            var from = pixels[i]
            var red = Color.red(from)
            var green = Color.green(from)
            var blue = Color.blue(from)

            var gray = (red + green + blue) / 3

            var grayColor = Color.rgb(gray, gray, gray)
            pixels[i] = grayColor
        }
    }
}