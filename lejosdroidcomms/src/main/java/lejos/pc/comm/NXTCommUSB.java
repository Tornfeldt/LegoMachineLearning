package lejos.pc.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

import lejos.nxt.remote.NXTProtocol;

/**
 * Base Implementation of NXTComm for USB
 * 
 * This module implements two types of I/O over USB. 
 * 1. The standard Lego LCP format used for LCP command processing.
 * 2. A Simple packet based protocol that can be used to transport a simple
 *    byte stream.
 * Protocol 2 is required (rather then using raw USB operations), to allow the
 * signaling of things like open, and close over the connection. 
 * 
 * Notes
 * This module assumes that the device read and write functions have a built in
 * timeout period of approx 20 seconds. This module assumes that this timeout
 * exists and uses it to timeout some requests.
 * 
 * Should not be used directly - use NXTCommFactory to create
 * an appropriate NXTComm object for your system and the protocol
 * you are using.
 *
 */
public abstract class NXTCommUSB implements NXTComm {
	private long nxtPtr;
    private boolean packetMode = false;
    private boolean EOF = false;
    static final int USB_BUFSZ = 64;
    static final String VENDOR_ATMEL = "0x03EB";
    static final String PRODUCT_SAMBA = "0x6124";
    static final String SAMBA_NXT_NAME = "%%NXT-SAMBA%%";
    
    private byte[] inBuf = new byte[USB_BUFSZ*8];
    private byte[] outBuf = new byte[USB_BUFSZ*8];
    int inCnt = 0;
    int inOffset = 0;
    int outCnt = 0;
	
    /**
     * Return a vector of available nxt devices. Each NXTInfo item should
     * have the address field populated and the other fields must contain
     * sufficient information such that a call to devIsValid will return
     * true and that devOpen will connect to the device. The name field may
     * be left empty, in which case it will be populated by code in this class.
     * @return vector of available nxt devices.
     */
	abstract Vector<NXTInfo> devFind();
    
    /**
     * Connect to the specified nxt device.
     * @param nxt The device to connect to
     * @return A handle to the device
     */
	abstract long devOpen(NXTInfo nxt);
    
    /**
     * Close the device. The device will no longer be available for use.
     * @param nxt The device to be closed.
     */
	abstract void devClose(long nxt);
    
    /**
     * Write bytes to the device. The call must timeout after approx 20 seconds
     * if it is not possible to write to the device.
     * @param nxt Device to write to.
     * @param message Bytes to be written.
     * @param offset Offset to start writing from.
     * @param len Number of bytes to write.
     * @return Number of bytes written, 0 if timed out < 0 if an error.
     */
	abstract int devWrite(long nxt, byte [] message, int offset, int len);
    
    /**
     * Read bytes from the device. The call must timeout after approx 20 seconds
     * if it is not possible to read from the device.
     * @param nxt Device to read from.
     * @param data Location to place the read bytes.
     * @param offset Offset of where to place the bytes.
     * @param len Number of bytes to read.
     * @return The number of bytes read, 0 if timeout < 0 if an error.
     */
	abstract int devRead(long nxt, byte[] data, int offset, int len);
    
    /**
     * Test to see if the contents of the NXTInfo structure are sufficient
     * to allow connection to the device.
     * @param nxt The device to check.
     * @return True if ok, False otherwise.
     */
    abstract boolean devIsValid(NXTInfo nxt);
    
    /**
     * Helper function to return the nth string that is part of a standard
     * double colon separated USB address. Note that first entry in a string is
     * entry 1 (not 0), -ve values may be used to access the address in reverse
     * so that the last entry is entry -1.
     * @param addr The address containing the string
     * @param loc The location of the entry.
     * @return The string at location loc or null if not found.
     */
    String getAddressString(String addr, int loc)
    {
        if (addr == null || addr.length() == 0) return null;
        int start, end;
        if (loc < 0)
        {
            end = addr.length();
            start = end;
            for(;;)
            {
                start = addr.lastIndexOf("::", end - 2) + 2;
                if (start < 2) start = 0;
                if (++loc >= 0) break;
                if (start <= 0) return null;
                end = start - 2;
            }
        }
        else
        {
            start = 0;
            end = 0;
            for(;;)
            {
               end = addr.indexOf("::", start);
               if (end < 0) end = addr.length();
               if (start > end) return null;
               if (--loc <= 0) break;
               if (end >= addr.length()) return null;
               start = end+2;
            }
        }
        if (start > end) return null;
        return addr.substring(start, end);
    }
	
