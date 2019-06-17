package lejos.nxt.addon;

import lejos.nxt.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/** 
 *Abstraction for an RCX temperature sensor. 
 * 
 * @author Soren Hilmer
 * 
 */
public class RCXTemperatureSensor
 implements SensorConstants {
    LegacySensorPort port;
    
    /**
     * Create an RCX temperature sensor object attached to the specified port.
     * @param port port, e.g. Port.S1
     */
    public RCXTemperatureSensor(LegacySensorPort port)
    {
        this.port = port;
        port.setTypeAndMode(TYPE_TEMPERATURE,
                            MODE_RAW);
    }
    
    /**
     * Read the current sensor value.
     * @return raw Value.
     */
    public int readValue()
    {
        return port.readRawValue();
    }

    /**
     * Convert sensor value to Celcius, value outside [-20;70] is not accurate
     * @return sensor value converted to Celcius
     **/
    public float getCelcius() {
        return (785-readValue())/8.0f;
    }

    /**
     * Convert sensor value to Fahrenheit, value outside [-4;158] is not accurate
     * @return sensor value converted to Fahrenheit
     **/
    public float getFahrenheit() {
        return (getCelcius()*1.8f) + 32.0f;
    }

}
