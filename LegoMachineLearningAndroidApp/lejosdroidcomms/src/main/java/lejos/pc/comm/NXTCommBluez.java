package lejos.pc.comm;

import java.io.*;

import lejos.internal.jni.JNIClass;
import lejos.internal.jni.JNIException;
import lejos.internal.jni.JNILoader;

/**
 * Implementation of NXTComm using the the jbluez library 
 * on Linux or Unix systems. 
 * 
 * Should not be used directly - use NXTCommFactory to create
 * an appropriate NXTComm object for your system and the protocol
 * you are using.
 *
 */
public class NXTCommBluez implements NXTComm, JNIClass {

	private static final String BDADDR_ANY = "00:00:00:00:00:00";

	private int sk = -1;
	private int lenRemaining = 0;
	byte[] savedData = null;

	public NXTInfo[] search(String name) {
		String[] btString = null;
		
		try {
			btString = rcSearch(name);
		} catch (BlueZException e) {
			System.err.println(e.getMessage());	
		}
		if (btString == null) return new NXTInfo[0];
		else {
			NXTInfo[] nxts = new NXTInfo[btString.length];
			for(int i=0;i<btString.length;i++) {
				NXTInfo nxtInfo = new NXTInfo();
				if (btString[i] == null) {
					System.err.println("Null btString");
					return new NXTInfo[0];
				}
				int sep = btString[i].indexOf("::");
				//System.out.println("Setting address to " + btAddress);
				nxtInfo.deviceAddress =  btString[i].substring(sep+2);
				nxtInfo.name = btString[i].substring(0, sep);
				nxtInfo.protocol = NXTCommFactory.BLUETOOTH;
				nxtInfo.btResourceString = btString[i];
				
				nxts[i] = nxtInfo;			
			}
			return nxts;
		}
	}

	public void close() throws IOException{
		try {
			rcSocketShutdown(sk);
		} catch (IOException ioe) {
			//System.err.println("Shutdown failed");
		}
		if (sk != -1) rcSocketClose(sk);
		sk = -1;
	}

	public boolean open(NXTInfo nxt, int mode) throws NXTCommException {
		lenRemaining = 0;
		savedData = null;

        if (mode == RAW) throw new NXTCommException("RAW mode not implemented");
		try {
			open(BDADDR_ANY, nxt.deviceAddress, 1);
			nxt.connectionState = (mode == LCP ? NXTConnectionState.LCP_CONNECTED : NXTConnectionState.PACKET_STREAM_CONNECTED);
			return true;
		} catch (BlueZException e) {
			nxt.connectionState = NXTConnectionState.DISCONNECTED;
			System.err.println("Error from open: " + e.getMessage());
			return false;
		}	
	}
    
    public boolean open(NXTInfo nxt) throws NXTCommException
    {
        return open(nxt, PACKET);
    }


	public byte [] sendRequest(byte[] request, int replyLen) throws IOException {
		
		// add lsb & msb
		byte[] lsb_msb = new byte[2];
		lsb_msb[0] = (byte) request.length;
		lsb_msb[1] = (byte) 0x00;
		request = concat(lsb_msb, request);
	
	    rcSocketSend(sk, request);
		
		if (replyLen == 0) return new byte[0];
		
		byte[] data = null;
	    data = rcSocketRecv(sk);
	
		// remove lsb & msb
		data = subArray(data, 2, data.length);

		return data;
	}

	private void open(String l_bdaddr, String r_bdaddr, int channel) throws BlueZException {
		boolean ok = false;

		try {
			//System.out.println("Creating socket");
			sk = rcSocketCreate();
			//System.out.println("Binding");
			rcSocketBind(sk, l_bdaddr);
			//System.out.println("Connecting");
			rcSocketConnect(sk, r_bdaddr, channel);

			ok = true;
		} finally {
			if (!ok) {
				if (sk != -1) {
					try {
						rcSocketClose(sk);
					} catch (IOException ioe) {}
					sk = -1;
				}
			}
		}
	}
	
