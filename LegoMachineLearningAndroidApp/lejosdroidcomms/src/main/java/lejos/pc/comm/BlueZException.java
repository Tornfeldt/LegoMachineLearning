package lejos.pc.comm;

/**
 * 
 * Exception thrown by the jbluez library.
 *
 */
public class BlueZException extends Exception {

	private static final long serialVersionUID = -1533948968757411349L;

	public BlueZException() {
	}

	public BlueZException(String message) {
		super(message);
	}
}
