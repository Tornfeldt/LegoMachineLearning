package lejos.nxt;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * An abstraction for a sensor port that supports 
 * setting and retrieving types and modes of sensors.
 * 
 * @author Lawrie Griffiths.
 *
 */
public interface BasicSensorPort extends SensorConstants {

	public int getMode();
	
	public int getType();
	
	public void setMode(int mode);
	
	public void setType(int type);
	
	public void setTypeAndMode(int type, int mode);

}

