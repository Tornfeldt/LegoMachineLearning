package es.jepp.legomachinelearning

import android.util.Log
import lejos.nxt.Motor
import lejos.nxt.SensorPort
import lejos.nxt.TouchSensor
import lejos.nxt.remote.NXTCommand
import lejos.nxt.remote.RemoteMotor
import lejos.pc.comm.NXTComm
import lejos.pc.comm.NXTCommLogListener
import lejos.pc.comm.NXTCommandConnector
import lejos.pc.comm.NXTConnector
import java.util.logging.Logger

object BasicRobotController {
    private enum class NXT_CONN_TYPE {
        LEJOS_PACKET, LEGO_LCP
    }

    private var nxtConnection : NXTConnector? = null
    private val logger = Logger.getLogger(this.javaClass.name)

    private var drivingMotor: RemoteMotor? = null
    private var steeringMotor: RemoteMotor? = null
    private var leftTouchSensor: TouchSensor? = null
    private var rightTouchSensor: TouchSensor? = null
    private var steeringSensor: SensorPort? = null

    var isConnected: Boolean = false

    private fun setupMotorsAndSensors(){
        drivingMotor = Motor.C
        steeringMotor = Motor.A
        leftTouchSensor = TouchSensor(SensorPort.S1)
        rightTouchSensor = TouchSensor(SensorPort.S2)
        steeringSensor = SensorPort.S3
        steeringSensor?.setTypeAndMode(SensorPort.TYPE_SWITCH, SensorPort.MODE_BOOLEAN)
    }

    /**
     * Tries to connect to the NXT LEGO robot.
     * @return whether the NXT is connected.
     */
    fun tryConnect(): Boolean {
        logger.info("Connecting")
        try {
            val conn = NXTConnector()
            conn.setDebug(true)
            conn.addLogListener(object : NXTCommLogListener {
                override fun logEvent(arg0: String) {
                    Log.d(" NXT log:", arg0)
                }

                override fun logEvent(arg0: Throwable) {
                    Log.e(" NXT log:", arg0.message, arg0)
                }
            })

            when (NXT_CONN_TYPE.LEJOS_PACKET) {
                NXT_CONN_TYPE.LEGO_LCP -> conn.connectTo("btspp://NXT", NXTComm.LCP)
                NXT_CONN_TYPE.LEJOS_PACKET -> conn.connectTo("btspp://")
            }

            NXTCommandConnector.setNXTCommand(NXTCommand(conn.getNXTComm()))

            setupMotorsAndSensors()

            nxtConnection = conn
            isConnected = true
        } catch (e: Exception) {
            nxtConnection = null
            isConnected = false
        }

        return isConnected;
    }

    /**
     * Disconnects the NXT LEGO robot if it was connected.
     */
    fun disconnect() {
        logger.info("Disconnecting")
        try {
            nxtConnection?.getNXTComm()?.close()
        } catch (e: Exception) {
        } finally {
            nxtConnection = null
            isConnected = false
        }
    }

    /**
     * Sets the driving power/speed of the robot.
     * [power] must be between 0 and 100.
     */
    fun drivingPower(power: Int) {
        drivingMotor?.power = power
    }

    /**
     * Makes the robot drive forward.
     */
    fun drivingForward() {
        drivingMotor?.backward()
    }

    /**
     * Makes the robot stop.
     */
    fun drivingStop() {
        drivingMotor?.stop()
    }

    /**
     * Sets the steering power/speed of the robot.
     * [power] must be between 0 and 100.
     */
    fun steeringPower(power: Int) {
        steeringMotor?.power = power
    }

    /**
     * Starts the steering motor in the left direction.
     */
    fun steeringLeft() {
        steeringMotor?.forward()
    }

    /**
     * Starts the steering motor in the right direction.
     */
    fun steeringRight() {
        steeringMotor?.backward()
    }

    /**
     * Stops the steering motor.
     */
    fun steeringStop() {
        steeringMotor?.stop()
    }

    /**
     * Resets the tacho count for the steering motor.
     */
    fun steeringResetTachoCount() {
        steeringMotor?.resetTachoCount()
    }

    /**
     * Rotates the steering motor to the specified [resultingTachoCount].
     */
    fun steeringRotateToTachoCount(resultingTachoCount: Int) {
        steeringMotor?.rotateTo(resultingTachoCount, true)
    }

    /**
     * Returns the current tacho count for the steering motor.
     */
    fun getCurrentSteeringTachoCount(): Int {
        return steeringMotor?.tachoCount!!
    }

    /**
     * Returns whether the steering motor is in the most left position.
     */
    fun isSteeringAtTheMostLeftPosition(): Boolean {
        return leftTouchSensor?.isPressed!!
    }

    /**
     * Returns whether the steering motor is in the most right position.
     */
    fun isSteeringAtTheMostRightPosition(): Boolean {
        return rightTouchSensor?.isPressed!!
    }

    /**
     * Gets the current steering angle in percent.
     */
    fun getCurrentSteeringSensorAngleInPercent(): Float {
        var rawValue = steeringSensor?.readRawValue()!!
        var percentage = convertRawSteeringValueToPercent(rawValue)
        return percentage
    }

    private fun convertRawSteeringValueToPercent(raw: Int): Float {
        val result = (100.0 * 0.6387659 * Math.log(-515.7895 / (raw - 765.7895))).toFloat() - 2
        if (result < 0)
            return 0f
        return if (result > 100) 100f else result
    }
}