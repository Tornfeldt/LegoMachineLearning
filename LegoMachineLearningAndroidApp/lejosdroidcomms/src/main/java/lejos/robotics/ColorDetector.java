package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This interface defines the methods of a generic ColorDetector object.
 * 
 * @see lejos.nxt.ColorSensor
 * @see lejos.nxt.addon.ColorHTSensor
 */
public interface ColorDetector {

	/**
	 * Return the Red, Green and Blue values together in one object.
	 * @return Color object containing the three RGB component values between 0-255.
	 */
	public Color getColor();
	
	/**
	 * Return an enumerated constant that indicates the color detected. e.g. Color.BLUE
	 * @return An integer from the Color constants, such as Color.BLUE
	 */
	public int getColorID();
	
}
