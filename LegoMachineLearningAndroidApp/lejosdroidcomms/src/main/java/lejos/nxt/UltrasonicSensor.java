package lejos.nxt;

import lejos.robotics.RangeFinder;
import lejos.util.Delay;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a NXT Ultrasonic Sensor. The sensor knows four modes: off,
 * continuous, ping and capture. In continuous mode, the device periodically
 * tests, whether and object in range. In ping mode, the sensor sends a single
 * ping, and records the distance of up to 8 objects. In capture mode, the
 * sensor listens for signals from other UltraSonic devices. At the present
 * time, we (the leJOS team) are not sure, how the capture mode works.
 * Please let us know about your experience with it.
 */
public class UltrasonicSensor extends I2CSensor implements RangeFinder
{
	/* Device modes */
	public static final byte MODE_OFF = 0x00;
	public static final byte MODE_PING = 0x01;
	public static final byte MODE_CONTINUOUS = 0x02;
	public static final byte MODE_CAPTURE = 0x03;
	public static final byte MODE_RESET = 0x04;
	/* Device control locations */
	private static final byte REG_FACTORY_DATA = 0x11;
	private static final byte REG_UNITS = 0x14;
	private static final byte REG_CONTINUOUS_INTERVAL = 0x40;
	private static final byte REG_MODE = 0x41;
	private static final byte REG_DISTANCE = 0x42;
	private static final byte REG_CALIBRATION = 0x4a;
	/* Device timing */
	private static final int DELAY_CMD = 5;
	private static final int DELAY_DATA_PING = 50;
	private static final int DELAY_DATA_OTHER = 30;

	private long nextCmdTime = 0;
	private long dataAvailableTime = 0;
	private byte mode = MODE_CONTINUOUS;
	private byte[] byteBuff = new byte[8];

	/**
	 * Wait until the specified time
	 */
	private void waitUntil(long when)
	{
		long delay = when - System.currentTimeMillis();
		Delay.msDelay(delay);
	}

	/*
	 * Over-ride standard get function to ensure correct inter-command timing
	 * when using the ultrasonic sensor. The Lego Ultrasonic sensor uses a
	 * "bit-banged" i2c interface and seems to require a minimum delay between
	 * commands otherwise the commands fail.
	 */
	@Override
	public synchronized int getData(int register, byte[] buf, int off, int len)
	{
		waitUntil(nextCmdTime);
		int ret = super.getData(register, buf, off, len);
		nextCmdTime = System.currentTimeMillis() + DELAY_CMD;
		return ret;
	}

	/*
	 * Over-ride the standard send function to ensure the correct inter-command
	 * timing for the ultrasonic sensor.
	 */
	@Override
	public synchronized int sendData(int register, byte[] buf, int off, int len)
	{
		waitUntil(nextCmdTime);
		int ret = super.sendData(register, buf, off, len);
		nextCmdTime = System.currentTimeMillis() + DELAY_CMD;
		return ret;
	}

	public UltrasonicSensor(I2CPort port)
	{
		// Set correct sensor type, default is TYPE_LOWSPEED
		super(port, DEFAULT_I2C_ADDRESS, I2CPort.LEGO_MODE, TYPE_LOWSPEED_9V);
		nextCmdTime = System.currentTimeMillis() + DELAY_CMD;
		// Perform a reset, to clean up settings from previous program
		if (this.reset() >= 0)
            setMode(MODE_CONTINUOUS);
        // Not sure what we should do if the reset fails!
	}

	/**
	 * Return distance to an object. To ensure that the data returned is valid
	 * this method may have to wait a short while for the distance data to
	 * become available.
	 * 
	 * @return distance or 255 if no object in range or an error occurred
	 */
	public int getDistance()
	{
		int delay;
		switch (mode)
		{
			case MODE_OFF:
				throw new IllegalStateException("sensor is off");
			case MODE_PING:
				delay = DELAY_DATA_PING;
				break;
			default:
				delay = DELAY_DATA_OTHER;
		}

		waitUntil(dataAvailableTime);
		int ret = getData(REG_DISTANCE, byteBuff, 1);
		if (ret < 0)
			return 255;

		// Make a note of when new data should be available.
		dataAvailableTime = System.currentTimeMillis() + delay;

		return byteBuff[0] & 0xFF;
	}

	/**
	 * {@inheritDoc}
	 */
	public float getRange()
	{
		return getDistance();
	}

	/**
	 * Return an array of distances. If in continuous mode, at most one distance
	 * is returned. If in ping mode, up to 8 distances are returned. If the
	 * distance data is not yet available the method will wait until it is. Same
	 * as <code>getDistances(dist, 0, dist.length)</code>.
	 * 
	 * @param dist the destination array
	 * @return negative value on error, the number of values returned otherwise
	 */
	public int getDistances(int dist[])
	{
		return getDistances(dist, 0, dist.length);
	}

