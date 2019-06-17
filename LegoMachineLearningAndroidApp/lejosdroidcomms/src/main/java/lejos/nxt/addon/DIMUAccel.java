package lejos.nxt.addon;
import java.io.*;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.Accelerometer;

/**
 * This class provides access to the accelerometer of Dexter Industries IMU sensor
 * @author Aswin Bouwmeester
 * @version 1.0
 */
public class DIMUAccel extends I2CSensor implements Accelerometer {

	/**
	 * List of possible units for acceleration
	 */
	public enum AccelUnits {

		/**
		 * Acceleration in milli-G
		 */
		MILLIG,
		/**
		 * Acceleration in G
		 */
		G,
		/**
		 * Acceleration in m/s^2
		 */
		MS2,
		/**
		 * Acceleration in mm/s^2
		 */
		MILLIMS2;

		/**
		 * Converts a value to specified unit
		 * 
		 * @param value
		 *          Value to convert.
		 * @param unit
		 *          Unit to convert to.
		 * @return Converted value.
		 */
		public float convertTo(float value, AccelUnits unit) {
			if (this == unit)
				return value;
			if (this == AccelUnits.MILLIG && unit == AccelUnits.G)
				return value * 0.001f;
			if (this == AccelUnits.MILLIG && unit == AccelUnits.MS2)
				return value * 0.00981f;
			if (this == AccelUnits.MILLIG && unit == AccelUnits.MILLIMS2)
				return value * 9.81f;

			if (this == AccelUnits.G && unit == AccelUnits.MILLIG)
				return value * 1000f;
			if (this == AccelUnits.G && unit == AccelUnits.MS2)
				return value * 9.81f;
			if (this == AccelUnits.G && unit == AccelUnits.MILLIMS2)
				return value * 9810f;

			if (this == AccelUnits.MS2 && unit == AccelUnits.MILLIG)
				return value / 0.00981f;
			if (this == AccelUnits.MS2 && unit == AccelUnits.G)
				return value / 9.81f;
			if (this == AccelUnits.MS2 && unit == AccelUnits.MILLIMS2)
				return value * 10001f;

			if (this == AccelUnits.MILLIMS2 && unit == AccelUnits.MILLIG)
				return value / 9.81f;
			if (this == AccelUnits.MILLIMS2 && unit == AccelUnits.G)
				return value / 9810f;
			if (this == AccelUnits.MILLIMS2 && unit == AccelUnits.MS2)
				return value * 0.001f;

			return Float.NaN;
		}

		/**
		 * Converts an array of values in place to specified unit
		 * 
		 * @param values
		 *          Array of values to convert.
		 * @param unit
		 *          Unit to convert to.
		 */
		public void convertTo(float[] values, AccelUnits unit) {
			for (int i = 0; i < values.length; i++)
				values[i] = convertTo(values[i], unit);
		}

	}

	/**
	 * List of possible units for Tilt
	 */
	public enum TiltUnits {

		/**
		 * Tilt in degrees
		 */
		DEGREES,
		/**
		 * Tilt in radians
		 */
		RADIANS,
		/**
		 * Tilt in cosine
		 */
		COSINE;

		/**
		 * Converts a value to specified unit
		 * 
		 * @param value
		 *          Value to convert.
		 * @param unit
		 *          Unit to convert to.
		 * @return Converted value.
		 */
		public float convertTo(float value, TiltUnits unit) {
			if (this == unit)
				return value;
			if (this == TiltUnits.DEGREES && unit == TiltUnits.RADIANS)
				return (float) Math.toRadians(value);
			if (this == TiltUnits.RADIANS && unit == TiltUnits.DEGREES)
				return (float) Math.toDegrees(value);
			if (this == TiltUnits.RADIANS && unit == TiltUnits.COSINE)
				return (float) Math.cos(value);
			if (this == TiltUnits.DEGREES && unit == TiltUnits.COSINE)
				return (float) Math.cos(Math.toRadians(value));
			if (this == TiltUnits.COSINE && unit == TiltUnits.RADIANS)
				return (float) Math.acos(value);
			if (this == TiltUnits.COSINE && unit == TiltUnits.DEGREES)
				return (float) Math.toDegrees(Math.acos(value));
			return Float.NaN;
		}

