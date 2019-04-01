package lejos.pc.comm;

import java.util.Vector;

import lejos.internal.jni.JNIClass;
import lejos.internal.jni.JNIException;
import lejos.internal.jni.JNILoader;

/**
 * Implementation of NXTComm using the the LEGO Fantom API.
 * 
 * Currently only supports USB access. The Fantom read function when using
 * Bluetooth seems to be broken, it does not work if the amount of data
 * available to be read does not match the amount of data requested. So for now
 * we only support USB.
 * 
 * 
 * Notes
 * The Fantom read and write functions have a built in timeout period of
 * 20 seconds. This module assumes that this timeout exists and uses it to
 * timeout some requests.
 * 
 * Should not be used directly - use NXTCommFactory to create
 * an appropriate NXTComm object for your system and the protocol
 * you are using.
 *
 */
public class NXTCommFantom extends NXTCommUSB implements JNIClass {
    private static final int MIN_TIMEOUT = 5000;
    private static final int MAX_ERRORS = 10;
	
	private native String[] jfantom_find();
	private native long jfantom_open(String nxt);
	private native void jfantom_close(long nxt);
	private native int jfantom_send_data(long nxt, byte [] message, int offset, int len);
	private native int jfantom_read_data(long nxt, byte[] data, int offset, int len);

	@Override
	Vector<NXTInfo> devFind()
    {
        // Address is in standard format so we can use the helper function
        // to do all the hard work.
		return find(jfantom_find());
    }
    
	@Override
	long devOpen(NXTInfo nxt)
    {
        if (nxt.btResourceString == null) return 0;
        return jfantom_open(nxt.btResourceString);
    }
    
	@Override
	void devClose(long nxt)
    {
		// Attention: is called from NXTCommUSB.finalize
        jfantom_close(nxt);
    }
    
	@Override
	int devWrite(long nxt, byte [] message, int offset, int len)
    {
        return jfantom_send_data(nxt, message, offset, len);
    }
    
	@Override
	int devRead(long nxt, byte[] data, int offset, int len)
    {
        int ret;
        // The Fantom lib does not seem to detect when the USB cable is 
        // disconnected very well. It does not give an error it just times out
        // very quickly. We treat fast timeouts as a potential disconnect.
        long startTime = System.currentTimeMillis();
        int errorCnt = 0;
        while((ret=jfantom_read_data(nxt, data, offset, len)) == 0)
        {
            long now = System.currentTimeMillis();
            if (now - startTime > MIN_TIMEOUT) return ret;
            if (errorCnt++ > MAX_ERRORS) return -1;
            startTime = now;
        }
        return ret;
    }
    
    
    @Override
	boolean devIsValid(NXTInfo nxt)
    {
        return (nxt.btResourceString != null);
    }
	
    private static boolean initialized = false;
    private static synchronized void initialize0(JNILoader jnil) throws JNIException
    {
    	if (!initialized)
    		jnil.loadLibrary(NXTCommLibnxt.class, "jfantom");
    	
    	initialized = true;
    }
    
	public boolean initialize(JNILoader jnil) throws JNIException
	{
		initialize0(jnil);
		return true;
	}
}

