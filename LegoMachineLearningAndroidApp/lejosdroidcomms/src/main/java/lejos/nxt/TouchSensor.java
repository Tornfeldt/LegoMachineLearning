package lejos.nxt;
import lejos.robotics.Touch;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a NXT touch sensor.
 * Also works with RCX touch sensors.
 * 
 */
public class TouchSensor implements SensorConstants, Touch {
	ADSensorPort port;
	
	/**
	 * Create a touch sensor object attached to the specified port.
	 * @param port an Analog/Digital port, e.g. SensorPort.S1
	 */
	public TouchSensor(ADSensorPort port)
	{
	   this.port = port;
	   port.setTypeAndMode(TYPE_SWITCH, MODE_BOOLEAN);
	}
	
	/**
	 * Check if the sensor is pressed.
	 * @return <code>true</code> if sensor is pressed, <code>false</code> otherwise.
	 */
	public boolean isPressed()
	{
		return (port.readRawValue() < 600);  
	}
}
