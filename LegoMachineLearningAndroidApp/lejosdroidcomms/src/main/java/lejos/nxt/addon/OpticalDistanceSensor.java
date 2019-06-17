package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.robotics.RangeFinder;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports Mindsensors DIST-Nx series of Optical Distance Sensor.<br>
 * This sensor is used for greater precision than the Ultrasonic Sensor.<br>
 * 
 * @author Michael Smith <mdsmitty@gmail.com>
 * <br><br>Lum, Many thanks for helping me test this class.
 * 
 */

public class OpticalDistanceSensor extends I2CSensor implements RangeFinder{
	private byte[] buf = new byte[2];
	//Registers
	private final static int COMMAND = 0x41;
	private final static int DIST_DATA_LSB = 0x42;
	private final static int DIST_DATA_MSB = 0x43;
	private final static int VOLT_DATA_LSB = 0x44;
	private final static int VOLT_DATA_MSB = 0x45;
	private final static int SENSOR_MOD_TYPE = 0x50;
	private final static int CURVE = 0x51;
	private final static int DIST_MIN_DATA_LSB = 0x52;
	private final static int DIST_MIN_DATA_MSB = 0x53;
	private final static int DIST_MAX_DATA_LSB = 0x54;
	private final static int DIST_MAX_DATA_MSB = 0x55;
	private final static int VOLT_DATA_POINT_LSB = 0x52;
	private final static int VOLT_DATA_POINT_MSB = 0x53;
	private final static int DIST_DATA_POINT_LSB = 0x54;
	private final static int DIST_DATA_POINT_MSB = 0x55;
	
	//Sensor Modules
	public final static byte GP2D12 = 0x31;
	
	/**
	 * DIST-Nx-Short
	 */
	public final static byte GP2D120 = 0x32;
	
	/**
	 * DIST-Nx-Medium
	 */
	public final static byte GP2YA21 = 0x33;
	
	/**
	 * DIST-Nx-Long
	 */
	public final static byte GP2YA02 = 0x34;
	
	/**
	 * Custom sensor
	 */
	public final static byte CUSTOM = 0x35;
	
	//Commands
	private final static byte DE_ENERGIZED = 0x44;
	private final static byte ENERGIZED = 0x45;
	private final static byte ARPA_ON = 0x4E;
	private final static byte ARPA_OFF = 0x4F; //(default)
	
	/**
	 *
	 * @param port NXT sensor port 1-4
	 */
	public OpticalDistanceSensor(I2CPort port){
		this(port, DEFAULT_I2C_ADDRESS);
	}

	/**
	 *
     * @param port NXT sensor port 1-4
     * @param address I2C address for the sensor
	 */
	public OpticalDistanceSensor(I2CPort port, int address){
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
		powerOn();
	}

	/**
	 * This only needs the be run if you are changing the sensor.
	 * @param module changes the sensor module attached the he board.
	 * 
	 */
	public void setSensorModule(byte module){
		sendData(COMMAND, module);
	}
	
	/**
	 * Returns the distance from the object in millimeters.
	 * This returns the same value as getDistLSB.
	 * @return int 
	 */
	public int getDistance(){
		return getDistLSB();
	}
	
	/**
	 * Returns the range to the object in centimeters.
	 * 
	 * @return the range as a float
	 */
	public float getRange(){
		return getDistLSB() / 10.0F;
	}
	
	/**
	 * Turns the sensor module on.  <br>
	 * Power is turned on by the constructor method.
	 *
	 */
	public void powerOn(){
		sendData(COMMAND, ENERGIZED);
	}
	
	/**
	 * Turns power to the sensor module off.
	 *
	 */
	public void powerOff(){
		sendData(COMMAND, DE_ENERGIZED);
	}
	
	/**
	 * Enables (ADPA) Auto Detecting Parallel Architecture. <br>
	 * Once you have enabled it you don't have to enable again.
	 *
	 */
	public void setAPDAOn() {
		sendData(COMMAND, ARPA_ON);
	}
	