    /**
     * Helper function. Open the specified nxt, get its name and close it.
     * @param dev the device to obtain the name for
     * @return the nxt name.
     */
    private String getName(NXTInfo dev)
    {
        long nxt = devOpen(dev);
        if (nxt == 0)
        	return null;
        try
        {
			byte[] request = { NXTProtocol.SYSTEM_COMMAND_REPLY, NXTProtocol.GET_DEVICE_INFO };
	        if (devWrite(nxt, request, 0, request.length) > 0)
	        {
	            int ret = devRead(nxt, inBuf, 0, 33);
	            if (ret >= 33)
	            {
	                char nameChars[] = new char[16];
	                int len = 0;
	                while (len < 15 && inBuf[len + 3] != 0) {
	                    nameChars[len] = (char) inBuf[len + 3];
	                    len++;
	                }
	                return new String(nameChars, 0, len);
	            }
	        }
	        return null;
        }
        finally
        {
        	devClose(nxt);
        }
    }

    /**
     * Helper function to make I/O simpler. Fill the input buffer when required.
     * @throws IOException
     */
    private void fillBuffer() throws IOException
    {
        inCnt = 0;
        inOffset = 0;
        int len = rawRead(inBuf, 0, inBuf.length, true);
        if (len <= 0)
        	throw new IOException("Error in read");
        inCnt = len;
    }

    /**
     * Read a single byte from the input buffer.
     * @return byte from the input buffer.
     * @throws IOException
     */
    private int readByte() throws IOException
    {
        if (inOffset >= inCnt)
        	fillBuffer();
        return inBuf[inOffset++] & 0xff;
    }

    /**
     * Helper function write the output buffer to the device.
     * @throws IOException
     */
    private void flushBuffer() throws IOException
    {
        int ret;
        if (outCnt <= 0) return;
        ret = rawWrite(outBuf, 0, outCnt, true);
        if (ret < 0 || ret != outCnt) throw new IOException("Error in write");
        outCnt = 0;
    }

    /**
     * Add a single byte to the output buffer, flush the buffer as required.
     * @param b
     * @throws IOException
     */
    private void writeByte(byte b) throws IOException
    {
        if (outCnt >= outBuf.length) flushBuffer();
        outBuf[outCnt++] = b;
    }

    /**
     * Low level access function reads and returns data from the
     * USB device with an optional timeout timeout.
     * @param buf output buffer
     * @param offset offset into the buffer
     * @param len of bytes to write
     * @param wait true if the call should block
     * @return date or null if the read times out
     */
    int rawRead(byte [] buf, int offset, int len, boolean wait) throws IOException
    {
    	if (nxtPtr == 0)
    		throw new IOException("NXTComm is closed");
    	
        int ret;
        while((ret=devRead(nxtPtr, buf, offset, len)) == 0 && wait)
            {}
        if (ret < 0) throw new IOException("Error in read");
        if (ret == 0) return 0;
        return ret;
    }
    
    /**
     * Low level access function, writes data to the USB device with an optional
     * timeout.
     * @param buf
     * @param offset
     * @param len
     * @param wait true if the call should block
     * @return number of bytes actually written
     * @throws java.io.IOException
     */
    int rawWrite(byte[] buf, int offset, int len, boolean wait) throws IOException
    {
    	if (nxtPtr == 0)
    		throw new IOException("NXTComm is closed");
    	
        int written = 0;
        while (written < len)
        {
            int ret;
            while ((ret = devWrite(nxtPtr, buf, offset + written, len - written)) == 0 && wait)
                {}
            if (ret < 0) throw new IOException("Error in write");
            if (ret == 0) return written;
            written += ret;
        }
        return written;
    }
    
    private boolean writeEOF() throws IOException
    {
        outBuf[0] = 0;
        outBuf[1] = 0;
        return (rawWrite(outBuf, 0, 2, false) == 2);
    }
        
