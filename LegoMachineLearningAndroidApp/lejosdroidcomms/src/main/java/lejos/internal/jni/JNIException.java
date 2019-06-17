package lejos.internal.jni;

public class JNIException extends Exception {

	private static final long serialVersionUID = -5982243842620794272L;

	public JNIException() {
		super();
	}

	public JNIException(String message) {
		super(message);
	}

	public JNIException(Throwable cause) {
		super(cause);
	}

	public JNIException(String message, Throwable cause) {
		super(message, cause);
	}

}
