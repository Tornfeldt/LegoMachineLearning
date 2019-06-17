package lejos.nxt.addon;

import lejos.nxt.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This class manages the sensor NXT Line Leader from Mindsensors. The sensor
 * add a sensor row to detect black/white lines.
 * 
 * This sensor is perfect to build a robot which has the mission to follow a
 * line.
 * 
 * @author Juan Antonio Brenha Moral
 * @author Eric Pascual (EP)
 */
public class NXTLineLeader extends I2CSensor {
	private byte[] buf = new byte[8];

	private final static byte COMMAND = 0x41;

	private final static byte LL_SETPOINT = 0x45;
	private final static byte LL_KP = 0x46;
	private final static byte LL_KI = 0X47;
	private final static byte LL_KD = 0X48;
	private final static byte LL_KP_DIVISOR = 0X61;
	private final static byte LL_KI_DIVISOR = 0X62;
	private final static byte LL_KD_DIVISOR = 0X63;

	private final static byte LL_READ_STEERING = 0x42;
	private final static byte LL_READ_AVERAGE = 0X43;
	private final static byte LL_READ_RESULT = 0X44;

	private final static byte SENSOR_ID_MIN = 1;
	private final static byte SENSOR_ID_MAX = 8;
	private final static byte LL_CAL_SENSOR_READING_BASE = 0x49;

	public enum Command {
		CALIBRATE_WHITE('W'),
		CALIBRATE_BLACK('B'),
		SLEEP('D'),
		WAKEUP('P'),
		INVERT_COLORS('I'),
		RESET_COLORS('R'),
		SNAPSHOT('S'),
		FREQ_60HZ('A'),
		FREQ_50HZ('E'),
		FREQ_UNIVERSAL('U');

		private Command(char value) {
			this.value = value;
		}

		final char value;
	}

	/**
	 * Color selector for white reading limit
	 * <p>
	 * See {@link #getReadingLimit(int, LineColor)} and
	 * {@link #getCalibrationData(int, LineColor)} for details.
	 */
	public enum LineColor {
		BLACK(0x59, 0x6c, Command.CALIBRATE_BLACK),
		WHITE(0x51, 0x64, Command.CALIBRATE_WHITE);

		private LineColor(int readingLimit, int calibrationData, Command calibrationCommand) {
			this.readingLimit = (byte) readingLimit;
			this.calibrationData = (byte) calibrationData;
			this.calibrationCommand = calibrationCommand;
		}

		final byte readingLimit;
		final byte calibrationData;
		final Command calibrationCommand;
	};

