package lejos.nxt.addon;

import lejos.nxt.BasicMotorPort;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports a motor connected to the Mindsensors RCX Motor Multiplexer
 * 
 * @author Lawrie Griffiths
 *
 */
public class RCXPlexedMotorPort implements BasicMotorPort {
	private RCXMotorMultiplexer plex;
	private int id;
	
	public RCXPlexedMotorPort(RCXMotorMultiplexer plex, int id) {
		this.plex = plex;
		this.id = id;
	}
	
	public void controlMotor(int power, int mode) {
		int mmMode = mode;
		if (mmMode == BasicMotorPort.FLOAT) mmMode = 0; // float
		int mmPower = (int) (power * 2.55f);
		if (mmMode == BasicMotorPort.STOP) {
			mmPower = 255; // Maximum breaking
		}
		plex.sendCommand(id, mmMode, mmPower);
	}
	
	public void setPWMMode(int mode) {
		// Not implemented
	}
}
