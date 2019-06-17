package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports the mindsensors RCX Motor Multiplexer
 * 
 * @author Lawrie Griffiths
 * 
 */
public class RCXMotorMultiplexer extends I2CSensor {
	private byte[] buf = new byte[2];

    public static final int DEFAULT_RCXMMUX_ADDRESS = 0xb4;

	public RCXMotor A = new RCXMotor(new RCXPlexedMotorPort(this,0));
	public RCXMotor B = new RCXMotor(new RCXPlexedMotorPort(this,1));
	public RCXMotor C = new RCXMotor(new RCXPlexedMotorPort(this,2));
	public RCXMotor D = new RCXMotor(new RCXPlexedMotorPort(this,3));
	
	
	public RCXMotorMultiplexer(I2CPort port) {
		this(port, DEFAULT_RCXMMUX_ADDRESS);

	}

	public RCXMotorMultiplexer(I2CPort port, int address) {
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED_9V);
	}

	public void setSpeed(int speed, int id) {
		buf[0] = (byte) speed;
		sendData(0x43 + (id*2), buf, 1);
	}
	
	public int getSpeed(int id) {
		getData(0x43 + (id*2), buf, 1);
	    return buf[0] & 0xFF;
	}
	
	public void sendCommand(int id, int direction, int speed) {
		buf[0] = (byte) direction;
		buf[1] = (byte) speed;
		sendData(0x42 + (id*2), buf, 2);
	}
	
	public void setDirection(int direction, int id) {
		buf[0] = (byte) direction;
		sendData(0x42 + (id*2), buf, 1);
	}
	
	public int getDirection(int id) {
		getData(0x42 + (id*2), buf, 1);
	    return buf[0] & 0xFF;
	}
}
