package lejos.pc.comm;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements a sub-set of the Atmel SAM-BA download protocol. Only those
 * functions required for program download to the NXT flash are currently
 * implemented.
 *
 */
public class NXTSamba {
	
	private class MemoryInputStream extends InputStream
	{
		private int len;
		private byte[] buf;
		private int off;

		public MemoryInputStream(int len)
		{
			this.len = len;
		}
		
		private boolean fillBuffer() throws IOException
		{
			if (buf == null || off >= buf.length)
			{
				if (len <= 0)
					return false;
					
				this.buf = NXTSamba.this.read();
				this.off = 0;
				if (this.buf.length > this.len)
					throw new IOException("protocol error");
				
				this.len -= this.buf.length;
			}
			return true;
		}

		@Override
		public int read() throws IOException
		{
			if (!this.fillBuffer())
				return -1;
			
			return this.buf[this.off++] & 0xFF;
		}
		
		@Override
		public void close() throws IOException
		{
			//consume all the bytes
			while (this.fillBuffer())
				this.buf = null;
		}
	}

	private static final String CHARSET = "iso-8859-1";
	private static final String COMMAND_TERMINATOR = "#\n";
	
	private static final char CMD_GOTO = 'G';
	private static final char CMD_TEXT = 'T';	//SAM-BA sends ">" prompt
	private static final char CMD_NON_TEXT = 'N';	//SAM-BA sends no prompt or newline
	private static final char CMD_VERSION = 'V';
	private static final char CMD_READ_OCTET = 'o';
	private static final char CMD_READ_HWORD = 'h';  
	private static final char CMD_READ_WORD = 'w';
	private static final char CMD_WRITE_OCTET = 'O';
	private static final char CMD_WRITE_HWORD = 'H';  
	private static final char CMD_WRITE_WORD = 'W';  
	private static final char CMD_STREAM_READ = 'R';
	private static final char CMD_STREAM_WRITE = 'S';
    private static final byte PROMPT_CHAR = '>';

    /**
     * The NXT has 64KB RAM starting at 0x200000.
     */
    private static final int RAM_BASE  = 0x00200000;
    private static final int RAM_MAX  = 0x00210000;
    
    /**
     * The NXT has 256KB Flash starting at 0x100000, divided into 256byte pages.
     */
    public static final int FLASH_BASE = 0x00100000;
    public static final int FLASH_MAX  = 0x00140000;
    public static final int FLASH_SIZE = FLASH_MAX - FLASH_BASE;
	public static final int PAGE_SIZE  = 256;
	public static final int PAGE_MAX = FLASH_SIZE / PAGE_SIZE;
	
    
    /**
     * According to the SAM7S datasheet, section 21.6 Hardware and Software Constraints,
     * the area 0x202000-0x210000 is unused RAM. 
     */
    private static final int SAMBA_RAM_BASE = RAM_BASE + 0x2000;
    private static final int SAMBA_RAM_MAX = RAM_MAX;
    
    private static final int HELPER_STACKSIZE = 0x1000;
    private static final int HELPER_CODEADR = SAMBA_RAM_BASE + HELPER_STACKSIZE;
    private static final int HELPER_PACKET = PAGE_SIZE + 4;
    private final byte[] helper_code;
    private final int helper_dataadr;

    static
    {
    	assert SAMBA_RAM_BASE >= RAM_BASE;
    	assert SAMBA_RAM_MAX <= RAM_MAX;
    	assert HELPER_CODEADR - HELPER_STACKSIZE >= SAMBA_RAM_BASE;
    }
    
	private NXTCommUSB nxtComm = null;
    private byte [] inputBuf = new byte[NXTCommUSB.USB_BUFSZ];
    private String version;
    
    public NXTSamba() throws IOException
    {
    	this.helper_code = FlashWrite.loadCode();
    	this.helper_dataadr = HELPER_CODEADR + helper_code.length;
    	assert helper_dataadr + HELPER_PACKET <= SAMBA_RAM_MAX;
    }
    
    /**
     * Locate all NXT devices that are running in SAM-BA mode.
     * @return An array of devices in SAM-BA mode
     * @throws lejos.pc.comm.NXTCommException
     */
    public NXTInfo[] search() throws NXTCommException {
		NXTInfo[] nxtInfos;

		if (nxtComm == null) {
			try {
                nxtComm = (NXTCommUSB) NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			} catch (NXTCommException e) {
				throw e;
			}

			if (nxtComm == null) {
				throw new NXTCommException("Cannot load a comm driver");
			}
		}

		// Look for a USB one first

        nxtInfos = nxtComm.search(NXTCommUSB.SAMBA_NXT_NAME);
        if (nxtInfos.length > 0) {
            return nxtInfos;
        }
		return new NXTInfo[0];
	}
   
