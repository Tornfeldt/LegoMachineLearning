package lejos.nxt.remote;

import java.io.IOException;

public class LCPException extends IOException
{
	public LCPException() {
		super();
	}

	public LCPException(String s) {
		super(s);
	}
	
	public LCPException(byte errorcode) {
		this(ErrorMessages.lcpErrorToString(errorcode));
	}
	
	public LCPException(String s, Throwable cause) {
		this(s);
		this.initCause(cause);
	}
}