    /**
     * Helper function, convert an array of names into an NXTInfo vector. This
     * function takes an array of standard Lego USB string addresses and converts
     * them into an nxtVector. It handles the both NXT and Samba type devices.
     * @param nxtNames an array of device address strings.
     * @return
     */
	Vector<NXTInfo> find(String[] nxtNames)
    {
        if (nxtNames == null) return new Vector<NXTInfo>();
		Vector<NXTInfo> nxtInfos = new Vector<NXTInfo>();
        for(int idx = 0; idx < nxtNames.length; idx++)
        {
            String addr = nxtNames[idx];
            NXTInfo info = new NXTInfo();
            // Use the default way to obtain the name
            info.name = null;
            info.btResourceString = addr;
            info.protocol = NXTCommFactory.USB;
            // Look to see if this is a Samba device
            if (getAddressString(addr, 2).equals(VENDOR_ATMEL) && 
                    getAddressString(addr, 3).equals(PRODUCT_SAMBA))
                info.name = SAMBA_NXT_NAME;
            info.deviceAddress = getAddressString(addr, -2);
            // if the device address is "000000000000" then it is not
            // supplying a serial number. This is either a very old version
            // of leJOS, or leJOS is not responding. Either way we ignore
            // this device.
            if (info.deviceAddress != null && !info.deviceAddress.equals("000000000000"))
                nxtInfos.addElement(info);
            else
                System.out.println("Ignoring device " + addr);
        }
        return nxtInfos;
    }


    /**
     * Locate available nxt devices and return them. Optionally filter the list
     * to those that match name.
     * @param name The name to search for. If null return all devices.
     * @return The list of devices.
     */
	public NXTInfo[] search(String name) {
		Vector<NXTInfo> nxtInfos = devFind();
        if (nxtInfos.size() == 0) return new NXTInfo[0];
        Iterator<NXTInfo> devs = nxtInfos.iterator();
        // Keep track of how many of the devices have names... We put these
        // first in the returned list
        int nameCnt = 0;
        // Filter the list against name
        while (devs.hasNext())
        {
            NXTInfo nxt = devs.next();
            if (nxt.deviceAddress == null)
                nxt.deviceAddress = "000000000000";
            if (nxt.name == null)
            {
                nxt.name = getName(nxt);
            }
            if (name != null && (nxt.name == null || !name.equals(nxt.name)))
                devs.remove();
            else
                if (nxt.name != null)
                    nameCnt++;
        }
		NXTInfo[] nxts = new NXTInfo[nxtInfos.size()];
        int named = 0;
        int unnamed = nameCnt;
        // Copy the elements over placing the ones with names first.
		for (int i = 0; i < nxts.length; i++)
        {
            NXTInfo nxt = nxtInfos.elementAt(i);
            if (nxt.name == null)
            {
                nxt.name = "Unknown";
                nxts[unnamed++] = nxt;
            }
            else
                nxts[named++] = nxt;
        }
        // Print out the list
		for (int i = 0; i < nxts.length; i++)
            SystemContext.out.println("Found NXT: " + nxts[i].name + " " + nxts[i].deviceAddress);
		return nxts;
	}