	/**
	 * Constructor
	 *
     * @param port
     * @param address I2C address for the device
	 */
	public NXTLineLeader(I2CPort port, int address) {
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED_9V);
	}

	/**
	 * Constructor
	 *
	 * @param port
	 */
	public NXTLineLeader(I2CPort port) {
		this(port, DEFAULT_I2C_ADDRESS);
	}

	/**
	 * Send a single byte command represented by a letter
	 * 
	 * @param cmd
	 *            the command to be sent
	 */
	public void sendCommand(Command cmd) {
		sendData(COMMAND, (byte) cmd.value);
	}

	/**
	 * Original version of sendCOmmand, left for compatibility with legacy code.
	 * <p>
	 * This version has no checking of the validity of the passed command, so it
	 * is advised to use {@link #sendCommand(Command)} instead.
	 * 
	 * @param cmd
	 *            the character based command
	 * @deprecated use {@link #sendCommand(Command)} instead
	 */
	@Deprecated
	public void sendCommand(char cmd) {
		sendData(COMMAND, (byte) cmd);
	}

	/**
	 * Sleep the sensor
	 */
	public void sleep() {
		this.sendCommand(Command.SLEEP);
	}

	/**
	 * Wake up the sensor
	 * 
	 */
	public void wakeUp() {
		this.sendCommand(Command.WAKEUP);
	}

	/**
	 * Calibrate the white and black levels.
	 * 
	 * @param color
	 *            color selector
	 */
	public void calibrate(LineColor color) {
		sendCommand(color.calibrationCommand);
	}

	/**
	 * Get the steering value
	 * <p>
	 * Steering is a signed value in the range [-100,+100] representing the
	 * amount of power to be added/subtracted to motors power setting. In case
	 * we cannot read the sensor, Integer.MIN_VALUE is returned to notify the
	 * anomaly.
	 */
	public int getSteering() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_READ_STEERING, buf, 1);
		int steering = 0;
		// [EP] 17-Jan-10:
		// steering is signed => no mask should be applied
		// In addition negative values are a valid result, so we cannot use -1
		// as an error notification
		steering = (ret == 0) ? buf[0] : Integer.MIN_VALUE;

		return steering;
	}

	/**
	 * Get the average value
	 */
	public int getAverage() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_READ_AVERAGE, buf, 1);
		int average = 0;
		average = (ret == 0 ? (buf[0] & 0xff) : -1);

		return average;
	}

	/**
	 * Get result value
	 */
	public int getResult() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_READ_RESULT, buf, 1);
		int result = 0;
		result = (ret == 0 ? (buf[0] & 0xff) : -1);
		return result;
	}

	/**
	 * Get the set point of the PID.
	 */
	public int getSetPoint() {
		int ret = getData(LL_SETPOINT, buf, 1);
		return (ret == 0 ? (buf[0] & 0xff) : -1);
	}

	/**
	 * Set the set point of the PID
	 */
	public void setSetPoint(int value) {
		sendData(LL_SETPOINT, (byte) value);
	}

	/**
	 * Get KP value
	 */
	public int getKP() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_KP, buf, 1);
		int KP = 0;
		KP = (ret == 0 ? (buf[0] & 0xff) : -1);
		return KP;
	}

	/**
	 * Set KP value
	 */
	public void setKP(int KP) {
		sendData(LL_KP, (byte) KP);
	}

	/**
	 * Get KP divisor
	 */
	public int getKPDivisor() {
		int ret = getData(LL_KP_DIVISOR, buf, 1);
		return (ret == 0 ? (buf[0] & 0xff) : -1);
	}

	/**
	 * Set the KP divisor
	 */
	public void setKPDivisor(int value) {
		sendData(LL_KP_DIVISOR, (byte) value);
	}

	/**
	 * Get KI value
	 */
	public int getKI() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_KI, buf, 1);
		int KI = 0;
		KI = (ret == 0 ? (buf[0] & 0xff) : -1);
		return KI;
	}

	/**
	 * Set KI value
	 */
	public void setKI(int KI) {
		sendData(LL_KI, (byte) KI);
	}

	/**
	 * Get KI divisor
	 */
	public int getKIDivisor() {
		int ret = getData(LL_KI_DIVISOR, buf, 1);
		return (ret == 0 ? (buf[0] & 0xff) : -1);
	}

	/**
	 * Set the KI divisor
	 */
	public void setKIDivisor(int value) {
		sendData(LL_KI_DIVISOR, (byte) value);
	}

	/**
	 * Get KD value
	 */
	public int getKD() {
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LL_KD, buf, 1);
		int KD = 0;
		KD = (ret == 0 ? (buf[0] & 0xff) : -1);
		return KD;
	}

	/**
	 * Set KD value
	 */
	public void setKD(int KD) {
		sendData(LL_KD, (byte) KD);
	}

	/**
	 * Get KD divisor
	 */
	public int getKDDivisor() {
		int ret = getData(LL_KD_DIVISOR, buf, 1);
		return (ret == 0 ? (buf[0] & 0xff) : -1);
	}

	/**
	 * Set the KD divisor
	 */
	public void setKDDivisor(int value) {
		sendData(LL_KD_DIVISOR, (byte) value);
	}

	private void checkSensorId(int index) {
		if ((index < SENSOR_ID_MIN) || (index > SENSOR_ID_MAX))
			throw new IllegalArgumentException("out of bounds sensor id");
	}

	/**
	 * Get status from each sensor in the raw
	 * 
	 * @deprecated
	 */
	@Deprecated
	public int getSensorStatus(int index) {
		checkSensorId(index);
		// [EP] 17-Jan-10:
		// changed byte count to 1 since register is a single byte one
		int ret = getData(LineColor.WHITE.readingLimit + index - 1, buf, 1);
		int status = 0;
		status = (ret == 0 ? (buf[0] & 0xff) : -1);
		return status;
	}

	/**
	 * Get the calibrated reading of a given sensor
	 * 
	 * @param index
	 *            sensor index (must be in range [1..8])
	 * @return sensor reading (in range [0..100]), or -1 if reading error
	 * @throws IllegalArgumentException
	 *             if index not in range
	 */
	public int getCalibratedSensorReading(int index) {
		checkSensorId(index);
		int ret = getData(LL_CAL_SENSOR_READING_BASE + index - 1, buf, 1);
		return (ret == 0) ? buf[0] & 0xff : -1;
	}

	/**
	 * Get the calibration value for white and black colors for a given sensor
	 * 
	 * @param index
	 *            sensor index (must be in range [1..8])
	 * @param color
	 *            color selector
	 * @return calibration value (in range [0..255]), or -1 if reading error
	 * @throws IllegalArgumentException
	 *             if index not in range
	 */
	public int getReadingLimit(int index, LineColor color) {
		checkSensorId(index);
		int ret = getData(color.readingLimit + index - 1, buf, 1);
		return (ret == 0) ? buf[0] & 0xff : -1;
	}

	/**
	 * Get the calibration data for white and black colors for a given sensor
	 * 
	 * @param index
	 *            sensor index (must be in range [1..8])
	 * @param color
	 *            color selector
	 * @return calibration data (in range [0..255]), or -1 if reading error
	 * @throws IllegalArgumentException
	 *             if index not in range
	 */
	public int getCalibrationData(int index, LineColor color) {
		checkSensorId(index);
		int ret = getData(color.calibrationData + index - 1, buf, 1);
		return (ret == 0) ? buf[0] & 0xff : -1;
	}
}
