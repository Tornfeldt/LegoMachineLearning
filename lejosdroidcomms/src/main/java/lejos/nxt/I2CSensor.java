package lejos.nxt;

import java.io.IOException;
import lejos.nxt.remote.*;
import lejos.pc.comm.*;

/**
 * A sensor wrapper to allow easy access to I2C sensors, like the ultrasonic sensor.
 * 
 * This version of this class supports remote execution of I2C.
 * 
 * @author Brian Bagnall and Lawrie Griffiths
 */
public class I2CSensor implements SensorConstants {
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
		
	private static byte STOP = 0x00; // Commands don't seem to use this?
	private static String BLANK = "       ";
	
    /**
     * Register number of sensor version string, as defined by standard Lego I2C register layout.
     * @see #getVersion() 
     */
    protected static final byte REG_VERSION = 0x00;
    /**
     * Register number of sensor vendor ID, as defined by standard Lego I2C register layout.
     * @see #getVendorID() 
     */
    protected static final byte REG_VENDOR_ID = 0x08;
    /**
     * Register number of sensor product ID, as defined by standard Lego I2C register layout.
     * @see #getProductID() 
     */
    protected static final byte REG_PRODUCT_ID = 0x10;
    
    protected static final int DEFAULT_I2C_ADDRESS = 0x02;
    
	protected byte port;
	protected int address;
	
	public I2CSensor(I2CPort port)
	{
        this(port, DEFAULT_I2C_ADDRESS, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
    }
	
	/**
	 * @param port
	 * @param mode will not work on PC side
	 */
	public I2CSensor(I2CPort port, int mode)
	{
		this(port, DEFAULT_I2C_ADDRESS, mode, TYPE_LOWSPEED);
	}
	
	/**
	 * @param port
	 * @param address
	 * @param mode will not work on PC side
	 * @param type
	 */
	public I2CSensor(I2CPort port, int address, int mode, int type)
	{
		port.setTypeAndMode(type, NXTProtocol.RAWMODE);
		this.port = (byte)port.getId();
        this.address = address;
		// Flushes out any existing data
		try {
			nxtCommand.LSGetStatus(this.port); 
			nxtCommand.LSRead(this.port); 
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
	
	public int getId() {
		return port;
	}
	
	public int getData(int register, byte [] buf, int length) {
		return this.getData(register, buf, 0, length);
	}
	
	/**
	 * Method for retrieving data values from the sensor. BYTE0 (
	 * is usually the primary data value for the sensor.
	 * Data is read from registers in the sensor, usually starting at 0x00 and ending around 0x49.
	 * Just supply the register to start reading at, and the length of bytes to read (16 maximum).
	 * NOTE: The NXT supplies UBYTE (unsigned byte) values but Java converts them into
	 * signed bytes (probably more practical to return short/int?)
	 * @param register e.g. FACTORY_SCALE_DIVISOR, BYTE0, etc....
	 * @param length Length of data to read (minimum 1, maximum 16) 
	 * @return the status
	 */
	public int getData(int register, byte [] buf, int offset, int length) {
		byte [] txData = {(byte)address, (byte) register};
		try {
			nxtCommand.LSWrite(port, txData, (byte)length);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		
		try {
			byte[] status;
			do {
				status = nxtCommand.LSGetStatus(port);
			} while(status[0] == ErrorMessages.PENDING_COMMUNICATION_TRANSACTION_IN_PROGRESS
				|| status[0] == ErrorMessages.SPECIFIED_CHANNEL_CONNECTION_NOT_CONFIGURED_OR_BUSY);
					
			byte [] ret = nxtCommand.LSRead(port);
			if (ret == null)
				return -1;
			
			System.arraycopy(ret, 0, buf, offset, ret.length);
			return 0;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}

	
	/**
	 * Helper method to return a single register byte.
	 * @param register
	 * @return the byte of data
	 */
	public int getData(int register) {
		byte [] buf1 = new byte[1];
		return getData(register, buf1 ,1);
	}
	
	/**
	 * Sets a single byte in the I2C sensor. 
	 * @param register A data register in the I2C sensor. e.g. ACTUAL_ZERO
	 * @param value The data value.
	 */
	public int sendData(int register, byte value) {
		byte [] txData = {(byte)address, (byte) register, value};
		try {
			int ret = nxtCommand.LSWrite(this.port, txData, (byte)0);
            return ret;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	public int sendData(int register, byte [] data, int length) {
		return this.sendData(register, data, 0, length);
	}
	
	/**
	 * Send data top the sensor
	 * @param register A data register in the I2C sensor.
	 * @param data The byte to send.
	 * @param length the number of bytes
	 */
	public int sendData(int register, byte [] data, int offset, int length) {
		byte [] sendData = new byte[length+2];
		sendData[0] = (byte) address;
		sendData[1] = (byte) register;
		// avoid NPE in case length==0 and data==null
		if (length > 0)
			System.arraycopy(data,offset,sendData,2,length);
		try {
			return nxtCommand.LSWrite(this.port, sendData, (byte)0);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}

    /**
     * Read the sensor's version string.
     * This method reads up to 8 bytes
     * and returns the characters before the zero termination byte.
     * Examples: "V1.0", ...
     * 
     * @return version number
     */
	public String getVersion() {
		return fetchString(REG_VERSION, 8);
	}
	
    /**
     * Read the sensor's vendor identifier.
     * This method reads up to 8 bytes
     * and returns the characters before the zero termination byte.
     * Examples: "LEGO", "HiTechnc", ...
     * 
     * @return vendor identifier
     */
	public String getVendorID() {
		return fetchString(REG_VENDOR_ID, 8);
	}
	
    /**
     * Read the sensor's product identifier.
     * This method reads up to 8 bytes
     * and returns the characters before the zero termination byte.
     * Examples: "Sonar", ...
     * 
     * @return product identifier
     */
    public String getProductID() {
        return fetchString(REG_PRODUCT_ID, 8);
    }
	
    /**
     * Read a string from the device.
     * This functions reads the specified number of bytes
     * and returns the characters before the zero termination byte.
     * 
     * @param reg
     * @param len maximum length of the string, including the zero termination byte
     * @return the string containing the characters before the zero termination byte
     */
	protected String fetchString(byte reg, int len) {
		byte[] buf = new byte[len];
		int ret = getData(reg, buf, 0, len);
		if (ret != 0)
			return "";
		
		int i;
		char[] charBuff = new char[len];		
		for (i=0; i<len && buf[i] != 0; i++)
			charBuff[i] = (char)(buf[i] & 0xFF);
		
		return new String(charBuff, 0, i);
	}

    /**
     * Set the address of the port
     * Addresses use the standard Lego/NXT format and are in the range 0x2-0xfe.
     * The low bit must always be zero. Some data sheets (and older versions
     * of leJOS) may use i2c 7 bit format (0x1-0x7f) in which case this address
     * must be shifted left one place to be used with this function.
     * 
     * @param addr 0x02 to 0xfe
     * @deprecated If the device has a changeable address, then constructor of the class should have an address parameter. If not, please report a bug.
     */
	public void setAddress(int addr) {
        if ((address & 1) != 0) throw new IllegalArgumentException("Bad address format");
		address = addr;
	}

	/**
     * Return the the I2C address of the sensor.
     * The sensor uses the address for writing/reading.
     * @return the I2C address.
     */
    public int getAddress()
    {
        return this.address;
    }
}