package lejos.nxt.remote;

import java.io.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Battery readings from a remote NXT.
 */
public class RemoteBattery implements NXTProtocol {
	
	private NXTCommand nxtCommand;
	
	public RemoteBattery(NXTCommand nxtCommand) {
		this.nxtCommand = nxtCommand;
	}
		
	/**
	 * The NXT uses 6 batteries of 1500 mV each.
	 * @return Battery voltage in mV. ~9000 = full.
	 */
	public int getVoltageMilliVolt() {
		/*
	     * calculation from LEGO firmware
	     */
		try {
			return nxtCommand.getBatteryLevel();
		} catch (IOException ioe) {
			return 0;
		}
	}

	/**
	 * The NXT uses 6 batteries of 1.5 V each.
	 * @return Battery voltage in Volt. ~9V = full.
	 */
	public float getVoltage() {
	   return (float)(getVoltageMilliVolt() * 0.001);
	}
}

