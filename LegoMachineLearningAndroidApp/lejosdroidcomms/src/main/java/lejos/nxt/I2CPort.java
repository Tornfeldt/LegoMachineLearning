package lejos.nxt;

/**
 * Abstraction for a port that supports I2C sensors.
 * 
 * This is a simplified version of the interface used by for remote execution 
 * of I2C.
 * 
 * @author Lawrie Griffiths
 *
 */
public interface I2CPort extends BasicSensorPort {
	
    public static final int STANDARD_MODE = 0;
    public static final int LEGO_MODE = 1;
    public static final int ALWAYS_ACTIVE = 2;
    
    /** Do not release the i2c bus between requests */
    public static final int NO_RELEASE = 4;
    /** Use high speed I/O (125KHz) */
    public static final int HIGH_SPEED = 8;
    /** Maximum read/write request length */
    public static final int MAX_IO = 32;
    
	public int getId();
}