    /**
     * Helper function perform a read with timeout. 
     * @return Bytes read from the device.
     * @throws java.io.IOException
     */
    private byte[] read() throws IOException
    {
        int ret = nxtComm.rawRead(inputBuf, 0, inputBuf.length, false);
        if (ret == 0)
            throw new IOException("Read timeout");
        // System.out.println("Debug: "+new String(ret, CHARSET));
        byte [] newBuf = new byte[ret];
        System.arraycopy(inputBuf, 0, newBuf, 0, ret);
        return newBuf;
    }
    
    private int readAnswerWord(int len) throws IOException
    {
        byte [] ret = read();
        if (ret.length != len)
            throw new IOException("bad packet length");
        
        int r = 0;
        for (int i=len; i>0;)
        {
        	r <<= 8;
        	r |= ret[--i] & 0xFF;
        }
        return r;
    }
    
    private void readAnswerStream(byte[] data, int off, int len) throws IOException
    {
    	while (len > 0)
    	{
	        byte [] ret = read();
	        int rlen = ret.length;
	        if (rlen > len)
	            throw new IOException("bad packet length");
	        
	        System.arraycopy(ret, 0, data, off, rlen);
	        
	        off += rlen;
	        len -= rlen;
    	}    	
    }
    
    private static boolean endsWithLinefeed(byte[] ret)
    {
    	int len = ret.length;
    	return len >= 2 && (ret[len-2] == (byte)'\n' || ret[len-1] == (byte)'\r');
    }
    
    private static boolean endsWithPrompt(byte[] ret)
    {
    	int len = ret.length;
    	return len >= 1 && ret[len - 1] == PROMPT_CHAR;
    }
    
    private String readLine() throws IOException
    {
    	StringBuilder sb = new StringBuilder();
    	
    	while (true)
    	{
    		byte[] ret = read();
    		sb.append(new String(ret, CHARSET));
    		if (endsWithLinefeed(ret))
    			return sb.toString();
    	}
    }

    /**
     * Helper function perform a write with timeout.
     * @param data Data to be written to the device.
     * @throws java.io.IOException
     */
    private void write(byte[] data) throws IOException
    {
        if (nxtComm.rawWrite(data, 0, data.length, false) != data.length)
            throw new IOException("Write timeout");
    }
    
    /**
     * Helper function, send a string to the device. Convert from Unicode to
     * ASCII and send the string.
     * @param str String to be sent.
     * @throws java.io.IOException
     */
    private void writeString(String str) throws IOException
    {
		// System.out.println("Debug: "+str);
    	write(str.getBytes(CHARSET));
    }
    
    private void sendInitCommand(char cmd) throws IOException
    {
    	String command = cmd + COMMAND_TERMINATOR;
        writeString(command);
    }
    
    private void sendGotoCommand(int addr) throws IOException
    {
    	String command = CMD_GOTO + hexFormat(addr, 8) + COMMAND_TERMINATOR;
        writeString(command);
    }
    
    private void sendStreamCommand(char cmd, int addr, int len) throws IOException
    {
    	String command = cmd + hexFormat(addr, 8) + "," + hexFormat(len, 8) + COMMAND_TERMINATOR;
        writeString(command);
    }
    
    private void sendWriteCommand(char cmd, int addr, int len, int value) throws IOException
    {
    	String command = cmd + hexFormat(addr, 8) + "," + hexFormat(value, 2 * len) + COMMAND_TERMINATOR;
        writeString(command);
    }
    
    private void sendReadCommand(char cmd, int addr, int len) throws IOException
    {
        String command = cmd + hexFormat(addr, 8) + "," + len + COMMAND_TERMINATOR;
        writeString(command);
    }
    
    /**
     * Generated <b>exactly</b> as many hex digits as specified.
     */
    private static String hexFormat(int value, int len)
    {
    	char[] buf = new char[len];
    	for (int i=0; i<len; i++)
    	{
    		int shift = 4 * (len - i - 1);    		
    		int c = (value >>> shift) & 0x0F;
    		if (c < 10)
    			c += '0';
    		else
    			c += 'A' - 10;
    		
    		buf[i] = (char)c;
    	}
    	return String.valueOf(buf);
    }
    
