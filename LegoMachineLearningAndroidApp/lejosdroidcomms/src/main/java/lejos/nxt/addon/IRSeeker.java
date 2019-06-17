package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * HiTechnic IRSeeker sensor - untested.
 * www.hitechnic.com
 * 
 */
public class IRSeeker extends I2CSensor {
	byte[] buf = new byte[1];
	
	public IRSeeker(I2CPort port)
	{
		super(port);
	}
	
	/**
	 * Returns the direction of the target (1-9)
	 * or zero if no target. 
	 * 
	 * @return direction
	 */
	public int getDirection() {
		int ret = getData(0x42, buf, 1);
		if(ret != 0) return -1;
		return (0xFF & buf[0]);
	}
	
	/**
	 * Returns value of sensor 1 - 5.
	 * 
	 * @return sensor value (0 to 255).
	 */
	public int getSensorValue(int id) {
		if (id <= 0 || id > 5) return -1;
		int ret = getData(0x42 + id, buf, 1);
		if(ret != 0) return -1;
		return (0xFF & buf[0]);
	}
}
