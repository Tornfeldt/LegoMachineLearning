package es.jepp.legomachinelearning

class RobotController {
    private val controller: BasicRobotController
    private var mostRightTachoCount = 0
    private var isTraining = false

    constructor(controller: BasicRobotController){
        this.controller = controller
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
        isTraining = false
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
     * Starts training and enables manual steering
     */
    fun startTraining() {
        isTraining = true

        Thread {
            // Steer the robot as long as we are training. Run in its own thread to not block.
            while (isTraining) {
                val steeringPercentage = controller.getCurrentSteeringSensorAngleInPercent()
                val wantedTachoCount = convertSteeringPercentageToTachoCount(steeringPercentage)
                controller.steeringRotateToTachoCount(wantedTachoCount)
            }
        }

        // The robot must run forward when training
        controller.drivingForward()
    }

    fun stopTraining() {
        isTraining = false
    }

    private fun convertSteeringPercentageToTachoCount(percentage: Float): Int {
        return (mostRightTachoCount * (100 - percentage) / 100).toInt()
    }
}