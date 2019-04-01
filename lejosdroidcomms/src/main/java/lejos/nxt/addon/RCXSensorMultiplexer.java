package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports Mindsensors RXMux<br>
 * This sensor allows you to connect up to four RCX type sensors toa single port.
 * Be aware that the sensor does not track the ticks of the rotation sensor when
 * the port is not selected. 
 * 
 * @author Michael Smith <mdsmitty@gmail.com>
 * 
 */
public class RCXSensorMultiplexer extends I2CSensor{
	private final static byte CONTROL = 0x00;
	private final static int CHANNEL1 = 0xfe;
	private final static int CHANNEL2 = 0xfd;
	private final static int CHANNEL3 = 0xfb;
	private final static int CHANNEL4 = 0xf7;
	private final static int ALLOFF = 0xFF;

    public static final int DEFAULT_RCXSMUX_ADDRESS = 0x7e;
	
	/**
	 *
	 * @param port NXT Sensor port
	 */
	public RCXSensorMultiplexer(I2CPort port){
		this(port, DEFAULT_RCXSMUX_ADDRESS);
	}

	/**
	 *
     * @param port NXT Sensor port
     * @param address I2C address
	 */
	public RCXSensorMultiplexer(I2CPort port, int address){
		super(port, address, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
	}

	/**
	 * Selects channel one
	 *
	 */	
	public void setChannelOne(){	
		sendData(CONTROL, (byte)CHANNEL1);
	}
	
	/**
	 * Selects channel two
	 *
	 */
	public void setChannelTwo(){
		sendData(CONTROL, (byte)CHANNEL2);
	}
	
	/**
	 * Selects channel three
	 *
	 */
	public void setChannelThree(){
		sendData(CONTROL, (byte)CHANNEL3);
	}
	
	/**
	 * Selects channel four
	 *
	 */
	public void setChannelFour(){
		sendData(CONTROL, (byte)CHANNEL4);
	}
	
	/**
	 * Turns off all channels
	 *
	 */
	public void allChannelsOff(){
		sendData(CONTROL, (byte)ALLOFF);
	}
}
