package es.jepp.legomachinelearning

interface BasicRobotController {
    fun tryConnect(): Boolean
    fun disconnect()
    fun drivingPower(power: Int)
    fun drivingForward()
    fun drivingStop()
    fun steeringPower(power: Int)
    fun steeringLeft()
    fun steeringRight()
    fun steeringStop()
    fun steeringResetTachoCount()
    fun steeringRotateToTachoCount(resultingTachoCount: Int)
    fun getCurrentSteeringTachoCount(): Int
    fun isSteeringAtTheMostLeftPosition(): Boolean
    fun isSteeringAtTheMostRightPosition(): Boolean
    fun getCurrentSteeringSensorAngleInPercent(): Float
}