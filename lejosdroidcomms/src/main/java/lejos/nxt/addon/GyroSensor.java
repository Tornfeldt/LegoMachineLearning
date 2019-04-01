package lejos.nxt.addon;
   
import lejos.nxt.ADSensorPort;
import lejos.nxt.Motor;
//import lejos.nxt.LCD;
import lejos.nxt.SensorConstants;
import lejos.robotics.Gyroscope;
import lejos.util.Delay;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Support the HiTechnic Gyro sensor. Provides raw <code>int</code> (with optional offset) and <code>float</code> angular velocity in degrees/sec.
 * <p>
 * <b>Notes:</b> 
 * <ul>
 * <li>You may want to use <code>setSpeed()</code> on any motor you will be using before instantiating so the AD sensor voltage stablizes. 
 * Otherwise, the offset
 * may be skewed. See LeJOS forum post <a href="http://lejos.sourceforge.net/forum/viewtopic.php?f=7&t=2276">"motor setSpeed() 
 * changes AD sensor value"</a>
 * <li>The <code>getAngularVelocity()</code> method uses statistical analysis to continuously determine the offset/bias to apply to 
 * the raw sensor value
 * to be able to calculate the "true" degrees per second the sensor is rotating at. It is important that this is called frequently 
 * enough to ensure the sample population
 * is adequate. This could have been done in a dedicated sampling thread but most use cases (such as <code>{@link GyroDirectionFinder}</code>)
 * would be calling the <code>getAngularVelocity()</code> method on fast periodic basis to support integration, etc. that it was
 * felt a dedicated thread would be redundant.
 * </ul>
 * <b>Assumptions:</b>
 * <ul>
 * <li>The HiTechnic Gyro sensor NGY1044 (or equivalent) is being used. (<a href="http://www.hitechnic.com/" target=-"_blank">
 * http://www.hitechnic.com/</a>)
 * <li>If used, the <code>{@link #getAngularVelocity}</code> method call rate is at least 100 times/sec. If slower rates are
 * used, the offset/bias drift value may not reliably be detected.
 * </ul>
 * 
 * @author Lawrie Griffiths
 * @author Kirk Thompson
 *
 */
public class GyroSensor implements SensorConstants, Gyroscope {
    /** The <code>ADSensorPort</code> passed in the constructor.
     */
    protected ADSensorPort port;
	private int offset = 0;
    private float gsRawTotal =0f;
    private float gsvarianceTotal =0f;
    private int samples=1;
    private boolean calibrating=false;
    private long timestamp;
    private int consecutiveStdv=0;
    /**
     * Creates and initializes a new <code>GyroSensor</code> bound to passed <code>ADSensorPort</code>.
     * 
     * @param port The <code>SensorPort</code> the Gyro is connected to
     * @see lejos.nxt.SensorPort
     */
    public GyroSensor(ADSensorPort port) {
		this.port = port;
		port.setTypeAndMode(TYPE_CUSTOM, MODE_RAW);
	    timestamp = System.currentTimeMillis();
	}
	
    /**
     * Creates and initializes a new <code>GyroSensor</code> bound to passed <code>ADSensorPort</code> and sets the 
     * offset to be used in <code>{@link #readValue}</code>.
     * 
     * @param port The <code>SensorPort</code> the Gyro is connected to
     * @param offset The offset to apply to <code>readValue()</code>
     * @see lejos.nxt.SensorPort
     */
	public GyroSensor(ADSensorPort port, int offset) {
		this(port);
		this.offset = offset;
	}
	
	/**
	 * Read the gyro raw value and return with offset applied. Set offset to zero to return raw value.
	 * 
	 * @return gyro value
     * @see #setOffset(int)
     * @see #getAngularVelocity
	 */
	public int readValue() { 
		return (port.readRawValue() - offset); 
	}
	
	/**
	 * Set the offset used by <code>readValue()</code>. Default at instantiation is zero.
     * @param offset The <code>int</code> offset value
     * @see #readValue
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

    /** Calculate and return the current angular velocity. When integrating for a heading, values less than 1.0 can be ignored 
     * to minimize perceived drift since the resolution of the Gyroscope sensor is 1 deg/sec.
     * <p>
     * Be sure to call <code>recalibrateOffset()</code> to establish the offset before using this method. 
     * 
     * @return The current angular velocity in degrees/sec
     * @see #recalibrateOffset
     */
    public float getAngularVelocity() {
        final int REBASELINE_INTERVAL=5000; // time in ms before rebaselining sample populations. Allows for drift to be managed
        final int CONSECUTIVE_REPR_SAMPLS=10; // # of consecutive samples < STDDEV_ENVELOPE to assume static sensor
        
        /** NOTE: I don't think this is needed if motor controller started first via Motor.A.flt() - BB */
        /** NOTE2: It _is_ needed or my continual offset bias correction algorithm breaks. This provides the stdv threshold value
         *         of non-movement "zero" values for the gyro. - KPT */
        final float STDDEV_ENVELOPE=.55f; // the standard deviation determined to limit the offset/bias population. Assumes 
                                          // initial sample population was done with non-moving sensor
        
        int gsVal;
        float stdev;
        float gsvarianceTemp;
        
        // get sensor raw value (note that offset was zeroed in recalibrateOffset() but could be changed by user)
        gsVal=readValue();
        // calc variance
        gsvarianceTemp=(float)Math.pow(gsVal-(gsRawTotal+gsVal)/(samples+1),2);
        // get the standard deviation of the raw value against the total bias population
        stdev=(float)Math.sqrt((gsvarianceTotal + gsvarianceTemp)/(samples+1));
        // if less than x standard deviation from maintained offset population and somewhat consecutive, allow to 
        // be used in the offset/bias population
        if(stdev<STDDEV_ENVELOPE)consecutiveStdv++; else consecutiveStdv=0; 
        // assume consecutive stdevs within defined range provide representative sample of non-moving sensor
        // or, if told to run because we are calibrating...
        if (consecutiveStdv>CONSECUTIVE_REPR_SAMPLS||calibrating) {
            consecutiveStdv=0;
            // add sensor raw value to sample population
            gsRawTotal+=gsVal;
            samples++; 
            // add variance to variance population so we can do standard deviation calc in future iterations
            gsvarianceTotal+=Math.pow(gsVal-gsRawTotal/samples, 2);
        }
        
        // re-baseline every 5 seconds. This allows for drift to be assimilated
        if (System.currentTimeMillis()-timestamp>REBASELINE_INTERVAL) {
            timestamp = System.currentTimeMillis();
//            LCD.drawString("mean:" + (gsRawTotal/samples) + " ", 0, 1);
            stdev=(float)Math.sqrt(gsvarianceTotal/samples);
//            LCD.drawString("stdev:" + stdev + " ", 0, 2);
            // re-baseline using current averages
            gsRawTotal /= samples;
            gsvarianceTotal /= samples;
            samples = 1;
        }
        
        // subtract the mean bias from the raw value and return it
        if (samples==0) return 0f; // avoid NaN
        return gsVal-gsRawTotal/samples;
    }


    /** Samples the <u>stationary</u> (make sure it is) Gyro Sensor to determine the offset. Will reset the offset for
     * <code>ReadValue()</code> to 0 (zero). Takes 5 seconds.
     * 
     * @see #setOffset(int)
     */
    public void recalibrateOffset() {
    	// TODO: Replace with recalibrateOffsetAlt()? It has the advantage of waiting
    	// until the gyro is still and taking less time to calibrate. Less chance of user error via vibrations.
    	
        // *** seed the initial bias/offset population
        offset=0;
        gsvarianceTotal=0;
        gsRawTotal=0;
        samples=0;
        // populate bias population for 5 seconds
        calibrating=true;
        for (int i=0;i<1000;i++) {
            getAngularVelocity(); 
            Delay.msDelay(5);
        }
        calibrating=false;
    }
    
    /**
	 * Number of offset samples to average when calculating gyro offset.
	 *<p>
     * I'd like to comment that the Gyro I built this class with shifted it's zero value radically for up to 4 seconds so that is
      * why I recalibrate for 5 seconds [with a 5 ms delay... lots of samples] -KPT  
    */
	private static final int OFFSET_SAMPLES = 100;

    private double gOffset; // TODO: This previous Segoway variable should be the int offset value at top of GyroSensor code?
    
    
     
	/**
	 * This function sets a suitable initial gyro offset.  It takes
	 * 100 gyro samples over a time of 1/2 second and averages them to
	 * get the offset.  It also check the max and min during that time
	 * and if the difference is larger than one it rejects the data and
	 * gets another set of samples.
	 */
	public void recalibrateOffsetAlt() {
		double gSum;
		int  i, gMin, gMax, g;
		
		// Bit of a hack here. Ensure that the motor controller is active since this affects the gyro values for HiTechnic.
		// TODO: Only one needed, even if motor not plugged in?
		Motor.A.flt();
		Motor.B.flt(); 
		Motor.C.flt();
		
		do {
			gSum = 0.0;
			gMin = 1000;
			gMax = -1000;
			calibrating=true;
			for (i=0; i<OFFSET_SAMPLES; i++) {
				g = readValue();
				if (g > gMax)
					gMax = g;
				if (g < gMin)
					gMin = g;

				gSum += g;
				try { Thread.sleep(5);
				} catch (InterruptedException e) {}
			}
			calibrating=false;
		} while ((gMax - gMin) > 1);   // Reject and sample again if range too large

		//Average the sum of the samples.
		gOffset = gSum / OFFSET_SAMPLES; // TODO: Used to have +1, which was mainly for stopping Segway wandering.	
	}
    
}