    /**
     * Open a connection to the specified device, and make it available for use.
     * @param nxtInfo The device to connect to.
     * @param mode the I/O mode to be used on this connection.
     * @return true if the device is now open, false otherwise.
     */
	public boolean open(NXTInfo nxtInfo, int mode) {
		if (nxtPtr != 0)
			//TODO throw IOExceptions/NXTCommExceptions
			throw new IllegalStateException("NXTComm is already open.");
		
        if (nxtInfo == null)
        	return false;
        
		nxtInfo.connectionState = NXTConnectionState.DISCONNECTED;
        // Is the info valid enough to connect directly?
        if (!devIsValid(nxtInfo))
        {
            // not valid so search for it.
            String addr = nxtInfo.deviceAddress;
            if (addr == null || addr.length() == 0)
                return false;
    		Vector<NXTInfo> nxtInfos = devFind();
            Iterator<NXTInfo> devs = nxtInfos.iterator();
            while (devs.hasNext())
            {
                NXTInfo nxt = devs.next();
                if (addr.equalsIgnoreCase(nxt.deviceAddress))
                {
                	//TODO caller expects, that nxtInfo.connectionState is set, however, here nxtInfo is overridden with some other temporrary object
                    nxtInfo = nxt;
                    break;
                }
            }
        }
		nxtPtr = devOpen(nxtInfo);
        if (nxtPtr == 0)
        	return false;
        
        boolean success = false;
        try
        {
	        // now the connection is open
			nxtInfo.connectionState = (mode == LCP ? NXTConnectionState.LCP_CONNECTED : NXTConnectionState.PACKET_STREAM_CONNECTED);
	        if (mode != RAW && mode != LCP)
	        {
		        // Now try and switch to packet mode for normal read/writes
				byte[] request = { NXTProtocol.SYSTEM_COMMAND_REPLY, NXTProtocol.NXJ_PACKET_MODE };
		        byte[] ret;
		        try {
		            ret = sendRequest(request, USB_BUFSZ);
		        } catch(IOException e) {
		            ret = null;
		        }
		        // Check the response. We are looking for a non standard response of
		        // 0x02, 0xfe, 0xef
		        if (ret != null && ret.length >= 3 && ret[0] == 0x02 && ret[1] == (byte)0xfe && ret[2] == (byte)0xef)
		            packetMode = true;
		        EOF = false;
	        }
	        success = true;
        }
        finally
        {
        	if (!success)
        	{
        		devClose(nxtPtr);
    	        nxtPtr = 0;
    	        nxtInfo = null;
        	}
        }
		return success;
	}
	
    public boolean open(NXTInfo nxt) throws NXTCommException
    {
        return open(nxt, PACKET);
    }

    /**
     * Close the current device.
     */
	public void close() throws IOException {
        if (nxtPtr == 0)
        	return;
        
        try
        {
	        try {
	            flushBuffer();
	            if (packetMode)
	            {
	                writeEOF();
	                while (!EOF)
	                    read();
	            }
	        }
	        catch (IOException e)
	        {
	            System.out.println("Got exception during close: " + e);
	        }
        }
        finally
        {
			devClose(nxtPtr);
	        nxtPtr = 0;
        }
	}
	
	@Override
	protected void finalize() throws Throwable
	{
		if (nxtPtr != 0)
			devClose(nxtPtr);
	}

    /**
     * Send a Lego Command Protocol (LCP) request to the device.
     * @param data The command to send.
     * @param replyLen How many bytes in the optional reply.
     * @return The optional reply, or null
     * @throws java.io.IOException Thrown on errors.
     */
    public byte[] sendRequest(byte[] data, int replyLen) throws IOException {
    	this.write(data);
        if (replyLen == 0)
        	return new byte [0];
        
        return this.read(); 
    }
	
    /**
     * Read bytes from the device
     * @return An array of bytes read from the device. null if at EOF
     * @throws java.io.IOException
     */
	public byte [] read() throws IOException {
        if (EOF)
        	return null;
        
        int len;
        if (packetMode) {
            // read header
        	int lenLSB = readByte();
        	int lenMSB = readByte();
            len = lenLSB | (lenMSB << 8);
            if (len == 0)
            {
                EOF = true;
                return null;
            }
        } else {
            if (inOffset >= inCnt)
            	fillBuffer();
            len = inCnt - inOffset;
        }
        byte[] ret = new byte[len];
        for(int i = 0; i < len; i++)
            ret[i] = (byte)readByte();
        return ret;
	}
	
    /**
     * The number of bytes that can be read without blocking.
     * @return Bytes available to be read.
     * @throws java.io.IOException
     */
	public int available() throws IOException {
		return 0;
	}
	
    /**
     * Write bytes to the device.
     * @param data Data to be written.
     * @throws java.io.IOException
     */
	public void write(byte[] data) throws IOException {
        if (packetMode)
        {
            writeByte((byte) data.length);
            writeByte((byte)(data.length >> 8));
        }
        for(int i = 0; i < data.length; i++)
            writeByte(data[i]);
        flushBuffer();
	}
    
	
	public OutputStream getOutputStream() {
		return new NXTCommOutputStream(this);		
	}
	
	public InputStream getInputStream() {
		return new NXTCommInputStream(this);		
	}

}