		/**
		 * Converts an array of values in place to specified unit
		 * 
		 * @param values
		 *          Array of values to convert.
		 * @param unit
		 *          Unit to convert to.
		 */
		public void convertTo(float[] values, TiltUnits unit) {
			for (int i = 0; i < values.length; i++)
				values[i] = convertTo(values[i], unit);
		}
	}

	protected static final int ACCEL = 0x00;
	protected static final int MODE_REG = 0x16;
	protected static final int DEFAULT_I2C_ADDRESS = 0x3A;
	protected float[] offset = { 0, 0, 0 };
	protected float[] scale = { 1, 1, 1 };
	protected static int samples = 400;

	private byte[] raw = new byte[6];
	private int[] buf = new int[3];

	/**
	 * The default unit to use when retuning acceleration data from the
	 * accelerometer
	 */
	protected AccelUnits accelUnit = AccelUnits.MILLIG;
	/**
	 * The default unit to use when retuning tilt data from the accelerometer
	 */
	protected TiltUnits tiltUnit = TiltUnits.DEGREES;

	/**
	 * A factor to use when converting raw data from the accelerometer.
	 * multiplier corresponds to 1 / Number of least significant bits per unit.
	 * This number and the unit are given in the documentation of the sensor.
	 */
	protected float multiplier = 0.015625f;

	/**
	 * 
	 * @param port
	 */
	public DIMUAccel(I2CPort port) {
		super(port, DEFAULT_I2C_ADDRESS, I2CPort.HIGH_SPEED, TYPE_LOWSPEED);
		sendData(MODE_REG, (byte) 0x01);
		load();
	}
	
	/**
	 * 
	 * Calibrates a single axis. The calibration process consists of determining
	 * static acceleration values due to gravity by placing the axis opposite and
	 * in the direction of gravity. This gives a minimum and maximum value. The
	 * difference between the two is equal to 2G and used to calculate a scale
	 * factor. The mean of the two corresponds to 0G and equals the offset value.
	 * <p>
	 * Calibration settings are held in memory. To store these values one should
	 * use the <code>{@link #save}</code> method.
	 * 
	 * @param axis
	 *          Axis should have the value X,Y or Z
	 */
	
	public void calibrateAxis(char axis) {
		float max = 0;
		float min = 0;
		float minG = 0;
		float maxG = 0;
		int index = 0;
		switch (axis) {
			case 'X':
				index = 0;
				break;
			case 'Y':
				index = 1;
				break;
			case 'Z':
				index = 2;
				break;
			default:
				return;
		}
		offset[index] = 0;
		scale[index] = 1;
		
		max = getRawMean(index);
		maxG = getMeanG(index);
		
		min = getRawMean(index);
		minG = getMeanG(index);
		
		offset[index] = (min + max) / 2.f;
		scale[index] = 2.f / (maxG - minG);
	}

	/**
	 * Acceleration along 3 axis.
	 * 
	 * @return Acceleration in selected units in X,Y, Z order
	 */
	public void fetchAllAccel(float[] ret) {
		fetchAllAccel(ret, accelUnit);
	}

	/**
	 * Acceleration along 3 axis.
	 * 
	 * @param unit
	 *          of acceleration
	 * @return Acceleration in provided unit
	 */
	public void fetchAllAccel(float[] ret, AccelUnits unit) {
		fetchRawAccel(buf);
		for (int i = 0; i < 3; i++) {
			ret[i] = (buf[i] - offset[i]) * multiplier * scale[i];
		}
		AccelUnits.G.convertTo(ret, unit);
	}

	/**
	 * Tilt along 3 axis
	 * 
	 * @return Tilt in selected unit
	 */
	public void fetchAllTilt(float[] ret) {
		fetchAllTilt(ret, tiltUnit);
	}

	/**
	 * Tilt along 3 axis. Calculation of tilt is based acceleration and on the
	 * fact that under static circumstances (no acceleration) the accleration (in
	 * G) equals the cosine of the tilt angle.
	 * 
	 * @param unit
	 *          of tilt
	 * @return Tilt in provided unit
	 */
	public void fetchAllTilt(float[] ret, TiltUnits unit) {
		// acceleration in G equals the Cosine of the tilt value (under static
		// conditions).
		fetchAllAccel(ret, AccelUnits.G);
		TiltUnits.COSINE.convertTo(ret, unit);
	}

