package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.util.EndianTools;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * <p>Class for controlling dGPS sensor from Dexter Industries. Documentation for this sensor
 * can be found at <a href="http://www.dexterindustries.com/download.html#dGPS">Dexter Industries</a>.</p>
 * 
 * <p>The sensor uses an integer-based representation of latitude and longitude values.
 * Assume that you want to convert the value of 77 degrees, 2 minutes and 54.79 seconds
 * to the integer-based representation. The integer value is computed as follows:
 * <code>R = 1000000 * (D + M / 60 + S / 3600)</code>
 * where <code>D=77</code>, <code>M=2</code>, and <code>S=54.79</code>.
 * For the given values, the formula yields the integer value 77048553.
 * Basically, this is equivalent to decimal degrees times a million.</p>
 * 
 * <p>You can use the standard <code>javax.microedition.location</code> package with this class by
 * using a <code>dGPSCriteria</code> object to request a LocationProvider as follows:</p>
 * <p><code>dGPSCriteria criteria = new gGPSCriteria(SensorPort.S1);<br>
 * LocationProvider lp = LocationProvider.getInstance(criteria);
 * </p></code>
 *
 * @author Mark Crosbie  <mark@mastincrosbie.com>
 * 22 January, 2011
 *
*/
public class GPSSensor extends I2CSensor {
	/*
	 * Documentation can be found here: http://www.dexterindustries.com/download.html#dGPS
	 */
	
	public static final byte DGPS_I2C_ADDR   = 0x06;      /*!< Barometric sensor device address */
	public static final byte DGPS_CMD_UTC    = 0x00;      /*!< Fetch UTC */
	public static final byte DGPS_CMD_STATUS = 0x01;      /*!< Status of satellite link: 0 no link, 1 link */
	public static final byte DGPS_CMD_LAT    = 0x02;      /*!< Fetch Latitude */
	public static final byte DGPS_CMD_LONG   = 0x04;      /*!< Fetch Longitude */
	public static final byte DGPS_CMD_VELO   = 0x06;      /*!< Fetch velocity in cm/s */
	public static final byte DGPS_CMD_HEAD   = 0x07;      /*!< Fetch heading in degrees */
	public static final byte DGPS_CMD_DIST   = 0x08;      /*!< Fetch distance to destination */
	public static final byte DGPS_CMD_ANGD   = 0x09;      /*!< Fetch angle to destination */
	public static final byte DGPS_CMD_ANGR   = 0x0A;      /*!< Fetch angle travelled since last request */
	public static final byte DGPS_CMD_SLAT   = 0x0B;      /*!< Set latitude of destination */
	public static final byte DGPS_CMD_SLONG  = 0x0C;      /*!< Set longitude of destination */
	
	/**
	* Constructor
	* @param sensorPort the sensor port the sensor is connected to
	*/
    public GPSSensor(I2CPort sensorPort) {
        super(sensorPort, DGPS_I2C_ADDR, I2CPort.STANDARD_MODE, TYPE_LOWSPEED);
    }
    
    /**
	* Return status of link to the GPS satellites
	* LED on dGPS should light if satellite lock acquired
	* @return true if GPS link is up, else false
	*/
    public boolean linkStatus() {
   		byte reply[] = new byte[1];

    	this.getData(DGPS_CMD_STATUS, reply, 0, 1);
    	return (reply[0] == 1);
    }

	/**
	* Get the current time stored on the dGPS
	* @return current UTC time stored on the device
	*/ 
    public int getUTC() {
   	 	byte reply[] = new byte[4];
    	int r = this.getData(DGPS_CMD_UTC, reply, 0, 4);

    	if(r < 0) return r;

    	return EndianTools.decodeIntBE(reply, 0);
    }


    /**
	 * <p>Read the current latitude as an integer value (decimal degrees times a million).
	 * See {@linkplain GPSSensor here} for explanation.  
     * positive=North, negative=South.</p>
     * 
     * @return current latitude in decimal degrees
     */
    public int getLatitude(){
    	byte reply[] = new byte[4];

    	this.getData(DGPS_CMD_LAT, reply, 0, 4);
    	
    	return EndianTools.decodeIntBE(reply, 0);
    }

	/**
	 * <p>Read the current longitude as an integer value (decimal degrees times a million).
	 * See {@linkplain GPSSensor here} for explanation.  
	 * positive=East, negative=West.</p>
     * 
	 * @return current longitude in decimal degrees
	 */ 
    public int getLongitude() {
   	 	byte reply[] = new byte[4];

    	this.getData(DGPS_CMD_LONG, reply, 0, 4);

    	return EndianTools.decodeIntBE(reply, 0);
    }


	/**
	 * Read the current velocity in cm/s
	 * @return current velocity in cm/s
	 */
    public int getVelocity() {
    	byte reply[] = new byte[4];
   
    	this.getData(DGPS_CMD_VELO, reply, 1, 3);
    	reply[0] = 0;

    	return EndianTools.decodeIntBE(reply, 0);
    }

    /**
     * Read the current heading in degrees as reported by the GPS chip.
     * @return current heading in degrees
     */
    public int getHeading() {
   	 byte reply[] = new byte[2];

    	this.getData(DGPS_CMD_HEAD, reply, 0, 2);

    	return EndianTools.decodeUShortBE(reply, 0);
    }

    /**
     * <p>Read the current relative heading in degrees. The relative heading is the angle of travel
     * since the last request of this method. The first time this method is called, the GPS coordinates
     * are stored in this chip. The second time it is called it reports the angle between the current 
     * coordinates and the coordinates from the previous call.</p> 
     * (See dGPS manual.)
     * @return relative head
     */
     public int getRelativeHeading() {
    	 byte reply[] = new byte[2];

    	 this.getData(DGPS_CMD_ANGR, reply, 0, 2);

     	return EndianTools.decodeUShortBE(reply, 0);
     }

	/**
	* Distance to destination in meters
	* @return distance to destination in meters
	*/
     public int getDistanceToDest() {
    	 byte reply[] = new byte[4];

    	 this.getData(DGPS_CMD_DIST, reply, 0, 4);

    	 return EndianTools.decodeIntBE(reply, 0);
     }

	/**
	* Angle to destination in degrees
	* @return angle to destination in degrees
	*/
     public int getAngleToDest() {
    	 byte reply[] = new byte[2];
    	 
    	 this.getData(DGPS_CMD_ANGD, reply, 0, 2);

    	 return EndianTools.decodeUShortBE(reply, 0);
     }

	/**
	* Set destination latitude coordinates
	* @param latitude destination's latitude in decimal degrees
	* @return 0 if no error else error code
	*/
     public int setLatitude(int latitude) {
    	 // We set the latitude in the dGPS
    	 byte args[] = new byte[4];
    	 EndianTools.encodeIntBE(latitude, args, 0);

    	 return this.sendData(DGPS_CMD_SLAT, args, 0, 4);
     }


	/**
	* Set destination longitude coordinates
	* @param longitude destination's longitude in decimal degrees
	* @return 0 if no error else error code
	*/
     public int setLongitude(int longitude) {
    	 // We set the longitude in the dGPS
    	 byte args[] = new byte[4];
    	 EndianTools.encodeIntBE(longitude, args, 0);

    	 return this.sendData(DGPS_CMD_SLONG, args, 0, 4);
     }
}
