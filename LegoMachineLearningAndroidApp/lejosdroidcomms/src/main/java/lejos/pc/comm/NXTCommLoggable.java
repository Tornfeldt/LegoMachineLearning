package lejos.pc.comm;

import java.util.ArrayList;

/**
 * Abstract class that allows inheriting class to register and use log listeners.
 * 
 * @author Lawrie Griffiths and Matthias Paul Scholz
 *
 */
public abstract class NXTCommLoggable {
	protected ArrayList<NXTCommLogListener> fLogListeners;
	
	public NXTCommLoggable() {
		fLogListeners = new ArrayList<NXTCommLogListener>();
	}
	

	/**
	 * register log listener
	 * 
	 * @param listener the log listener
	 */
	public void addLogListener(NXTCommLogListener listener) {
		fLogListeners.add(listener);
	}

	/**
	 * unregister log listener
	 * 
	 * @param listener the log listener
	 */
	public void removeLogListener(NXTCommLogListener listener) {
		fLogListeners.remove(listener);
	}

	/**
	 * Log an exception to all the log listeners
	 * 
	 * @param t a Throwable
	 */
	protected void log(Throwable t) {
		for (NXTCommLogListener listener : fLogListeners) {
			listener.logEvent(t);
		}
	}
	
	/**
	 * Log a message to all the log listeners
	 * 
	 * @param s the message
	 */
	protected void log(String s) {
		for (NXTCommLogListener listener : fLogListeners) {
			listener.logEvent(s);
		}
	}
	
}