	private byte[] concat(byte[] data1, byte[] data2) {
		int l1 = data1.length;
		int l2 = data2.length;
		
		byte[] data = new byte[l1 + l2];
		System.arraycopy(data1, 0, data, 0, l1);
		System.arraycopy(data2, 0, data, l1, l2);
		
		return data;
	}
		
	
	private byte[] subArray(byte[] data, int start, int end) {	

		byte[] result = new byte[end - start];
		System.arraycopy(data, start, result, 0, end - start);

		return result;
	}
	
	public byte [] read () throws IOException {
		byte [] availData = rcSocketRecv(sk); // Can read multiple packets
		int len = (availData == null ? 0 : availData.length);
		int newLenRemaining = 0;
		
		//System.out.println("Length = " + availData.length);
		
		if (len == 0) return null; // EOF
		
		if (len <= lenRemaining) { // Just more of previous packet
			lenRemaining -= len;
			return availData;
		}
		
		// Append data to saved data, if any
		if (savedData != null) {
			availData = concat(savedData, availData);
			len = availData.length;
			savedData = null;
		}
		
		// Make sure we have the header and at least one byte of data
		while (len < 3) {
			byte [] moreData = rcSocketRecv(sk);
			if (moreData == null || moreData.length == 0) return null;
			availData = concat(availData, moreData);
			len = availData.length;		
		}
		
		int i = lenRemaining;
		int dataLen = lenRemaining;
		
		while (i < len) { // Calculate length skipping packet headers
			if (len - i < 3) {
				//Save the remaining data
				savedData = new byte[len - i];
				for(int j=0;j<len-i;j++) savedData[j] = availData[i+j];
				break;
			}
			int lsb = availData[i++];
			int msb = availData[i++];
			int packetLen = ((lsb & 0xFF) + (msb << 8));
			
			//System.out.println("Packet length is " + packetLen);
			
            if (i + packetLen <= len) {
            	dataLen += packetLen;
            } else {
            	dataLen += (len-i);
            	newLenRemaining = packetLen - (len - i);
            }
            i += packetLen;
		}
		
		//System.out.println("data length is " + dataLen);
		
		byte [] data = new byte [dataLen];
		
		// Copy any data from previous packet
		for(i=0;i<lenRemaining;i++) data[i] = availData[i];

		int j = i;
		
		// Copy any available packets. The last one may be incomplete.		
		while (i < len-2) {
			int lsb = availData[i++];
			int msb = availData[i++];
			int packetLen = ((lsb & 0xFF) + (msb << 8));
            for(int k = 0;k<packetLen && i+k < len;k++) data[j++] = availData[i+k];
            i += packetLen;
		}
		
		lenRemaining = newLenRemaining;
		
		//System.out.println("Length remaining is " + lenRemaining);
		return data;
	}
	
	public int available() throws IOException {
		return 0;
	}
	
	public void write(byte[] data) throws IOException {
		byte[] lsb_msb = new byte[2];
		lsb_msb[0] = (byte) data.length;
		lsb_msb[1] = (byte) ((data.length >> 8) & 0xff);
		rcSocketSend(sk, concat(lsb_msb, data));
	}
	
	public OutputStream getOutputStream() {
		return new NXTCommOutputStream(this);		
	}
	
	public InputStream getInputStream() {
		return new NXTCommInputStream(this);		
	}
	
	native private String[] rcSearch(String name) throws BlueZException;
	
	native private int rcSocketCreate() throws BlueZException;

	native private void rcSocketBind(int sk, String bdaddr) throws BlueZException;

	native private void rcSocketConnect(int sk, String bdaddr, int channel) throws BlueZException;

	native private void rcSocketSend(int sk, byte[] data) throws IOException;

	native private byte[] rcSocketRecv(int sk) throws IOException;

	native private void rcSocketShutdown(int sk) throws IOException;

	native private void rcSocketClose(int sk) throws IOException;

	
    private static boolean initialized = false;
    private static synchronized void initialize0(JNILoader jnil) throws JNIException
    {
    	if (!initialized)
    		jnil.loadLibrary(NXTCommLibnxt.class, "jbluez");
    	
    	initialized = true;
    }
    
	public boolean initialize(JNILoader jnil) throws JNIException
	{
		initialize0(jnil);
		return true;
	}
}
