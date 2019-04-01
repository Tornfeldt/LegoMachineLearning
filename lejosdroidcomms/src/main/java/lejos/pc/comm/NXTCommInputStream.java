package lejos.pc.comm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of InputStream over NXTComm using Bluetooth.
 */
public class NXTCommInputStream extends InputStream {
	private NXTComm nxtComm;
	private byte buf[];
	private int bufIdx, bufSize;
	boolean endOfFile;
	
	public NXTCommInputStream(NXTComm nxtComm) {
		this.nxtComm = nxtComm;
		endOfFile = false;
		bufIdx = 0;
		bufSize = 0;
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (endOfFile)
			return -1;
		
		int avail = bufSize - bufIdx;
		if (avail <= 0)
		{
			buf = nxtComm.read();
			bufIdx = 0;
			if (buf == null || buf.length == 0)
			{
				bufSize = 0;
				endOfFile = true;
				return -1;
			}
			avail = bufSize = buf.length;
		}
		
		if (len > avail)
			len = avail;
		
		System.arraycopy(buf, bufIdx, b, off, len);
		bufIdx += len;
		
		return len;
	}
	
    /**
     * Returns one byte as an integer between 0 and 255.  
     * Returns -1 if the end of the stream is reached.
     * Does not return till some bytes are available.
     */
	@Override
	public int read() throws IOException
    {
		if (endOfFile)
			return -1;
		
		int avail = bufSize - bufIdx;
		if (avail <= 0)
		{
			buf = nxtComm.read();
			bufIdx = 0;
			if (buf == null || buf.length == 0)
			{
				bufSize = 0;
				endOfFile = true;
				return -1;
			}
			bufSize = buf.length;
		}
		
		return buf[bufIdx++] & 0xFF;
	}
	
    /**
     * returns the number of bytes in the input buffer - can be read without blocking
     */
    @Override
	public int available() throws IOException
    {
       return bufSize - bufIdx;
    }
    
    /**
     * Close the stream
     */
    @Override
	public void close() throws IOException
    { 
        endOfFile = true;
    }
}