	/**
	 * Disables (ADPA) Auto Detecting Parallel Architecture.<br>
	 * Disabled by default.
	 *
	 */
	public void setAPDAOff() {
		sendData(COMMAND, ARPA_OFF);
	}
	
	/**
	 * Returns the current distance in millimeters for the LSB.
	 * @return int
	 */
	public int getDistLSB(){
		return readDISTNX(DIST_DATA_LSB, 2);
	}
	
	/**
	 * Returns the current distance in millimeters for MSB.
	 * @return int
	 */
	public int getDistMSB(){
		return readDISTNX(DIST_DATA_MSB, 2);
	}
	
	/**
	 * Returns the current voltage level in millivolts for the LSB. 
	 * @return int
	 */
	public int getVoltLSB(){
		return readDISTNX(VOLT_DATA_LSB, 2);
	}
	
	/**
	 * Returns the current voltage level in millivolts for the MSB. 
	 * @return int
	 */
	public int getVoltMSB(){
		return readDISTNX(VOLT_DATA_MSB, 2);
	}
	
	/**
	 * Used to determine the sensor module that is configured. 
	 * This can be helpful if the sensor is not working properly.
	 * @return int
	 */
	public int getSensorModule(){
		return readDISTNX(SENSOR_MOD_TYPE, 1);
	}
	
	/**
	 * Gets the number of points that will be in the curve.
	 *  This corresponds with the set/get Volt and Distance methods.<br>
	 * Used for recalibrating the sensor. 
	 * @return int
	 */
	public int getCurveCount(){
		return readDISTNX(CURVE, 1);
	}
	
	/**
	 * Sets the number of points that will be in the configured curve. 
	 * This corresponds with the set/get Vold and Distance points methods.<br>
	 * Used for recalibrating the sensor. 
	 * @param value max 39
	 */
	public void setCurveCount(int value){
		sendData(CURVE, (byte)value);
	}	
	
	/**
	 * Gets the min value in millimeters for the LSB.<br>
	 * Used for recalibrating the sensor. 
	 * @return int
	 */
	public int getDistMinLSB(){
		return readDISTNX(DIST_MIN_DATA_LSB, 2);
	}
	
	/**
	 * Sets the min value in millimeters for the LSB.<br>
	 * Used for recalibrating the sensor.
	 * @param value int
	 */
	public void setDistMinLSB(int value){
		writeDISTNX(DIST_MIN_DATA_LSB, (byte)value);
	}
	
	/**
	 * Gets the min value in millimeters for the MSB.<br>
	 * Used for recalibrating the sensor. 
	 * @return int
	 */
	public int getDistMinMSB(){
		return readDISTNX(DIST_MIN_DATA_MSB, 2);
	}
	
	/**
	 * Sets the min value in millimeters for the MSB.<br>
	 * Used for recalibrating the sensor. 
	 * @param value int
	 */
	public void setDistMinMSB(int value){
		writeDISTNX(DIST_MIN_DATA_MSB, (byte)value);
	}
	
	/**
	 * Gets the max value in millimeters for the LSB.<br>
	 * Used for recalibrating the sensor. 
	 * @return int
	 */
	public int getDistMaxLSB(){
		return readDISTNX(DIST_MAX_DATA_LSB, 2);
	}
	
	/**
	 * Sets the max value in millimeters for LSB.<br>
	 * Used for recalibrating the sensor. 
	 * @param value int
	 */
	public void setDistMaxLSB(int value){
		writeDISTNX(DIST_MAX_DATA_LSB, (byte)value);
	}
	
	/**
	 * Gets the max value in millimeters for the MSB.<br>
	 * Used for recalibrating the sensor. 
	 * @return int
	 */	
	public int getDistMaxMSB(){
		return readDISTNX(DIST_MAX_DATA_MSB, 2);
	}
	
	/**
	 * Sets the max value in millimeters for the MSB.<br>
	 * Used for recalibrating the sensor. 
	 * @param value int
	 */
	public void setDistMaxMSB(int value){
		writeDISTNX(DIST_MAX_DATA_MSB, (byte)value);
	}
	