    /**
     * Write a 8 bit octet to the specified address.
     * @param addr
     * @param val
     * @throws java.io.IOException
     */
    public void writeOctet(int addr, int val) throws IOException
    {
        sendWriteCommand(CMD_WRITE_OCTET, addr, 1, val);
    }

    /**
     * Write a 16 bit halfword to the specified address.
     * @param addr
     * @param val
     * @throws java.io.IOException
     */
    public void writeHalfword(int addr, int val) throws IOException
    {
        sendWriteCommand(CMD_WRITE_HWORD, addr, 2, val);
    }

    /**
     * Write a 32 bit word to the specified address.
     * @param addr
     * @param val
     * @throws java.io.IOException
     */
    public void writeWord(int addr, int val) throws IOException
    {
        sendWriteCommand(CMD_WRITE_WORD, addr, 4, val);
    }

    /**
     * Read a 8 bit octet from the specified address.
     * @param addr
     * @return value read from addr
     * @throws java.io.IOException
     */
    public int readOctet(int addr) throws IOException
    {
    	sendReadCommand(CMD_READ_OCTET, addr, 1);
    	return readAnswerWord(1);
    }

    /**
     * Read a 16 bit halfword from the specified address.
     * @param addr
     * @return value read from addr
     * @throws java.io.IOException
     */
    public int readHalfword(int addr) throws IOException
    {
    	sendReadCommand(CMD_READ_HWORD, addr, 2);
    	return readAnswerWord(2);
    }

    /**
     * Read a 32 bit word from the specified address.
     * @param addr
     * @return value read from addr
     * @throws java.io.IOException
     */
    public int readWord(int addr) throws IOException
    {
    	sendReadCommand(CMD_READ_WORD, addr, 4);
    	return readAnswerWord(4);
    }
    
    public InputStream createInputStream(int addr, int len) throws IOException
    {
    	sendStreamCommand(CMD_STREAM_READ, addr, len);
    	return new MemoryInputStream(len);
    }

    /**
     * Read a 32 bit word from the specified address.
     * @param addr
     * @param data the return data
     * @param off the offset
     * @param len the length
     * @throws java.io.IOException
     */
    public void readBytes(int addr, byte[] data, int off, int len) throws IOException
    {
    	sendStreamCommand(CMD_STREAM_READ, addr, len);
    	readAnswerStream(data, off, len);
    }

    /**
     * Write a series of bytes to the device.
     * @param addr
     * @param data
     * @throws java.io.IOException
     */
    public void writeBytes(int addr, byte[] data) throws IOException
    {
    	sendStreamCommand(CMD_STREAM_WRITE, addr, data.length);
        write(data);
    }

    /**
     * Start execution of code at the specified address.
     * @param addr
     * @throws java.io.IOException
     */
    public void jump(int addr) throws IOException
    {
        sendGotoCommand(addr);
    }
    
	public void reboot() throws IOException
	{
		sendGotoCommand(FLASH_BASE);
	}
	
    /**
     * Wait for the flash controller to be ready to accept commands.
     * @throws java.io.IOException
     */
    private void waitReady() throws IOException
    {
        while ((readWord(0xffffff68) & 0x1) == 0)
            Thread.yield();
    }
    
    /**
     * Change the lock bits for a region of flash memory.
     * @param rgn
     * @param lock
     * @throws java.io.IOException
     */
    private void changeLock(int rgn, boolean lock) throws IOException
    {
        int cmd = 0x5a000000 | (rgn << 14);
        if (lock)
            cmd |= 0x2;
        else
            cmd |= 0x4;
        waitReady();
        writeWord(0xffffff60, 0x00050100);
        writeWord(0xffffff64, cmd);
        writeWord(0xffffff60, 0x00340100);
    }
  
    /**
     * Turn off the lock bits for all of flash memory.
     * @throws java.io.IOException
     */
    public void unlockAllPages() throws IOException
    {
        for(int i = 0; i < 16; i++)
            changeLock(i, false);
    }

    /**
     * Write a single page to flash memory. We write the page to ram and then
     * use the FlashWriter code to transfer this data to flash. The FlashWriter
     * code must have already been downloaded.
     * @param page
     * @param data
     * @param offset
     * @throws java.io.IOException
     */
    public void writePage(int page, byte[] data, int offset) throws IOException
    {
    	this.writePage(page, data, offset, data.length - offset);
    }
    
