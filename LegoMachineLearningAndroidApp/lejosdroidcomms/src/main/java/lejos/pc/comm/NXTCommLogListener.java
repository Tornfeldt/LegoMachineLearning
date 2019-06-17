package lejos.pc.comm;

/**
 * Listener for log events
 * 
 * @author Matthias Paul Scholz
 * 
 */
public interface NXTCommLogListener {
	
	public void logEvent(String message);
	public void logEvent(Throwable throwable);

}
