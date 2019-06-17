package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * An EncoderMotor is a platform independent interface for an <i>unregulated motor</i>
 * that also has basic tachometer functions.
 * 
 * @author BB
 *
 */
public interface EncoderMotor extends DCMotor, Encoder {
	// no extra methods
}
