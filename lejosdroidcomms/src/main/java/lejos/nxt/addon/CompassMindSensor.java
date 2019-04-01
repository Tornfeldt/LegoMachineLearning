package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.*;
/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This class supports the <a href="http://mindsensors.com">Mindsensors</a> compass sensor.
 * 
 */
public class CompassMindSensor extends I2CSensor implements DirectionFinder {
	byte[] buf = new byte[2];
	private float cartesianCalibrate = 0; // Used by both cartesian methods. 
	
	// Mindsensors.com constants:
	private final static byte COMMAND = 0x41;
	private final static byte BEGIN_CALIBRATION = 0x43;
	private final static byte END_CALIBRATION = 0x44;
	
    /**
     * Create a compass sensor object
     * @param port Sensor port for the compass
     * @param address The I2C address used by the sensor
     */
	public CompassMindSensor(I2CPort port, int address)
	{
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
	}

   /**
     * Create a compass sensor object
     * @param port Sensor port for the compass
     */
	public CompassMindSensor(I2CPort port)
    {
        this(port, DEFAULT_I2C_ADDRESS);
    }
	
	/**
	 * Returns the directional heading in degrees. (0 to 359.9)
	 * 0 is due North (on Mindsensors circuit board a white arrow indicates
	 * the direction of compass). Reading increases clockwise.
	 * @return Heading in degrees. Resolution is within 0.1 degrees
	 */
	public float getDegrees() {		
		int ret = getData(0x42, buf, 2);
		if(ret != 0) return -1;

		// TODO: The following commented out code works when Mindsensors compass in integer mode
		// Add ability to set to integer mode.
		/*int iHeading = (0xFF & buf[0]) | ((0xFF & buf[1]) << 8);
		float dHeading = iHeading / 10.00F;*/
		
		// Byte mode (default - will use Integer mode later)
		int dHeading = (0xFF & buf[0]);
		dHeading = dHeading * 360;
		dHeading = dHeading / 255;
		return dHeading;

	}
	/**
	 * Compass readings increase clockwise from 0 to 360, but Cartesian
	 * coordinate systems increase counter-clockwise. This method returns
	 * the Cartesian compass reading. Also, the resetCartesianZero() method
	 * can be used to designate any direction as zero, rather than relying
	 * on North as being zero.
	 * @return Cartesian direction.
	 */
	public float getDegreesCartesian() {
		float degrees = cartesianCalibrate - getDegrees() ;
		if(degrees>=360) degrees -= 360;
		if(degrees<0) degrees += 360;
		return degrees;
	}
	
	/**
	 * Changes the current direction the compass is facing into the zero 
	 * angle. 
	 *
	 */
	public void resetCartesianZero() {
		cartesianCalibrate = getDegrees();
	}
	
	/**
	 * Starts calibration for Mindsensors.com compass. Must rotate *very* 
	 * slowly ,taking at least 20 seconds per rotation.
	 * At least 2 full rotations.
	 * Must call stopCalibration() when done.
	 */
	public void startCalibration() {
		buf[0] = BEGIN_CALIBRATION; 
		// Same value for HiTechnic and Mindsensors
		super.sendData(COMMAND, buf, 1);
	}
	
	/**
	 * Ends calibration sequence.
	 *
	 */
	public void stopCalibration() {
		buf[0] = END_CALIBRATION;
		super.sendData(COMMAND, buf, 1);
	}
}