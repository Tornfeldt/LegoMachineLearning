package lejos.nxt;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction for a NXT sound sensor.
 * 
 */
public class SoundSensor implements SensorConstants {
	ADSensorPort port;
	
	/**
	 * Create a sound sensor object attached to the specified port.
	 * The sensor will be set to DB mode.
	 * @param port port, e.g. Port.S1
	 */
	public SoundSensor(ADSensorPort port)
	{
	   this.port = port;
	   port.setTypeAndMode(TYPE_SOUND_DB,
                           MODE_PCTFULLSCALE);
	}
	
	/**
	 * Create a sound sensor object attached to the specified port,
	 * and sets DB or DBA mode.
	 * @param port port, e.g. Port.S1
	 * @param dba true to set DBA mode, false for DB mode.
	 */
	public SoundSensor(ADSensorPort port, boolean dba)
	{
	   this.port = port;
       port.setTypeAndMode(
    		   (dba ? TYPE_SOUND_DBA
    				: TYPE_SOUND_DB),
    		   MODE_PCTFULLSCALE);   
	}
	
	/**
	 * Set DB or DBA mode.
	 * @param dba true to set DBA mode, false for DB mode.
	 */
	public void setDBA(boolean dba)
	{
	    port.setType((dba ? TYPE_SOUND_DBA
	    				  : TYPE_SOUND_DB));
	}

	/**
	 * Read the current sensor value.
	 * @return value as a percentage.
	 */
	public int readValue()
	{
		return ((1023 - port.readRawValue()) * 100/ 1023);  
	}
}
