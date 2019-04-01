package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.Gyroscope;

/**
 * <p>This class provides access to the gyro of Dexter Industries IMU sensor.
 * Rate data can be fetched using the fetchAllRate method.
 * Temperature data can be fetched using the fetchTemperature method.</p>
 * 
 * <p><b>Orientation of Axes:</b> Assuming the flat side of the dIMU sensor is facing you and the sensor port
 * is at the bottom, the axes are as follows: X axis runs from top to bottom, Y axis runs left to right, Z axis
 * comes directly out the front of the sensor board.</p>
 * 
 * 
 * @author Aswin Bouwmeester
 * @version 1.0
 */
public class DIMUGyro extends I2CSensor {

	/**
	 * Dynamic ranges supported by the sensor
	 */
	public enum Range {
		_250DPS((byte) 0x00, 8.75f), _500DPS((byte) 0x10, 17.5f), _2000DPS((byte) 0x20, 70f);
		private final byte	code;
		private final float	multiplier;

		Range(byte code, float multiplier) {
			this.code = code;
			this.multiplier = multiplier;
		}

		public byte getCode() {
			return code;
		}

		public float getMultiplier() {
			return multiplier;
		}

	}

	/**
	 * Rotation units supported by the sensor
	 */
	public enum RateUnits {

		/**
		 * Rate in degrees/s
		 */
		DPS,
		/**
		 * Rate in radians/s
		 */
		RPS,
		/**
		 * Rate in cosine/s
		 */
		CPS;

		/**
		 * Converts a value to specified unit
		 * 
		 * @param value
		 *          Value to convert.
		 * @param unit
		 *          Unit to convert to.
		 * @return Converted value.
		 */
		public float convertTo(float value, RateUnits unit) {
			if (this == unit)
				return value;
			if (this == RateUnits.DPS && unit == RateUnits.RPS)
				return (float) Math.toRadians(value);
			if (this == RateUnits.RPS && unit == RateUnits.DPS)
				return (float) Math.toDegrees(value);
			if (this == RateUnits.RPS && unit == RateUnits.CPS)
				return (float) Math.cos(value);
			if (this == RateUnits.DPS && unit == RateUnits.CPS)
				return (float) Math.cos(Math.toRadians(value));
			if (this == RateUnits.CPS && unit == RateUnits.RPS)
				return (float) Math.acos(value);
			if (this == RateUnits.CPS && unit == RateUnits.DPS)
				return (float) Math.acos(Math.toRadians(value));
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
		public void convertTo(float[] values, RateUnits unit) {
			for (int i = 0; i < values.length; i++)
				values[i] = convertTo(values[i], unit);
		}

	}

	/**
	 * Internal sample rates supported by the sensor
	 */
	public enum SampleRate {

		_100Hz((byte) 0x00), _200Hz((byte) 0x40), _400Hz((byte) 0x80), _800Hz((byte) 0xC0);

		private final byte	code;

		SampleRate(byte code) {
			this.code = code;
		}

		public byte getCode() {
			return code;
		}
	}

	/**
	 * Temperature units supported by the sensor
	 */	
	public enum TemperatureUnits {

		/**
		 * Rate in degrees
		 */
		CELCIUS,
		/**
		 * Rate in radians
		 */
		FAHRENHEIT;

		/**
		 * Converts a value to specified unit
		 * 
		 * @param value
		 *          Value to convert.
		 * @param unit
		 *          Unit to convert to.
		 * @return Converted value.
		 */
		public float convertTo(float value, TemperatureUnits unit) {
			if (this == unit)
				return value;
			if (this == TemperatureUnits.CELCIUS && unit == TemperatureUnits.FAHRENHEIT)
				return 1.8f * value + 32f;
			if (this == TemperatureUnits.FAHRENHEIT && unit == TemperatureUnits.CELCIUS)
				return (value - 32f) / 1.8f;
			return Float.NaN;
		}

	}

	/**
	 * Axis units supported by the sensor. Used with the {@link lejos.nxt.addon.DIMUGyro#getAxis(Axis)} method.
	 * @author BB
	 *
	 */
	public enum Axis {
		X, Y, Z;
	}
	
