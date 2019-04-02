package es.jepp.legomachinelearning.drawview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

class ColorBall(internal var mContext: Context, resourceId: Int, internal var point: Point, id: Int) {

    var bitmap: Bitmap
        internal set
    var id: Int = 0
        internal set

    val widthOfBall: Int
        get() = bitmap.width

    val heightOfBall: Int
        get() = bitmap.height

    var x: Int
        get() = point.x
        set(x) {
            point.x = x
        }

    var y: Int
        get() = point.y
        set(y) {
            point.y = y
        }

    init {
        this.id = id
        bitmap = BitmapFactory.decodeResource(
            mContext.resources,
            resourceId
        )
    }

    fun addY(y: Int) {
        point.y = point.y + y
    }

    fun addX(x: Int) {
        point.x = point.x + x
    }

    companion object {
        internal var count = 0
    }
}
