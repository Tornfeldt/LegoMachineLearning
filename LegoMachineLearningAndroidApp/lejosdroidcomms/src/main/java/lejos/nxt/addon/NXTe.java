package lejos.nxt.addon;

import lejos.nxt.SensorPort;
import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import java.util.ArrayList;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a  Lattebox NXT Extension Kit with  Lattebox 10-Axis Servo Kit
 * http://www.lattebox.com
 * UML: http://www.juanantonio.info/p_research/robotics/lejos/nxj/lattebox/LatteboxNXTeKit.png
 *
 * @author Juan Antonio Brenha Moral
 * 
 */
public class NXTe  extends I2CSensor{
	//LSC
	private ArrayList<LSC> arrLSC;
	private final int MAXIMUM_LSC = 4;
	
	//Exception handling
	private final String ERROR_SERVO_DEFINITION =  "Error with Servo Controller definition";
	private final String ERROR_SPI_CONFIGURATION = "Error in SPI Configuration";
	
	//I2C
	private SensorPort portConnected;
	private final byte SPI_PORT[] = {0x01,0x02,0x04,0x08};//SPI Ports where you connect LSC
	public static final byte NXTE_ADDRESS = 0x50;
	private final byte REGISTER_IIC = (byte)0xF0;//NXTe IIC address
	
	/**
	 * Constructor
	 * 
	 * @param port
	 */
	public NXTe(SensorPort port){
		super(port, NXTE_ADDRESS, I2CPort.LEGO_MODE, TYPE_LOWSPEED_9V);
		
		port.setType(TYPE_LOWSPEED_9V);
		port.setMode(MODE_RAW);
		
		portConnected = port;
		
		arrLSC = new ArrayList<LSC>();
		
		this.setAddress((int) NXTE_ADDRESS);
		int I2C_Response;
		I2C_Response = this.sendData((int)this.REGISTER_IIC, (byte)0x0c);
	}
	
	/**
	 * Add a LSC, Lattebox Servo Controller
	 * 
	 * @param SPI_PORT
	 * @throws Exception
	 */
	public void addLSC(int SPI_PORT) throws ArrayIndexOutOfBoundsException{
		if(arrLSC.size() <= MAXIMUM_LSC){
			LSC LSCObj = new LSC(this.portConnected,this.SPI_PORT[SPI_PORT]);
			arrLSC.add(LSCObj);
		}else{
			//throw new ArrayIndexOutOfBoundsException(ERROR_SERVO_DEFINITION);
			throw new ArrayIndexOutOfBoundsException();
		}		
	}	
	
	/**
	 * Get a LSC, Lattebox Servo Controller
	 * 
	 * @param index in the array
	 * @return the LSC object
	 */
	public LSC getLSC(int index){
		return arrLSC.get(index);
	}
}