	// Used by the getAxis() method:
	private Gyroscope x_gyroscope = null;
	private Gyroscope y_gyroscope = null;
	private Gyroscope z_gyroscope = null;
		
	private static int CTRL_REG1 = 0x020;
	private static int CTRL_REG2 = 0x021;
	private static int CTRL_REG3 = 0x022;
	private static int CTRL_REG4 = 0x023;
	private static int CTRL_REG5 = 0x024;
	private static int REG_RATE = 0x28 | 0x80;
	private static int SAMPLESIZE = 400;
	private static int REG_TEMP = 0x26;
	private static int REG_STATUS = 0x27;
	private static int address = 0xD2;

	protected float I = 0.0001f;
	protected boolean[] dynamicOffset = { false, false, false };
	protected float[] offset = { 0, 0, 0 };

	protected Range range = Range._500DPS;
	protected SampleRate sampleRate = SampleRate._800Hz;

	protected TemperatureUnits temperatureUnit = TemperatureUnits.CELCIUS;
	protected RateUnits rateUnit = RateUnits.DPS;

	byte[] buf = new byte[16];
	int[] raw = new int[16];
	float[] t = new float[3];
	float multiplier;

	public DIMUGyro(I2CPort port) {
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
		init();
	}

	/**
	 * This method returns an instance of one of the three axes (X, Y, or Z) that the dIMU can supply.
	 * @param axis The axis (X, Y or Z) to retrieve Gyroscope object
	 * @return A Gyroscope object representing the axis you requested. 
	 */
	public Gyroscope getAxis(Axis axis) {
		switch(axis) {
		case X:
			if(x_gyroscope == null) x_gyroscope = new GyroAxis(axis);
			return 	x_gyroscope;
		case Y:
			if(y_gyroscope == null) y_gyroscope = new GyroAxis(axis);
			return 	y_gyroscope;
		case Z:
			if(z_gyroscope == null) z_gyroscope = new GyroAxis(axis);
			return 	z_gyroscope;
		}
		return null; // actually impossible to reach this line of code
	}
	
	private class GyroAxis implements Gyroscope {
		
		private int axis_index;
		
		private GyroAxis(Axis axis) {
			switch(axis) {
			case X:
				axis_index = 0;
				break;
			case Y:
				axis_index = 1;
				break;
			case Z:
				axis_index = 2;
				break;
			}
		}
		
		public float getAngularVelocity() {
			// TODO: Fancy code to recycle values in temp if another axis requested within xx ms? (xx=I2C time to retrieve data) 
			float[] temp = {0, 0, 0};
			fetchAllRate(temp, RateUnits.DPS);
			return temp[axis_index];
		}

		public void recalibrateOffset() {
			// TODO: Way to calibrate only the one axis? 
			calculateOffset();
		}
	}
	
	/**
	 * Calibrates the sensor 
	 */
	public void calculateOffset() {
		// Allow the sensor to settle in
		for (int s = 1; s <= 25; s++) {
			getRawRate(raw);
			while(!isNewDataAvailable());
		}
		for (int i = 0; i < 3; i++)
			offset[i] = 0;
		for (int s = 1; s <= SAMPLESIZE; s++) {
			getRawRate(raw);
			for (int i = 0; i < 3; i++)
				offset[i] += raw[i];
		while(!isNewDataAvailable());
		}
		for (int i = 0; i < 3; i++)
			offset[i] /= SAMPLESIZE;
	}
	
	public void fetchAllRate(float[] ret) {
		fetchAllRate(ret, rateUnit);

	}

	public void fetchAllRate(float[] ret, RateUnits unit) {
		getRawRate(raw);
		for (int i = 0; i < 3; i++) {
			ret[i] = (raw[i] - offset[i]) * multiplier;
		}
		RateUnits.DPS.convertTo(ret, unit);
	}

	/**
	 * Temperature is not calibrated.
	 *
	 * @return temperature in degrees Celsius
	 */
	public float fetchTemperature() {
		float temp = 0;
		getData(REG_TEMP, buf, 1);
		temp = 50.0f - buf[0];
		if (temperatureUnit == TemperatureUnits.FAHRENHEIT)
			temp = 1.8f * temp + 32;
		return temp;
	}

