package es.jepp.legomachinelearning

object FakeRobotController: BasicRobotController {
    override fun resetSteeringAngleToCenterPosition() {

    }

    override fun tryConnect(): Boolean {
        return true
    }

    override fun disconnect() {

    }

    override fun drivingPower(power: Int) {

    }

    override fun drivingForward() {

    }

    override fun drivingStop() {

    }

    override fun steeringPower(power: Int) {

    }

    override fun steeringLeft() {

    }

    override fun steeringRight() {

    }

    override fun steeringStop() {

    }

    override fun steeringResetTachoCount() {

    }

    override fun steeringRotateToTachoCount(resultingTachoCount: Int) {
        Thread.sleep(500)
    }

    override fun getCurrentSteeringTachoCount(): Int {
        return 3
    }

    override fun isSteeringAtTheMostLeftPosition(): Boolean {
        return true
    }

    override fun isSteeringAtTheMostRightPosition(): Boolean {
        return true
    }

    override fun getCurrentSteeringSensorAngleInPercent(): Float {
        return 3f
    }
}