	/**
	 * Gets millivolts value of the specific index for the LSB. 
	 * These will correspond with the point methods index value.<br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @return int
	 */
	public int getVoltPointLSB(int index){
		if(index == 0) index = 1;
		index = VOLT_DATA_POINT_LSB + 4 * index;
		return readDISTNX(index, 2);
	}
	
	/**
	 * Sets millivolts value of the specific index for the LSB. 
	 * These will correspond with the point methods index value.<br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @param value int
	 */
	public void setVoltPointLSB(int index, int value){
		if(index == 0) index = 1;
		index = VOLT_DATA_POINT_LSB + 4 * index;
		sendData(index, (byte)value);
	}
	
	/**
	 * Gets millivolts value of the specific index for the MSB.
	 *  These will correspond with the point methods index value.<br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @return Returns int
	 */
	public int getVoltPointMSB(int index){
		if(index == 0) index = 1;
		index = VOLT_DATA_POINT_MSB + 4 * index;
		return readDISTNX(index, 2);
	}
	
	/**
	 * Sets millivolts value of the specific index for the MSB. 
	 * These will correspond with the point methods index value.<br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @param value int
	 */
	public void setVoltPointMSB(int index, int value){
		if(index == 0) index = 1;
		index = VOLT_DATA_POINT_MSB + 4 * index;
		writeDISTNX(index, value);
	}
	
	/**
	 * Gets millimeter value of the specific index for the LSB.<br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @return Returns int
	 */
	public int getDistPointLSB(int index){
		if(index == 0) index = 1;
		index = DIST_DATA_POINT_LSB + 4 * index;
		return readDISTNX(index, 2);
	}
	
	/**
	 * Sets millimeter value of the specific index for the LSB. <br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @param value int
	 */
	public void setDistPointLSB(int index, int value){
		if(index == 0) index = 1;
		index = DIST_DATA_POINT_LSB + 4 * index;
		writeDISTNX(index, value);
	}
	
	/**
	 * Gets millimeter value of the specific index for the MSB. <br>
	 * Used for recalibrating the sensor.
	 * @param index max 39
	 * @return int
	 */
	public int getDistPointMSB(int index){
		if(index == 0) index = 1;
		index = DIST_DATA_POINT_MSB + 4 * index;
		return readDISTNX(index, 2);
	}
	
	/**
	 * Sets millimeter value of the specific index for the MSB. <br>
	 * Used for recalibrating the sensor.
	 * @param index max 39.
	 * @param value int
	 */
	public void setDistPointMSB(int index, int value){
		if(index == 0) index = 1;
		index = DIST_DATA_POINT_MSB + 4 * index;
		writeDISTNX(index, value);
	}
	
	/**
	 * Returns an integer value from the specified register.<br>
	 * @param register I2C register, e.g 0x41
	 * @param bytes number of bytes to read 1 or 2
	 * @return int value from register
	 */
	private int readDISTNX(int register, int bytes){
		int buf0;
		int buf1;
		
		getData(register, buf, bytes);
		
		if (bytes == 1)	return buf[0] & 0xFF;
		
		buf0 = buf[0] & 0xFF;
		buf1 = buf[1] & 0xFF;
		return buf1 * 256 + buf0;
	}
	
	/**
	 * Writes an integer value to the register. <br>
	 * This is called if two bytes are to be written.<br>
	 * All other methods just call sendData() from i2cSensor.
	 * @param register I2C register, e.g 0x41
	 * @param value integer value to be written to the register
	 */
	private void writeDISTNX(int register, int value){		
		int buf0;
		int buf1;
		
		buf0 = value % 256;
		buf1 = value / 256;
		buf[0] = (byte)buf0;
		buf[1] = (byte)buf1;
		
		sendData(register, buf, 2);	
	}

	public float[] getRanges() {
		float [] ranges = new float[1]; // Optical sensor can only return one value
		ranges[0] = getRange();
		return ranges;
	}
}