	public boolean[] getDynamicOffset() {
		return dynamicOffset.clone();
	}

	public float getOffsetCorrectionSpeed() {
		return I;
	}

	@Override
	public String getProductID() {
		return "dIMU";
	}
	
	public final Range getRange() {
		return range;
	}

	public RateUnits getRateUnit() {
		return rateUnit;
	}

	public void getRawRate(int[] ret) {
		while (!isNewDataAvailable())
			Thread.yield();
		getData(REG_RATE, buf, 6);
		// if (isDataOverrun()) Sound.beep();
		// The Dexter IMU has swapped the x and y axis of the gyro;
		ret[0] = -((buf[3]) << 8) | (buf[2] & 0xFF);
		ret[1] = ((buf[1]) << 8) | (buf[0] & 0xFF);
		ret[2] = ((buf[5]) << 8) | (buf[4] & 0xFF);
		for (int i = 0; i < 3; i++) {
			if (dynamicOffset[i])
				offset[i] = (1 - I) * offset[i] + I * ret[i];
		}
	}

	public final SampleRate getSampleRate() {
		return sampleRate;

	}

	@Override
	public String getVendorID() {
		// TODO: Probably "Dexter" would be more appropriate here?
		return "L3G4200D";
	}

	private byte getStatus() {
		getData(REG_STATUS, buf, 1);
		return buf[0];
	}

	public TemperatureUnits getTemperatureUnit() {
		return temperatureUnit;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	/**
	 * configures the sensor. Method is called whenever one of the sensor settings
	 * changes
	 */
	private void init() {
		int reg;
		// put in sleep mode;
		sendData(CTRL_REG1, (byte) 0x08);
		// oHigh-pass cut off 1 Hz;
		sendData(CTRL_REG2, (byte) 0x00);
		// no interrupts, no fifo
		sendData(CTRL_REG3, (byte) 0x08);
		// set range
		reg = range.getCode() | 0x80;
		multiplier = range.getMultiplier() / 1000f;
		sendData(CTRL_REG4, (byte) reg);
		// disable fifo and high pass
		sendData(CTRL_REG5, (byte) 0x00);
		// stabilize output signal;
		// enable all axis, set output data rate ;
		reg = sampleRate.getCode() | 0x3F;
		// set sample rate, wake up
		sendData(CTRL_REG1, (byte) reg);
		// Allow sensor to settle in
		for (int s = 1; s <= 15; s++) {
			while (!isNewDataAvailable())
				Thread.yield();
			getRawRate(raw);
		}
	}

	protected boolean isDataOverrun() {
		return (getStatus() & 0x80) == 0x80;
	}

	/**
	 * Returns true if new data is available from the sensor
	 */
	public boolean isNewDataAvailable() {
		return (getStatus() & 0x08) == 0x08;
	}
	
	/**
	 * Enables or disables the dynamic offset correction mechanism of the sensor
	 * Dynamic offset correction assumes that in the long run the sensor keeps its
	 * orientation. It updates the offset using a I-controller. The update speed
	 * (I-factor) is controlled by setOffsetCorrectionSpeed().
	 * 
	 * <P>
	 * It is advised not to use dynamic offset for this sensor.
	 * 
	 * @param set
	 *          an 3-element array of booleans. True enables Dynamic Offset
	 *          Correction, false disables it. Order of elements is X, Y, Z.
	 */
	public void setDynamicOffset(boolean[] set) {
		dynamicOffset = set.clone();
	}

	public void setOffsetCorrectionSpeed(float i) {
		I = i;
	}

	public void setRange(Range range) {
		this.range = range;
		init();
	}

	public void setRateUnit(RateUnits unit) {
		this.rateUnit = unit;
	}

	public void setSampleRate(SampleRate rate) {
		sampleRate = rate;
		init();
	}

	public void setTemperatureUnit(TemperatureUnits unit) {
		temperatureUnit = unit;

	}
}
