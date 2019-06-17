
package lejos.robotics;
/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for touch sensors
 *
 * @author Andy
 *
 */
public interface Touch {
	/**
	 * Check if the sensor is pressed.
	 * @return <code>true</code> if sensor is pressed, <code>false</code> otherwise.
	 */
	public boolean isPressed();
}