    /**
     * Write a single page to flash memory. We write the page to ram and then
     * use the FlashWriter code to transfer this data to flash. The FlashWriter
     * code must have already been downloaded.
     * @param page
     * @param data
     * @param offset
     * @param len
     * @throws java.io.IOException
     */
    public void writePage(int page, byte[] data, int offset, int len) throws IOException
    {
    	if (page < 0 || page >= PAGE_MAX)
    		throw new IllegalArgumentException("page number out of range");
    	if (len > PAGE_SIZE)
    		len = PAGE_SIZE;
    	
        // Generate data chunk (32 bit int pagenum + 256 byte data)
        byte [] buf = new byte[HELPER_PACKET];
        System.arraycopy(data, offset, buf, 4, len);
        encodeInt(buf, 0, page);
        // And the data into ram
        writeBytes(helper_dataadr, buf);
        // And now use the flash writer to write the data into flash.
        sendGotoCommand(HELPER_CODEADR);
    }

    /**
     * Write a series of pages to flash memory.
     * @param firstPage
     * @param data
     * @param offset
     * @param len
     * @throws java.io.IOException
     */
    public void writePages(int firstPage, byte[] data, int offset, int len) throws IOException
    {
        while (len > 0)
        {
            writePage(firstPage, data, offset, len);
            offset += PAGE_SIZE;
            len -= PAGE_SIZE;
            firstPage++;
        }
    }

    /**
     * Read a single page from flash memory.
     * @param page
     * @param data
     * @param offset
     * @throws java.io.IOException
     */
    public void readPage(int page, byte[] data, int offset) throws IOException
    {
        //System.out.println("Write page " + page);
        int addr = FLASH_BASE + page * PAGE_SIZE;
        readBytes(addr, data, offset, PAGE_SIZE);
    }

    /**
     * Read a series of pages from flash memory.
     * @param first
     * @param data
     * @param start
     * @param len
     * @throws java.io.IOException
     */
    public void readPages(int first, byte[] data, int start, int len) throws IOException
    {
        int offset = start;
        int page = first;
        while (offset < start + len)
        {
            readPage(page, data, offset);
            page++;
            offset += PAGE_SIZE;
        }
    }
    
    /**
     * Open the specified USB device and check that it is in SAM-BA mode. We
     * switch the device into "quiet" mode and also download the FlashWrite
     * program.
     * @param nxt Device to open.
     * @return true if the device is now open, false otherwise.
     * @throws java.io.IOException
     */
	public boolean open(NXTInfo nxt) throws IOException
    {
		boolean success = false;
		if (nxtComm.open(nxt, NXTComm.RAW))
        {
            try
            {
                // We first set the device to interactive/text mode verbose mode
                // (which is the default) and wait for the prompt. This is safe no matter
                // whether the device originally is in interactive or quiet mode.
                // Then we switch back to quiet mode and ask for the version string.
            	// This matches the behavior of the original SAM-BA software
            	
            	// Switch into quiet mode, NXT may answer with line-feed if in verbose mode
            	sendInitCommand(CMD_TEXT);            	
            	while (!endsWithPrompt(read())) { /* wait for prompt */ }
            	
            	// Switch into quiet mode, NXT may answer with line-feed if in verbose mode
            	sendInitCommand(CMD_NON_TEXT);
            	readLine();	//discard response
            	
            	// Ask for version number, terminated by line-feed
            	sendInitCommand(CMD_VERSION);
            	// Example version string: "v1.4 Nov 10 2004 14:49:33"
            	version = readLine().trim();            	
            	// strip everything after the first whitespace 
            	version = version.replaceAll("\\s.*", "");
                
                // Now upload the flash writer helper routine
                writeBytes(HELPER_CODEADR, helper_code);
                // And set the the clock into PLL/2 mode ready for writing
                writeWord(0xfffffc30, 0x7);
                success = true;
            }
            finally
            {
            	if (!success)
            	{
	                // Unable to sync things make sure the device is closed.
	                nxtComm.close();
            	}
            }
        }
        return success;
	}
	
	private static void encodeInt(byte[] code, int off, int value)
	{
		code[off    ] = (byte)(value       );
		code[off + 1] = (byte)(value >>>  8);
		code[off + 2] = (byte)(value >>> 16);
		code[off + 3] = (byte)(value >>> 24);
	}
	
    /**
     * Close the device.
     */
    public void close() throws IOException
    {
    	nxtComm.close();
    }
   
    /**
     * returns the SAM-BA version string for the current device.
     * @return The SAM-BA version.
     * @throws java.io.IOException
     */
    public String getVersion() throws IOException
    {
        return version;
    }

}
