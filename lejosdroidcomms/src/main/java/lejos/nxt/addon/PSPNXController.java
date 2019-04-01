package lejos.nxt.addon;

import lejos.nxt.SensorPort;
import lejos.nxt.I2CSensor;
import lejos.nxt.I2CPort;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * This class allows you to use a Sony Playstation 2 controller to
 * control your robot in conjunction with the Mindsensors.com
 * PSP-Nx interface. The controller has 2 analog joysticks and
 * 16 buttons. See www.mindsensors.com
 *
 */
/*
 * DEV NOTES To Do:
 * - Add listened interface?
 *
 */
public class PSPNXController extends I2CSensor {
	/* Send command */
	private static final byte MODE = 0x41;

	/* Mode Commands */
	private static final byte ENERGIZED = 0x45; // Power on
	private static final byte DE_ENERGIZED = 0x44; // Power off
	private static final byte SET_DIGITAL_MODE = 0x41;
	private static final byte SET_ANALOG_MODE = 0x73;
	private static final byte SET_ADPA_MODE_ON = 0x4E;
	private static final byte SET_ADPA_MODE_OFF = 0x4F;

	/* Device Registers */
	/**
	 * BUTTON_1 and _2 combine to provide status for 16 buttons
	 * (8 button states per byte)
	 */
	private static final byte BUTTON_1 = 0x42;
	private static final byte BUTTON_2 = 0x43;
	private static final byte X_LEFT_JOYSTICK = 0x44;
	private static final byte Y_LEFT_JOYSTICK = 0x45;
	private static final byte X_RIGHT_JOYSTICK = 0x46;
	private static final byte Y_RIGHT_JOYSTICK = 0x47;

	private byte[] buf = new byte[2];

	public PSPNXController(SensorPort port)	{
        this(port, DEFAULT_I2C_ADDRESS);
    }

	public PSPNXController(SensorPort port, int address)	{
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
		// Set correct sensor type, default is TYPE_LOWSPEED
		// port.setType(TYPE_LOWSPEED_9V);

		// Set proper mode (power on, etc..):
		powerUp(true);
		setDigitalMode(true);
	}

	/*
	 * Set the sensor into the specified mode. Keep track of which mode we are
	 * operating in. Make a note of when any distance data will become available
	 *
	 */
	private int setMode(byte mode)	{
		buf[0] = mode;
		int ret = sendData(MODE, buf, 1);
		return ret;
	}

	public int powerUp(boolean activate) {
		if (activate)
			return setMode(ENERGIZED);
		else
			return setMode(DE_ENERGIZED);
	}

	/**
	 * Each bit in the short byte represents the boolean (pressed or
	 * not pressed) of a button.
	 * @return Data for all 16 buttons as a int array
	 */
	public int[] getButtons() {
		int Buttons[] = new int[16];

		if (getData(BUTTON_1, buf,2) == 0) {
            Buttons[0] = ((buf[0] >> 7) & 0x01) > 0 ? 0 : 1;
            Buttons[1] = ((buf[0] >> 6) & 0x01) > 0 ? 0 : 1;
            Buttons[2] = ((buf[0] >> 5) & 0x01) > 0 ? 0 : 1;
            Buttons[3] = ((buf[0] >> 4) & 0x01) > 0 ? 0 : 1;
            Buttons[4] = ((buf[0] >> 3) & 0x01) > 0 ? 0 : 1;
            Buttons[5] = ((buf[0] >> 2) & 0x01) > 0 ? 0 : 1;

            Buttons[6] = ((buf[0] >> 1) & 0x01) > 0 ? 0 : 1;
            Buttons[7] = ((buf[0] >> 0) & 0x01) > 0 ? 0 : 1;

            Buttons[8] = ((buf[1] >> 7) & 0x01) > 0 ? 0 : 1;
            Buttons[9] = ((buf[1] >> 6) & 0x01) > 0 ? 0 : 1;
            Buttons[10] = ((buf[1] >> 5) & 0x01) > 0 ? 0 : 1;
            Buttons[11] = ((buf[1] >> 4) & 0x01) > 0 ? 0 : 1;
            Buttons[12] = ((buf[1] >> 3) & 0x01) > 0 ? 0 : 1;
            Buttons[13] = ((buf[1] >> 2) & 0x01) > 0 ? 0 : 1;
            Buttons[14] = ((buf[1] >> 1) & 0x01) > 0 ? 0 : 1;
            Buttons[15] = ((buf[1] >> 0) & 0x01) > 0 ? 0 : 1;
        }

		return Buttons;
    }

	public int setDigitalMode(boolean activate) {
		if(activate)
			return setMode(SET_DIGITAL_MODE);
		else
			return setMode(SET_ANALOG_MODE);
	}

	public int getLeftX() {
		int ret = getData(X_LEFT_JOYSTICK, buf,1);
		return (ret == 0 ? ((buf[0] & 0xFF) - 128) * 100 / 128 : -1);
	}

	public int getLeftY() {
		int ret = getData(Y_LEFT_JOYSTICK, buf,1);
		return (ret == 0 ? ((buf[0] & 0xFF) - 128) * 100 / 128 : -1);
	}

	public int getRightX() {
		int ret = getData(X_RIGHT_JOYSTICK, buf,1);
		return (ret == 0 ? ((buf[0] & 0xFF) - 128) * 100 / 128 : -1);
	}

	public int getRightY() {
		int ret = getData(Y_RIGHT_JOYSTICK, buf,1);
		return (ret == 0 ? ((buf[0] & 0xFF) - 128) * 100 / 128 : -1);
	}

	/**
	 * Returns the current operating mode of the sensor.
	 * (put list of possible return values here:)
	 *
	 * @return -1 if error otherwise the operating mode
	 */
	public byte getMode() {
		int ret = getData(MODE, buf,1);
		return (ret == 0 ? buf[0] : -1);
	}

	/**
	 * Use ADPA mode only if you are trying to connect more
	 * than one I2C sensor to a single port.
	 * @param activate
	 * @return the status value
	 */
	public int setADPAMode(boolean activate) {
		/*
		 * DEVELOPER NOTES: If all I2C sensors use the same
		 * adpa mode address, this method could be incorporated into
		 * the I2CSensor class instead.
		 */
		if(activate)
			return setMode(SET_ADPA_MODE_ON);
		else
			return setMode(SET_ADPA_MODE_OFF);
	}
}