package es.jepp.legomachinelearning

import kotlinx.coroutines.*

class RobotController {
    private val controller: BasicRobotController
    private val robotHasSteeredHandler: RobotHasSteeredHandler
    private var mostRightTachoCount = 0
    private var isCollectingData = false

    constructor(controller: BasicRobotController, robotHasSteeredHandler: RobotHasSteeredHandler){
        this.controller = controller
        this.robotHasSteeredHandler = robotHasSteeredHandler
    }

    /**
     * Tries to connect to the robot.
     * @return whether the is connected.
     */
    fun tryConnect(): Boolean {
        return controller.tryConnect()
    }

    /**
     * Disconnects the robot if it was connected.
     */
    fun disconnect() {
        isCollectingData = false
        controller.disconnect()
    }

    /**
     * Initializes the steering by turning steering all the way left and right
     */
    fun initializeSteering() {
        controller.steeringPower(20)

        // First steer all the way right to make sure the steering initialization is the same every time
        if (!controller.isSteeringAtTheMostRightPosition()) {
            controller.steeringRight()
            while (!controller.isSteeringAtTheMostRightPosition()) { }
            controller.steeringStop()
        }

        // Turn the steering all the way left and reset the tacho count
        controller.steeringLeft()
        while (!controller.isSteeringAtTheMostLeftPosition()) { }
        // We are now at the most left position, so stop the motor and reset tacho count
        controller.steeringStop()
        controller.steeringResetTachoCount()

        // Turn the steering back right and read the tacho count
        controller.steeringRight()
        while (!controller.isSteeringAtTheMostRightPosition()) { }
        controller.steeringStop()
        var currentTachoCount = controller.getCurrentSteeringTachoCount()

        // Set the tacho count so it can be used in calculations
        mostRightTachoCount = currentTachoCount

        // Turn the steering position to the middle (just to make the robot look nice)
        var newTachoCount = convertSteeringPercentageToTachoCount(50f)
        controller.steeringRotateToTachoCount(newTachoCount)
    }

    /**
     * Starts collecting data and enables manual steering
     */
    fun startCollectData() {
        isCollectingData = true

        controller.steeringPower(80)
        controller.drivingPower(20)

        GlobalScope.launch {
            // Steer the robot as long as we are collecting data. Run in its own thread to not block.
            while (isCollectingData) {
                val steeringPercentage = controller.getCurrentSteeringSensorAngleInPercent()

                if (isCollectingData){
                    GlobalScope.launch {
                        robotHasSteeredHandler.robotHasSteered(steeringPercentage)
                    }

                    val wantedTachoCount = convertSteeringPercentageToTachoCount(steeringPercentage)
                    controller.steeringRotateToTachoCount(wantedTachoCount)
                }
            }

            // Make sure motors are stopped after collecting data
            controller.drivingStop()
            controller.steeringStop()
        }.start()

        // The robot must run forward when collecting data
        controller.drivingForward()
    }

    fun stopCollectData() {
        isCollectingData = false
    }

    private fun convertSteeringPercentageToTachoCount(percentage: Float): Int {
        return (mostRightTachoCount * (100 - percentage) / 100).toInt()
    }
}