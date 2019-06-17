package lejos.pc.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lejos.nxt.remote.NXTCommRequest;

/**
 * 
 * Interface that all <code>NXTComm</code> implementation classes must implement for low-level communication
 * with the NXT.
 *
 */
public interface NXTComm extends NXTCommRequest {
    /**
     * Lego Communications Protocol (<code>LCP</code>) I/O mode. The LCP is defined by The Lego Company to allow limited remote 
     * command control of a NXT brick. 
     * 
     * See the <a href="http://mindstorms.lego.com">Lego Mindstorms</a> Site. Look for the Bluetooth Developer Kit in Support |
     * Files | Advanced
     */
    public static final int LCP = 1;
    /**
     * <code>PACKET</code> I/O mode. This is default and  is probably the best mode to use if you are talking to a
     * NXT using the leJOS classes. Headers are included for each packet of data sent and received.
     */
    public static final int PACKET = 0;
    /**
     * <code>RAW</code> I/O mode. This mode is just that and omits any headers. It is used normally for connections to non-NXT 
     * devices such as cell phones, etc.
     */
    public static final int RAW = 2;
    
            
	/**
	 * Search for NXTs over USB, Bluetooth or both
     * 
	 * @param name name of the NXT or <code>null</code>
	 * @return a NXTInfo object describing the NXt found and the connection to it
	 * @throws NXTCommException
	 */
	public NXTInfo[] search(String name) throws NXTCommException;

	/**
	 * Connect to a NXT found by a search or created from name and address.
	 * 
	 * @param nxt the <code>NXTInfo</code> object for the NXT
     * @param mode the mode for the connection: <code>{@link #LCP}</code>, <code>{@link #PACKET}</code> or
     * <code>{@link #RAW}</code>
	 * @return <code>true</code> if the open succeeded
	 * @throws NXTCommException
	 */
	public boolean open(NXTInfo nxt, int mode) throws NXTCommException;

	/**
	 * Connect to a NXT found by a search or created from name and address.
	 * 
	 * @param nxt the <code>NXTInfo</code> object for the NXT
	 * @return <code>true</code> if the open succeeded
	 * @throws NXTCommException
	 */
	public boolean open(NXTInfo nxt) throws NXTCommException;
		
	/**
	 * Read data from a NXT that has an open connection.
	 * Used for stream connections.
	 * 
	 * @return the data in a <code>byte[]</code> array
	 * @throws IOException
	 */
	public byte[] read() throws IOException;
	
	/**
	 * Request the number of bytes available to read.
	 * 
	 * @return the number of bytes available
	 * @throws IOException
	 */
	public int available() throws IOException;
	
	/**
	 * Write data to a NXT that has an open connection.
	 * 
	 * @param data the data to be written. Used for stream connections.
	 * 
	 * @throws IOException
	 */
	public void write(byte [] data) throws IOException;
	
	/**
	 * Return an <code>OutputStream</code> for writing a stream of data to the NXT over this connection.
	 * 
	 * @return the <code>OutputStream</code> object
	 */
	public OutputStream getOutputStream();
	
	/**
	 * Return an <code>InputStream</code> for reading a stream of data from the NXT over this connection.
	 * 
	 * @return the <code>InputStream</code> object
	 */
	public InputStream getInputStream();
}
