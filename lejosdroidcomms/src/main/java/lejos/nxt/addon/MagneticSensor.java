package lejos.nxt.addon;

import lejos.nxt.ADSensorPort;
import lejos.nxt.SensorConstants;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Support for HiTechnic Magnetic sensor
 */
public class MagneticSensor implements SensorConstants {
    protected ADSensorPort port;
	private int offset = 0;
	
    public MagneticSensor(ADSensorPort port) {
		this.port = port;
		port.setTypeAndMode(TYPE_CUSTOM, MODE_RAW);
	}
    
	public MagneticSensor(ADSensorPort port, int offset) {
		this(port);
		this.offset = offset;
	}
	
	/**
	 * Get the relative magnetic field strength
	 * @return the relative magnetic field strength
	 */
	public int readValue() { 
		return (port.readRawValue() - offset); 
	}
}
	
	
