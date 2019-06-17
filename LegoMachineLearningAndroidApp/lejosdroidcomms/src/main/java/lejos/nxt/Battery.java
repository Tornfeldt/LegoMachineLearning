package lejos.nxt;

import lejos.pc.comm.*;
import lejos.nxt.remote.*;

/**
 * Battery class that supports remote execution.
 * 
 * Usage: int x = Battery.getVoltageMilliVolt();
 * 
 * @author Brian Bagnall and Lawrie Griffiths
 *
 */
public class Battery implements NXTProtocol {
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
	private static RemoteBattery remoteBattery = new RemoteBattery(nxtCommand);
	
	// Ensure no one tries to instantiate this.
	private Battery() {}
		
	/**
	 * The NXT uses 6 batteries of 1500 mV each.
	 * @return Battery voltage in mV. ~9000 = full.
	 */
	public static int getVoltageMilliVolt() {
		return remoteBattery.getVoltageMilliVolt();
	}

	/**
	 * The NXT uses 6 batteries of 1.5 V each.
	 * @return Battery voltage in Volt. ~9V = full.
	 */
	public static float getVoltage()  {
	   return remoteBattery.getVoltage();
	}
}

