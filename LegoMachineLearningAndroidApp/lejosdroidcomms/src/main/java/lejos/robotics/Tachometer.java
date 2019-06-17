package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a Tachometer, which monitors speed of the encoder.
 *
 * @author BB
 *
 */
public interface Tachometer extends Encoder {
	
	
	  /**
	   * Returns the actual speed.
	   * 
	   * @return speed in degrees per second, negative value means motor is rotating backward
	   */
	  int getRotationSpeed();

}