	/**
	 * Return an array of distances. If in continuous mode, at most one distance
	 * is returned. If in ping mode, up to 8 distances are returned, but not
	 * more than <code>len</code>. If the distance data is not yet available the
	 * method will wait until it is. The LEGO ultrasonic sensor can return multiple 
	 * readings in approx. 75 ms.
	 * 
	 * @param dist the destination array
	 * @param off the index of the first distance
	 * @param len the number of distances to read
	 * @return negative value on error, the number of distances returned
	 *         otherwise
	 */
	public int getDistances(int dist[], int off, int len)
	{
		if (len <= 0)
		{
			if (len == 0)
				return 0;
			
			throw new IllegalArgumentException("len is negative");
		}

		int delay = DELAY_DATA_OTHER;
		int maxlen = 8;
		switch (mode)
		{
			case MODE_OFF:
				throw new IllegalStateException("sensor is off");
			case MODE_PING:
				delay = DELAY_DATA_PING;
				break;
			case MODE_CAPTURE:
				maxlen = 1;
				break;
			default:
				// MODE_CONTINUOUS
				break;
		}

		if (len > maxlen)
			len = maxlen;

		waitUntil(dataAvailableTime);
		int ret = getData(REG_DISTANCE, byteBuff, len);
		if (ret < 0)
			return ret;

		dataAvailableTime = System.currentTimeMillis() + delay;
		int i;
		for (i = 0; i < len && byteBuff[i] != -1; i++)
			dist[off + i] = byteBuff[i] & 0xff;

		return i;
	}

	/**
	 * Set the sensor into the specified mode. A value of {@link #MODE_RESET}
	 * will reset the sensor. After a reset, the sensor will be off. A value of
	 * {@link #MODE_PING} sends a single ping.
	 * 
	 * @param mode the mode, either {@link #MODE_OFF}, {@link #MODE_RESET}, {@link #MODE_CONTINUOUS}, {@link #MODE_PING}, or {@link #MODE_CAPTURE}
	 * @return a negative value on error, 0 otherwise
	 * @see #ping()
	 * @see #continuous()
	 * @see #capture()
	 * @see #reset()
	 * @see #off()
	 */
	public int setMode(int mode)
	{
		int delay = 0;
		byte modeNew = (byte)mode;
		switch (mode)
		{
			case MODE_RESET:
				// Is off after a reset;
				modeNew = MODE_OFF;
				break;
			case MODE_PING:
				delay = DELAY_DATA_PING;
				break;
			case MODE_OFF:
			case MODE_CAPTURE:
			case MODE_CONTINUOUS:
				delay = DELAY_DATA_OTHER;
				break;
			default:
				throw new IllegalArgumentException("unknown mode");
		}

		byteBuff[0] = (byte)mode;
		int ret = sendData(REG_MODE, byteBuff, 1);
		if (ret == 0)
		{
			// Make a note of when the data will be available
			dataAvailableTime = System.currentTimeMillis() + delay;
			this.mode = modeNew;
		}
		return ret;
	}

	/**
	 * Send a single ping. Up to 8 echoes are captured. These may be read by
	 * making a call to {@link #getDistances(int[])} or
	 * {@link #getDistances(int[], int, int)} and passing a suitable array. 
	 * A delay is required between the call to {@link #ping()} and
	 * obtaining the results. The getDistances methods automatically takes
	 * care of this. {@link #getDistance()} may also be used with ping, returning
	 * information for the first echo.
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int ping()
	{
		return setMode(MODE_PING);
	}

	/**
	 * Switch to continuous mode.
	 * The device will periodically test, whether an object is in range.
	 * The current distance of the object can be obtained using
	 * {@link #getDistance()}. Calling {@link #getDistances(int[])}
	 * and {@link #getDistances(int[], int, int)} is also supported,
	 * but at most 1 distance is returned.
	 * 
	 * @return negative value on error, 0 otherwise
	 * @see #ping()
	 */
	public int continuous()
	{
		return setMode(MODE_CONTINUOUS);
	}

