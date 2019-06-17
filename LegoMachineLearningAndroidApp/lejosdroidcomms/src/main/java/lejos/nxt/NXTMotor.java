package lejos.nxt;

import lejos.robotics.Encoder;
import lejos.robotics.EncoderMotor;
/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for an NXT motor with no speed regulation.
 * 
 */
public class NXTMotor extends BasicMotor implements EncoderMotor {
    protected Encoder encoderPort;

    /**
     * Create an instance of a NXTMotor using the specified motor port and
     * PWM operating mode.
     * @param port The motor port that the motor will be attached to.
     * @param PWMMode see {@link lejos.nxt.BasicMotorPort#PWM_FLOAT} and see {@link lejos.nxt.BasicMotorPort#PWM_BRAKE}
     */
    public NXTMotor(TachoMotorPort port, int PWMMode)
    {
        this.port = port;
        // We use extra var to avoid cost of a cast check later
        encoderPort = port;
        port.setPWMMode(PWMMode);
    }
    /**
     * Create an instance of a NXTMotor using the specified motor port the
     * PWM operating mode will be PWM_BREAK {@link lejos.nxt.BasicMotorPort#PWM_BRAKE}
     * @param port The motor port that the motor will be attached to.
     */
	public NXTMotor(TachoMotorPort port)
	{
        this(port, TachoMotorPort.PWM_BRAKE);
	}
    
    public int getTachoCount()
    {
        return encoderPort.getTachoCount();
    }

    public void resetTachoCount()
    {
        encoderPort.resetTachoCount();
    }
}
