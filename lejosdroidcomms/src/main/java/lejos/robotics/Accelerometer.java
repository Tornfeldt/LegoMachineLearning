package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Interface for Acceleration sensors
 * 
 * @author Lawrie Griffiths
 *
 */
public interface Accelerometer {	
	/**
	 * Measures the x-axis of the accelerometer, in meters/second^2.
	 * @return acceleration in m/s^2
	 */
	public int getXAccel();
	
	/**
	 * Measures the y-axis of the accelerometer, in meters/second^2.
	 * @return acceleration in m/s^2
	 */
	public int getYAccel();
	/**
	 * Measures the z-axis of the accelerometer, in meters/second^2.
	 * @return acceleration in m/s^2
	 */
	public int getZAccel();
}
