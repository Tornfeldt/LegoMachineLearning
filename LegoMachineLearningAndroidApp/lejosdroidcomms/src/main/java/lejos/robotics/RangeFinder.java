package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a range finder sensor that returns the distance to the nearest object
 * @see lejos.robotics.RangeScanner
 * @author Lawrie Griffiths
 */
public interface RangeFinder {
	/**
	 * Get the range to the nearest object
	 * 
	 * @return the distance to the nearest object
	 */
	public float getRange();
	
	/**
	 * If the sensor is capable, this method returns multiple range values from a single scan. Sensors that can only
	 * return a single value should return an array containing a single value.
	 * 
	 * @return an array of ranges, ordered from closest to farthest.
	 */
	public float [] getRanges();
}
