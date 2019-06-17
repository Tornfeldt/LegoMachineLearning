package lejos.nxt.addon;

import lejos.nxt.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports HiTechnics EOPD (Electro Optical Proximity Detector) sensor.<br>
 * This sensor is used to detect objects and small changes in distance to a target.
 *  Unlike the lego light sensor it is not affected by other light sources.
 * 
 * @author Michael Smith <mdsmitty@gmail.com>
 * 
 */
public class EOPD implements SensorConstants{
	ADSensorPort port;
	
	/**
	 * By default the sensor is short range.
	 * @param port NXT sensor port 1-4
	 */
	public EOPD (ADSensorPort port){
		this.port = port;
		port.setTypeAndMode(TYPE_LIGHT_INACTIVE, MODE_PCTFULLSCALE);
	}
	
	/**
	 * 
	 * @param port NXT sensor port 1-4.
	 * @param longRange true = long range false = short range.
	 */
	public EOPD(ADSensorPort port, boolean longRange)
	{
		this.port = port;
		port.setTypeAndMode((longRange ? TYPE_LIGHT_ACTIVE : TYPE_LIGHT_INACTIVE),
			MODE_PCTFULLSCALE); 
	}
	
	/**
	 * Changes the sensor to short range mode.
	 *
	 */
	public void setModeShort(){
		port.setTypeAndMode(TYPE_LIGHT_INACTIVE, MODE_PCTFULLSCALE);
	}
	
	/**
	 * Changes the port to long range mode.
	 *
	 */
	public void setModeLong(){
		port.setTypeAndMode(TYPE_LIGHT_ACTIVE, MODE_PCTFULLSCALE);
	}
	
	/**
	 * @return The raw value is returned.
	 *  
	 */
	public int readRawValue() {
		return port.readRawValue();
	}
	
	/**
	 * 
	 * @return A value between 0 and 100. This mimics the HiTechnic
	 *  programming block.
	 */
	public int processedValue()
	{ 
		return (int)Math.sqrt((1023-port.readRawValue())*10); 
	}
}
