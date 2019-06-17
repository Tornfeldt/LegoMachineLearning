package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.Accelerometer;
import lejos.util.EndianTools;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This class works with the Mindsensors acceleration (tilt) sensor ACCL-Nx-v2/v3.
 * 
 * TODO there are a lot of features not supported by this class
 */
public class AccelMindSensor extends I2CSensor implements Accelerometer {
	/*
	 * Documentation: http://www.mindsensors.com/index.php?module=pagemaster&PAGE_user_op=view_page&PAGE_id=101
	 * Some details from MSAC-driver.h from http://rdpartyrobotcdr.sourceforge.net/
	 */
	
	private byte[] buf = new byte[6];

	private static final byte BASE_TILT = 0x42;
	private static final byte OFF_X_TILT = 0x00;
	private static final byte OFF_Y_TILT = 0x01;
	private static final byte OFF_Z_TILT = 0x02;

	private static final byte BASE_ACCEL = 0x45;
	private static final byte OFF_X_ACCEL = 0x00;
	private static final byte OFF_Y_ACCEL = 0x02;
	private static final byte OFF_Z_ACCEL = 0x04;
	
	public static final int ERROR = Integer.MIN_VALUE;

	public AccelMindSensor(I2CPort port) {
		this(port, DEFAULT_I2C_ADDRESS);
	}

	public AccelMindSensor(I2CPort port, int address) {
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED_9V);
	}

	/**
	 * Tilt of sensor along X-axis (see top of Mindsensors.com sensor for
	 * diagram of axis). 0 is level.
	 * 
	 * @return X tilt value in degrees, or {@link #ERROR} if call failed
	 */
	public int getXTilt() {
		int ret = getData(BASE_TILT + OFF_X_TILT, buf, 1);
		if (ret != 0)
			return ERROR;

		return (buf[0] & 0xFF) - 128;
	}

	/**
	 * Returns Y tilt value (see top of Mindsensors.com sensor for
	 * diagram of axis). 0 is level.
	 * 
	 * @return Y tilt value in degrees, or {@link #ERROR} if call failed
	 */
	public int getYTilt() {
		int ret = getData(BASE_TILT + OFF_Y_TILT, buf, 1);
		if (ret != 0)
			return ERROR;

		return (buf[0] & 0xFF) - 128;
	}

	/**
	 * Returns Z tilt value (see top of Mindsensors.com sensor for
	 * diagram of axis). 0 is level.
	 * 
	 * @return Z tilt value in degrees, or {@link #ERROR} if call failed
	 */
	public int getZTilt() {
		int ret = getData(BASE_TILT + OFF_Z_TILT, buf, 1);
		if (ret != 0)
			return ERROR;

		return (buf[0] & 0xFF) - 128;
	}

	/**
	 * Acceleration along X axis. Positive or negative values in mg. (g =
	 * acceleration due to gravity = 9.81 m/s^2)
	 * 
	 * @return Acceleration e.g. 9810 mg (falling on earth) or {@link #ERROR}.
	 */
	public int getXAccel() {
		int ret = getData(BASE_ACCEL + OFF_X_ACCEL, buf, 2);
		if (ret != 0)
			return ERROR;
		
		return EndianTools.decodeShortLE(buf, 0);
	}

	/**
	 * Acceleration along Y axis. Positive or negative values in mg. (g =
	 * acceleration due to gravity = 9.81 m/s^2)
	 * 
	 * @return Acceleration e.g. 9810 mg (falling on earth) or {@link #ERROR}.
	 */
	public int getYAccel() {
		int ret = getData(BASE_ACCEL + OFF_Y_ACCEL, buf, 2);
		if (ret != 0)
			return ERROR;
		
		return EndianTools.decodeShortLE(buf, 0);
	}

	/**
	 * Acceleration along Z axis. Positive or negative values in mg. (g =
	 * acceleration due to gravity = 9.81 m/s^2)
	 * 
	 * @return Acceleration e.g. 9810 mg (falling on earth) or {@link #ERROR}.
	 */
	public int getZAccel() {
		int ret = getData(BASE_ACCEL + OFF_Z_ACCEL, buf, 2);
		if (ret != 0)
			return ERROR;
		
		return EndianTools.decodeShortLE(buf, 0);
	}
	
	/**
	 * Reads all 3 tilt values into the given array.
	 * Elements off+0, off+1, and off+2 are filled with X, Y, and Z axis.
	 * @param dst destination array.
	 * @param off offset
	 * @return true on success, false on error
	 */
	public boolean getAllTilt(int[] dst, int off) {
		int ret = getData(BASE_TILT, buf, 0, 3);
		if (ret != 0)
			return false;
		
		dst[off+0] = (buf[0] & 0xFF) - 128;
		dst[off+1] = (buf[1] & 0xFF) - 128;
		dst[off+2] = (buf[2] & 0xFF) - 128;
		return true;
	}
	
	/**
	 * Reads all 3 acceleration values into the given array.
	 * Elements off+0, off+1, and off+2 are filled with X, Y, and Z axis.
	 * @param dst destination array.
	 * @param off offset
	 * @return true on success, false on error
	 */
	public boolean getAllAccel(int[] dst, int off) {
		int ret = getData(BASE_ACCEL, buf, 0, 6);
		if (ret != 0)
			return false;
		
		dst[off+0] = EndianTools.decodeShortLE(buf, OFF_X_ACCEL);
		dst[off+1] = EndianTools.decodeShortLE(buf, OFF_Y_ACCEL);
		dst[off+2] = EndianTools.decodeShortLE(buf, OFF_Z_ACCEL);
		return true;
	}
}