	public void fetchRawAccel(int[] ret) {
		getData(ACCEL, raw, 6);
		for (int i = 0; i < 3; i++) {
			ret[i] = ((raw[i * 2 + 1]) << 8) | (raw[2 * i] & 0xFF) & 0x03ff;
			if (ret[i] > 512)
				ret[i] -= 1024;
		}
		// ret[0]=-ret[0];
		// ret[1]=-ret[1];
	}

	/**
	 * Returns the current acceleration unit
	 * 
	 * @return accelUnit
	 */
	public AccelUnits getAccelUnit() {
		return accelUnit;
	}

	/**
	 * returns the mean of samples values of aixs indicated by the index number .
	 * 
	 * @param index
	 *          Indicates the axis (x=0,y=1,z=2).
	 * @return The mean value.
	 */
	private float getMeanG(int index) {
		float t = 0;
		float[] rawValues=new float[3];
		for (int i = 1; i <= samples; i++) {
			fetchAllAccel(rawValues, AccelUnits.G);
			t += rawValues[index];
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException ex) {
			}
		}
		return t / samples;
	}

	/**
	 * 
	 * @return A factor to use when converting raw data from the accelerometer.
	 */
	protected float getMultiplier() {
		return multiplier;
	}

	@Override
	public String getProductID() {
		return "dIMU";
	}

	/**
	 * returns the mean of N raw values of aixs indicated by the index number .
	 * 
	 * @param index
	 *          Indicates the axis (x=0,y=1,z=2).
	 * @return The mean value.
	 */
	private float getRawMean(int index) {
		int t = 0;
		int[] rawValues=new int[3];
		for (int i = 1; i <= samples; i++) {
			fetchRawAccel(rawValues);
			t += rawValues[index];
			try {
				Thread.sleep(10);
			}
			catch (InterruptedException ex) {
			}
		}
		return t / samples;
	}

	@Override
	public String getVendorID() {
		// TODO: Probably "Dexter" would be more appropriate here?
		return "MMA7455L";
	}

	/**
	 * Returns the current unit for tilt
	 * 
	 * @return
	 */
	public TiltUnits getTiltUnit() {
		return tiltUnit;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	/**
	 * Loads saved offset and scale values from memory. If no saved values are
	 * found it will load default values for offset=0 and scale =1;
	 */
	public void load() {
		File store = new File(this.getProductID());
		FileInputStream in = null;
		if (store.exists()) {
			try {
				in = new FileInputStream(store);
				DataInputStream din = new DataInputStream(in);
				for (int i = 0; i < 3; i++) {
					offset[i] = din.readFloat();
					scale[i] = din.readFloat();
				}
				din.close();
			}
			catch (IOException e) {
				System.err.println("Failed to load calibration");
			}
		}
		else {
			for (int i = 0; i < 3; i++) {
				offset[i] = 0;
				scale[i] = 1;
			}
		}
	}
	
	/**
	 * Saves the offset and scale factors in Flash memory.
	 */
	public void save() {
		File store = new File(this.getProductID());
		FileOutputStream out = null;
		if (store.exists())
			store.delete();
		try {
			out = new FileOutputStream(store);
		}
		catch (FileNotFoundException e) {
			System.err.println("Failed to save calibration");
			System.exit(1);
		}
		DataOutputStream dataOut = new DataOutputStream(out);
		try {
			for (int i = 0; i < 3; i++) {
				dataOut.writeFloat(offset[i]);
				dataOut.writeFloat(scale[i]);
			}
			out.close();
		}
		catch (IOException e) {
			System.err.println("Failed to save calibration");
			System.exit(1);
		}
	}

	/**
	 * Sets the unit for acceleration
	 * 
	 * @param accelUnit
	 */
	public void setAccelUnit(AccelUnits accelUnit) {
		this.accelUnit = accelUnit;
	}

	/**
	 * Sets the unit for tilt
	 * 
	 * @param tiltUnit
	 */
	public void setTiltUnit(TiltUnits tiltUnit) {
		this.tiltUnit = tiltUnit;
	}

	public int getXAccel() {
		float [] temp = {0, 0, 0};
		fetchAllAccel(temp, AccelUnits.MILLIMS2);
		return (int)temp[0];
	}

	public int getYAccel() {
		float [] temp = {0, 0, 0};
		fetchAllAccel(temp, AccelUnits.MILLIMS2);
		return (int)temp[1];
	}

	public int getZAccel() {
		float [] temp = {0, 0, 0};
		fetchAllAccel(temp, AccelUnits.MILLIMS2);
		return (int)temp[2];
	}
	
}
