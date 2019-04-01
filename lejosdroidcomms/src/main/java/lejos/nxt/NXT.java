package lejos.nxt;

import lejos.pc.comm.*;
import java.io.*;
import lejos.nxt.remote.*;

/**
 * Abstraction for the local NXT device.
 * 
 * This version of the NXT class supports remote execution.
 * 
 * @author Lawrie Griffiths and Brian Bagnall
 *
 */
public class NXT {
	private static NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();

	/**
	 * Get the (emulated) standard LEGO firmware version number
	 * 
	 * @return the version number
	 */
	public static float getFirmwareVersion() {
		try {
			FirmwareInfo f = nxtCommand.getFirmwareVersion();
			return Float.parseFloat(f.firmwareVersion);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return 0f;
		}		
	}

	/**
	 * Get the LEGO Communication Protocol version number 
	 * 
	 * @return the version number
	 */
	public static float getProtocolVersion() {
		try {
			FirmwareInfo f = nxtCommand.getFirmwareVersion();
			return Float.parseFloat(f.protocolVersion);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return 0f;
		}	
	}
	
	/**
	 * Get the number of bytes of free flash memory
	 * @return Free memory remaining in FLASH
	 */
	public static int getFlashMemory() {
		try {
			DeviceInfo i = nxtCommand.getDeviceInfo();
			return i.freeFlash;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return 0;
		}	 
	}
	
	/**
	 * Deletes all user programs and data in FLASH memory
	 * @return the status
	 */
	public static byte deleteFlashMemory() {
		try {
			return nxtCommand.deleteUserFlash(); 
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	/**
	 * Get the friendly name of the brick
	 * 
	 * @return the friendly name
	 */
	public static String getBrickName() {
		try {
			DeviceInfo i = nxtCommand.getDeviceInfo();
			return i.NXTname;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return null;
		}
		
	}

	/**
	 * Set the friendly name of the brick
	 * 
	 * @return the status code
	 */
	public static byte setBrickName(String newName) {
		try {
			return nxtCommand.setFriendlyName(newName);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	/**
	 * This doesn't seem to be implemented in Lego NXT firmware/protocol?
	 * @return Seems to return 0 every time
	 */
	public static int getSignalStrength() {
		try {
			DeviceInfo i = nxtCommand.getDeviceInfo();
			return i.signalStrength;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}		
	}
	
	/**
	 * Close the connection to the NXT and exit
	 * 
	 * @param code the exit code
	 */
	public static void exit(int code) {
		try {
			NXTCommandConnector.close();
		} catch (IOException ioe) {}
		
		System.exit(code);
	}
}
