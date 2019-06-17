package lejos.nxt;

import lejos.robotics.Encoder;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a motor port that supports NXT motors with tachometers.
 * 
 * @author Lawrie Griffiths
 *
 */
public interface TachoMotorPort extends BasicMotorPort, Encoder {
}