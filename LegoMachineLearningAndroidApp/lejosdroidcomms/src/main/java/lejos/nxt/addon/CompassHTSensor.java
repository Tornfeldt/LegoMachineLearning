package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.*;
/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This class supports the <a href="http://www.hitechnic.com">HiTechnic</a> compass sensor.
 * 
 */
public class CompassHTSensor extends I2CSensor implements DirectionFinder {
	byte[] buf = new byte[2];
	
	private float cartesianCalibrate = 0; // Used by both cartesian methods. 
	
	private final static byte COMMAND = 0x41;
	private final static byte BEGIN_CALIBRATION = 0x43;
	private final static byte MEASUREMENT_MODE = 0x00;

    /**
     * Create a compass sensor object
     * @param port Sensor port for the compass
     * @param address The I2C address used by the sensor
     */
	public CompassHTSensor(I2CPort port, int address)
	{
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
	}

   /**
     * Create a compass sensor object
     * @param port Sensor port for the compass
     */
	public CompassHTSensor(I2CPort port)
    {
        this(port, DEFAULT_I2C_ADDRESS);
    }
	
	/**
	 * Returns the directional heading in degrees. (0 to 359.9)
	 * 0 is due North. Readings increase clockwise.
	 * @return Heading in degrees. Resolution is within 0.1 degrees
	 */
	public float getDegrees() {		
		int ret = getData(0x42, buf, 2);
		if(ret != 0) return -1;
		
		return ((buf[0] & 0xff)<< 1) + buf[1];
	}
	/**
	 * Cartesian coordinate systems increase from 0 to 360 counter-clockwise, but Compass readings 
	 * increase clockwise . This method returns the Cartesian compass reading. Also, the resetCartesianZero()
	 * method can be used to designate any direction as zero, rather than relying on North as being zero.
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
	 * angle for the method getDegreesCartesian(). 
	 *
	 */
	public void resetCartesianZero() {
		cartesianCalibrate = getDegrees();
	}
	
	// TODO: Rotate in any direction while calibrating? Specify.
	/**
	 * Starts calibration for the compass. Must rotate *very* 
	 * slowly, taking at least 20 seconds per rotation.
	 * 
	 * Should make 1.5 to 2 full rotations.
	 * Must call stopCalibration() when done.
	 */
	public void startCalibration() {
		buf[0] = BEGIN_CALIBRATION; 
		super.sendData(COMMAND, buf, 1);
	}
	
	/**
	 * Ends calibration sequence.
	 *
	 */
	public void stopCalibration() {
		buf[0] = MEASUREMENT_MODE;
		super.sendData(COMMAND, buf, 1);
	}
}