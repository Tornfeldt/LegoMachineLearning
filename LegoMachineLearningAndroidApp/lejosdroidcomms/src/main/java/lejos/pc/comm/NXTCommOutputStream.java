package lejos.pc.comm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Implementation of OutputStream over NXTComm using Bluetooth.
 */
public class NXTCommOutputStream extends OutputStream {
	private ByteArrayOutputStream baos;
	private NXTComm nxtComm;
	
	public NXTCommOutputStream(NXTComm nxtComm) {
		this.nxtComm = nxtComm;
		baos = new ByteArrayOutputStream();
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
		baos.write(b, off, len);
	}
	
	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}
	
	@Override
	public void flush() throws IOException {
        if (baos.size() > 0)
        {
            byte[] b = baos.toByteArray();
            nxtComm.write(b);
            baos.reset();
        }
	}
	
	@Override
	public void close() throws IOException {
		this.flush();
		//TODO mark this stream as closed
	}
}
