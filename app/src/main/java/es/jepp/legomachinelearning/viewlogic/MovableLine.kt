package es.jepp.legomachinelearning.viewlogic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class MovableLine : View {
    private var isHorizontal = true
    private var distanceInPercentFromTopOrLeft = 50f
    private var distanceInPixelsFromTopOrLeft = -1
    private var lineWidth = 10f
    private var halfLineWidth = lineWidth / 2f
    private var canMove = true
    private var touchRadius = 30f

    private var paint = Paint()
    private var isMoving = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        isFocusable = true
        paint.color = Color.RED
        paint.strokeWidth = 3f
        paint.alpha = 100
    }

    override fun onDraw(canvas: Canvas) {
        if (distanceInPixelsFromTopOrLeft != -1) {
            if (isHorizontal){
                if (distanceInPixelsFromTopOrLeft <= this.height) {
                    distanceInPercentFromTopOrLeft = 100f * distanceInPixelsFromTopOrLeft / this.height
                }
            } else {
                if (distanceInPixelsFromTopOrLeft <= this.width) {
                    distanceInPercentFromTopOrLeft = 100f * distanceInPixelsFromTopOrLeft / this.width
                }
            }

            distanceInPixelsFromTopOrLeft = -1
        }

        var left = 0f
        var right = 0f
        var top = 0f
        var bottom = 0f

        val width = this.width.toFloat()
        val height = this.height.toFloat()

        if (isHorizontal) {
            left = 0f
            right = width
            top = height * distanceInPercentFromTopOrLeft / 100f - halfLineWidth
            bottom = height * distanceInPercentFromTopOrLeft / 100f + halfLineWidth
        } else {
            left = width * distanceInPercentFromTopOrLeft / 100f - halfLineWidth
            right = width * distanceInPercentFromTopOrLeft / 100f + halfLineWidth
            top = 0f
            bottom = height
        }

        canvas.drawRect(left, top, right, bottom, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMoving = false;

                if (canMove) {
                    if (isHorizontal &&
                        event.y > distanceInPercentFromTopOrLeft / 100f * this.height - halfLineWidth - touchRadius &&
                        event.y < distanceInPercentFromTopOrLeft / 100f * this.height + halfLineWidth + touchRadius) {
                        isMoving = true
                    } else if (!isHorizontal &&
                        event.x > distanceInPercentFromTopOrLeft / 100f * this.width - halfLineWidth - touchRadius &&
                        event.x < distanceInPercentFromTopOrLeft / 100f * this.width + halfLineWidth + touchRadius) {
                        isMoving = true
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isMoving){
                    distanceInPercentFromTopOrLeft = if (isHorizontal) {
                        100f * event.y / this.height
                    } else {
                        100f * event.x / this.width
                    }

                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isMoving = false
            }
        }

        return true
    }

    fun setIsHorizontal(isHorizontal: Boolean){
        this.isHorizontal = isHorizontal
        invalidate()
    }

    fun setDistanceInPercentFromTopOrLeft(distanceInPercentFromTopOrLeft: Float) {
        this.distanceInPercentFromTopOrLeft = distanceInPercentFromTopOrLeft
        this.distanceInPixelsFromTopOrLeft = -1
        invalidate()
    }

    fun setDistanceInPixelsFromTopOrLeft(distanceInPixelsFromTopOrLeft: Int) {
        this.distanceInPixelsFromTopOrLeft = distanceInPixelsFromTopOrLeft
        invalidate()
    }

    fun setCanMove(canMove: Boolean) {
        this.canMove = canMove
    }

    fun getDistanceInPixelsFromTopOrLeft(): Int {
        if (isHorizontal) {
            return (this.height * distanceInPercentFromTopOrLeft / 100f).toInt()
        } else {
            return (this.width * distanceInPercentFromTopOrLeft / 100f).toInt()
        }
    }
}