package es.jepp.legomachinelearning.drawview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import es.jepp.legomachinelearning.R

class RectagleDrawView : View {

    internal var point1: Point = Point()
    internal var point3: Point = Point()
    internal var point2: Point = Point()
    internal var point4: Point = Point()

    internal var startMovePoint: Point? = null

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    internal var groupId = 2
    private var colorballs: ArrayList<ColorBall>? = null
    // array that holds the balls
    private var balID = 0
    // variable to know what ball is being dragged
    internal var paint: Paint = Paint()
    internal var canvas: Canvas = Canvas()

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
        paint = Paint()
        setFocusable(true) // necessary for getting the touch events
        canvas = Canvas()
        // setting the start point for the balls
        point1 = Point()
        point1.x = 50
        point1.y = 20

        point2 = Point()
        point2.x = 150
        point2.y = 20

        point3 = Point()
        point3.x = 150
        point3.y = 120

        point4 = Point()
        point4.x = 50
        point4.y = 120

        // declare each ball with the ColorBall class
        colorballs = ArrayList()
        colorballs!!.add(0, ColorBall(context, R.drawable.gray_circle, point1, 0))
        colorballs!!.add(1, ColorBall(context, R.drawable.gray_circle, point2, 1))
        colorballs!!.add(2, ColorBall(context, R.drawable.gray_circle, point3, 2))
        colorballs!!.add(3, ColorBall(context, R.drawable.gray_circle, point4, 3))
    }

    internal fun listPoints(): Array<Point> {
        return arrayOf(point1, point2, point3, point4)
    }

    // the method that draws the balls
    protected override fun onDraw(canvas: Canvas) {
        // canvas.drawColor(0xFFCCCCCC); //if you want another background color

        paint.setAntiAlias(true)
        paint.setDither(true)
        paint.setColor(Color.parseColor("#55000000"))
        paint.setStyle(Paint.Style.FILL)
        paint.setStrokeJoin(Paint.Join.ROUND)
        // mPaint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5F)

        canvas.drawPaint(paint)
        paint.setColor(Color.parseColor("#55FFFFFF"))

        if (groupId == 1) {
            canvas.drawRect(
                point1.x + colorballs!![0].widthOfBall / 2F,
                point3.y + colorballs!![2].widthOfBall / 2F,
                point3.x + colorballs!![2].widthOfBall / 2F, point1.y + colorballs!![0].widthOfBall / 2F, paint
            )
        } else {
            canvas.drawRect(
                point2.x + colorballs!![1].widthOfBall / 2F,
                point4.y + colorballs!![3].widthOfBall / 2F,
                point4.x + colorballs!![3].widthOfBall / 2F, point2.y + colorballs!![1].widthOfBall / 2F, paint
            )
        }
        val mBitmap: BitmapDrawable
        mBitmap = BitmapDrawable()

        // draw the balls on the canvas
        for (ball in colorballs!!) {
            canvas.drawBitmap(
                ball.bitmap, ball.x.toFloat(), ball.y.toFloat(),
                Paint()
            )
        }
    }

    // events when touching the screen
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventaction = event.action

        val X = event.x.toInt()
        val Y = event.y.toInt()

        when (eventaction) {

            MotionEvent.ACTION_DOWN // touch down so check if the finger is on
            -> {
                // a ball
                balID = -1
                startMovePoint = Point(X, Y)
                for (ball in colorballs!!) {
                    // check if inside the bounds of the ball (circle)
                    // get the center for the ball
                    val centerX = ball.x + ball.widthOfBall
                    val centerY = ball.y + ball.heightOfBall
                    paint.setColor(Color.CYAN)
                    // calculate the radius from the touch to the center of the ball
                    val radCircle = Math
                        .sqrt(((centerX - X) * (centerX - X) + (centerY - Y) * (centerY - Y)).toDouble())

                    if (radCircle < ball.widthOfBall) {

                        balID = ball.id
                        if (balID == 1 || balID == 3) {
                            groupId = 2
                            canvas.drawRect(
                                point1.x.toFloat(), point3.y.toFloat(), point3.x.toFloat(), point1.y.toFloat(),
                                paint
                            )
                        } else {
                            groupId = 1
                            canvas.drawRect(
                                point2.x.toFloat(), point4.y.toFloat(), point4.x.toFloat(), point2.y.toFloat(),
                                paint
                            )
                        }
                        invalidate()
                        break
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_MOVE // touch drag with the ball
            ->
                // move the balls the same as the finger
                if (balID > -1) {
                    colorballs!![balID].x = X
                    colorballs!![balID].y = Y

                    paint.setColor(Color.CYAN)

                    if (groupId == 1) {
                        colorballs!![1].x = colorballs!![0].x
                        colorballs!![1].y = colorballs!![2].y
                        colorballs!![3].x = colorballs!![2].x
                        colorballs!![3].y = colorballs!![0].y
                        canvas.drawRect(
                            point1.x.toFloat(), point3.y.toFloat(), point3.x.toFloat(), point1.y.toFloat(),
                            paint
                        )
                    } else {
                        colorballs!![0].x = colorballs!![1].x
                        colorballs!![0].y = colorballs!![3].y
                        colorballs!![2].x = colorballs!![3].x
                        colorballs!![2].y = colorballs!![1].y
                        canvas.drawRect(
                            point2.x.toFloat(), point4.y.toFloat(), point4.x.toFloat(), point2.y.toFloat(),
                            paint
                        )
                    }

                    invalidate()
                } else {
                    if (startMovePoint != null) {
                        paint.setColor(Color.CYAN)
                        val diffX = X - startMovePoint!!.x
                        val diffY = Y - startMovePoint!!.y
                        startMovePoint!!.x = X
                        startMovePoint!!.y = Y
                        colorballs!![0].addX(diffX)
                        colorballs!![1].addX(diffX)
                        colorballs!![2].addX(diffX)
                        colorballs!![3].addX(diffX)
                        colorballs!![0].addY(diffY)
                        colorballs!![1].addY(diffY)
                        colorballs!![2].addY(diffY)
                        colorballs!![3].addY(diffY)
                        if (groupId == 1)
                            canvas.drawRect(
                                point1.x.toFloat(), point3.y.toFloat(), point3.x.toFloat(), point1.y.toFloat(),
                                paint
                            )
                        else
                            canvas.drawRect(
                                point2.x.toFloat(), point4.y.toFloat(), point4.x.toFloat(), point2.y.toFloat(),
                                paint
                            )
                        invalidate()
                    }
                }

            MotionEvent.ACTION_UP -> {
            }
        }// touch drop - just do things here after dropping
        // redraw the canvas
        invalidate()
        return true

    }

    fun shade_region_between_points() {
        canvas.drawRect(point1.x.toFloat(), point3.y.toFloat(), point3.x.toFloat(), point1.y.toFloat(), paint)
    }
}