	/**
	 * Turn off the sensor. This call disables the sensor. No pings will be
	 * issued after this call, until either ping, continuous or reset is called.
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int off()
	{
		return setMode(MODE_OFF);
	}

	/**
	 * Set capture mode Set the sensor into capture mode. The Lego documentation
	 * states: "Within this mode the sensor will measure whether any other
	 * ultrasonic sensors are within the vicinity. With this information a
	 * program can evaluate when it is best to make a new measurement which will
	 * not conflict with other ultrasonic sensors." I have no way of testing
	 * this. Perhaps someone with a second NXT could check it out!
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int capture()
	{
		return setMode(MODE_CAPTURE);
	}

	/**
	 * Reset the device. Performs a "soft reset" of the device. Restores things
	 * to the default state. Following this call the sensor will be off.
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int reset()
	{
		return setMode(MODE_RESET);
	}

	private int getMultiBytes(int reg, byte data[], int len)
	{
		/*
		 * For some locations that are adjacent in address it is not possible to
		 * read the locations in a single read, instead we must read them using
		 * a series of individual reads. No idea why this should be, but that is
		 * how it is!
		 */
		for (int i = 0; i < len; i++)
		{
			int ret = getData(reg + i, byteBuff, 1);
			if (ret < 0)
				return ret;
			data[i] = byteBuff[0];
		}
		return 0;
	}

	private int setMultiBytes(int reg, byte data[], int len)
	{
		/*
		 * For some locations that are adjacent in address it is not possible to
		 * read the locations in a single write, instead we must write them
		 * using a series of individual writes. No idea why this should be, but
		 * that is how it is!
		 */
		for (int i = 0; i < len; i++)
		{
			byteBuff[0] = data[i];
			int ret = sendData(reg + i, byteBuff, 1);
			if (ret < 0)
				return ret;
		}
		return 0;
	}

	/**
	 * Return 10 bytes of factory calibration data. The bytes are as follows
	 * data[0] : Factory zero (cal1) data[1] : Factory scale factor (cal2)
	 * data[2] : Factory scale divisor.
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int getFactoryData(byte data[])
	{
		if (data.length < 3)
			throw new IllegalArgumentException("array too small");

		return getMultiBytes(REG_FACTORY_DATA, data, 3);
	}

	/**
	 * Return a string indicating the type of units in use by the unit. The
	 * default response is 10E-2m indicating centimeters in use.
	 * 
	 * @return "" on error, the units string otherwise
	 */
	public String getUnits()
	{
		return fetchString(REG_UNITS, 8);
	}

	/**
	 * Return 3 bytes of calibration data. The bytes are as follows data[0] :
	 * zero (cal1) data[1] : scale factor (cal2) data[2] : scale divisor.
	 * 
	 * @return negative value on error, 0 otherwise
	 */
	public int getCalibrationData(byte data[])
	{
		/*
		 * Note the lego documentation says this is at loacation 0x50, however
		 * it looks to me like this is a hex v decimal thing and it should be
		 * location 0x49 + 1 which is 0x4a not 0x50! There certainly seems to be
		 * valid data at 0x4a...
		 */
		if (data.length < 3)
			throw new IllegalArgumentException("array too small");

		return getMultiBytes(REG_CALIBRATION, data, 3);
	}

	/**
	 * Set 3 bytes of calibration data. The bytes are as follows data[0] : zero
	 * (cal1) data[1] : scale factor (cal2) data[2] : scale divisor. This does
	 * not currently seem to work.
	 * 
	 * @return 0 if ok <> 0 otherwise
	 */
	public int setCalibrationData(byte data[])
	{
		if (data.length < 3)
			throw new IllegalArgumentException("array too small");

		return setMultiBytes(REG_CALIBRATION, data, 3);
	}

	/**
	 * Return the interval used in continuous mode. This seems to be in the
	 * range 1-15. It can be read and set. However tests seem to show it has no
	 * effect. Others have reported that this does vary the ping interval (when
	 * used in other implementations). Please report any new results.
	 * 
	 * @return negative value on error, the interval otherwise
	 */
	public int getContinuousInterval()
	{
		int ret = getData(REG_CONTINUOUS_INTERVAL, byteBuff, 1);
		return ret < 0 ? ret : (byteBuff[0] & 0xFF);
	}

	/**
	 * Set the ping inetrval used when in continuous mode. See
	 * getContinuousInterval for more details.
	 * 
	 * @return negative value on error, 0 otherwise.
	 */
	public int setContinuousInterval(int interval)
	{
		if (interval < 0 || interval > 0xFF)
			throw new IllegalArgumentException("value between 0 and 0xFF expected");

		byteBuff[0] = (byte)interval;
		return sendData(REG_CONTINUOUS_INTERVAL, byteBuff, 1);
	}

	/**
	 * Returns the current operating mode of the sensor.
	 * 
	 * @return the operating mode
	 * @see #MODE_OFF
	 * @see #MODE_CONTINUOUS
	 * @see #MODE_PING
	 * @see #MODE_CAPTURE
	 */
	public int getMode()
	{
		return this.mode;
	}
	
	/**
	 * Returns the current operating mode of the sensor. In contrast to
	 * {@link #getMode()}, this method determines the operation mode by actually
	 * querying the hardware.
	 * 
	 * @return negative value on error, the operating mode otherwise
	 * @see #MODE_OFF
	 * @see #MODE_CONTINUOUS
	 * @see #MODE_PING
	 * @see #MODE_CAPTURE
	 */
	public int getActualMode()
	{
		int ret = getData(REG_MODE, byteBuff, 1);
		return ret < 0 ? ret : (byteBuff[0] & 0xFF);
	}

	public float[] getRanges() {
		//int oldMode = getMode();
		ping();
		int [] dists = new int[8]; // 8 is max number of return pings from ultrasonic sensor
		int numScans = getDistances(dists);
		if (numScans <= 0) return new float[0];
		float [] distsF = new float[numScans];
		for(int i=0;i<numScans;i++){
			distsF[i] = dists[i];
		}
		//setMode(oldMode);
		return distsF;
	}
}