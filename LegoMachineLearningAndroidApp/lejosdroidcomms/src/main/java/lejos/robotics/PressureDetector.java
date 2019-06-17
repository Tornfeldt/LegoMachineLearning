package lejos.robotics;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Interface for pressure sensors.
 * 
 * @author Lawrie Griffiths
 *
 */
public interface PressureDetector {
	
    /**
     * Get the pressure reading in kilopascals
     * 
     * @return the pressure in kPa
     */
	public float getPressure();
}
