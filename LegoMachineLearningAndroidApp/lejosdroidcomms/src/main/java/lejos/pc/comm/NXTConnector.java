package lejos.pc.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Connects to a NXT using Bluetooth or USB (or either) and supplies input and output
 * data streams.
 * 
 * @author Lawrie Griffiths and Roger Glassey
 */
public class NXTConnector extends NXTCommLoggable
{
	private InputStream input;
	private OutputStream output;
	private NXTInfo nxtInfo;
	private NXTComm nxtComm;
	private boolean debugOn = false;
    
	/**
	 * Connect to any NXT over any protocol in <code>NXTComm.PACKET</code> mode
	 * 
	 * @return <code>true</code> if the connection succeeded
     * @see NXTComm#PACKET
	 */
    public boolean connectTo() {
    	return connectTo(null, null, NXTCommFactory.ALL_PROTOCOLS, NXTComm.PACKET);
    }
    
	/**
	 * Connect to any NXT over any protocol specifying mode
	 * @param mode the NXTComm mode (<code>{@link NXTComm#PACKET}</code>, <code>{@link NXTComm#LCP}</code>, or 
     * <code>{@link NXTComm#RAW}</code>)
	 * 
	 * @return <code>true</code> if the connection succeeded
	 */
    public boolean connectTo(int mode) {
    	return connectTo(null, null, NXTCommFactory.ALL_PROTOCOLS, mode);
    }
    
    /**
     * Connect to a specified NXT in packet mode
     * 
     * @param nxt the name of the NXT to connect to or <code>null</code> for any
     * @param addr the address of the NXT to connect to or <code>null</code>
     * @param protocols the protocols to use: <code>{@link NXTCommFactory#ALL_PROTOCOLS}</code>,
     * <code>{@link NXTCommFactory#BLUETOOTH}</code>, or <code>{@link NXTCommFactory#USB}</code>
     * @return <code>true</code> if the connection succeeded
     */
    public boolean connectTo(String nxt, String addr, int protocols) {
    	return connectTo(nxt, addr, protocols, NXTComm.PACKET);
    }
    /**
     * Search for one or more NXTs and return an array of <code>NXTInfo</code> instances for those found.
     * 
     * @param nxt the name of the NXT to connect to or <code>null</code> for any
     * @param addr the address of the NXT to connect to or <code>null</code> 
     * @param protocols the protocols to use: <code>{@link NXTCommFactory#ALL_PROTOCOLS}</code>,
     * <code>{@link NXTCommFactory#BLUETOOTH}</code>, or <code>{@link NXTCommFactory#USB}</code>
     * @return an array of <code>NXTInfo</code> instances for found NXTs
     */
	public NXTInfo[] search(String nxt, String addr, int protocols)
	{
		String name = (nxt == null || nxt.length() == 0 ? nxt: "Unknown");
		String searchParam = (nxt == null || nxt.length() == 0 || nxt.equals("*") ? null : nxt);
		String searchFor = (nxt == null || nxt.length() == 0 ? "any NXT" : nxt);
       	Properties props = null;
       	
       	// reset the relevant instance variables
		NXTInfo[] nxtInfos = new NXTInfo[0];

		debug("Protocols = " + protocols);
		debug("Search Param = " + searchParam);
		
		// Try USB first
		if ((protocols & NXTCommFactory.USB) != 0) {
			NXTComm nxtComm;
			try {
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			} catch (NXTCommException e) {
				nxtComm = null;
				logException("Error: Failed to load USB comms driver.", e);
			}
			if (addr != null && addr.length() > 0) {
				log("Using USB device with address = " + addr);
				nxtInfos = new NXTInfo[1];
				nxtInfos[0] = new NXTInfo(NXTCommFactory.USB, name, addr);
			} else if (nxtComm != null){
				debug("Searching for " + searchFor + " using USB");
				try {
					nxtInfos = nxtComm.search(searchParam);
					if (nxtInfos.length == 0) 
						debug((searchParam == null ? "No NXT found using USB: " : (searchParam + " not found using USB: ")) +  "Is the NXT switched on and the USB cable connected?");
				} catch (NXTCommException ex) {
					logException("Error: Search failed.", ex);
				}
			}
		}
		
		if (nxtInfos.length > 0) return nxtInfos;
		
		// If nothing found on USB, try Bluetooth		
		if ((protocols & NXTCommFactory.BLUETOOTH) != 0) {
			NXTComm nxtComm;
			// Load Bluetooth driver
			try {
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			} catch (NXTCommException e) {
				logException("Error: Failed to load Bluetooth comms driver.", e);
				return nxtInfos;
			}
			
			// If address specified, connect by address
			if (addr != null && addr.length() > 0) {
				log("Using Bluetooth device with address = " + addr);
				nxtInfos = new NXTInfo[1];
				nxtInfos[0] = new NXTInfo(NXTCommFactory.BLUETOOTH, name, addr);
				return nxtInfos;
			}
			
			// Get known NXT names and addresses from the properties file
			try {	       	
				props = NXTCommFactory.getNXJCache();
								
				// Create an array of NXTInfos from the properties
				if (props.size() > 0 && !(nxt != null && nxt.equals("*"))) {	
					HashMap<String,String> nxtNames = new HashMap<String,String>();
					
					debug("Searching cache file for known Bluetooth devices");
					
					// Populate hashTable from NXT_<addr>=<name> entries, filtering by name, if supplied
					for (Map.Entry<?, ?> e : props.entrySet()) {
				        // Get property name						
				        String propName = (String)e.getKey();
				        
				        if (propName.startsWith("NXT_")) {
				        	String nxtAddr = propName.substring(4);
				        	String nxtName = (String)e.getValue();
					        
				        	if (isAddress(nxtAddr) && (searchParam == null || nxtName.equals(nxt))) {
				        		debug("Found " + nxtName + " " + nxtAddr + " in cache file");
				        		nxtNames.put(nxtAddr, nxtName);
				        	}				        	
				        }				    
				    }
				    
				    debug("Found " + nxtNames.size() + " matching NXTs in cache file");
				    
				    // If any found, create the NXTInfo array from the hashtable
				    if (nxtNames.size() > 0) {					    
						nxtInfos = new NXTInfo[nxtNames.size()];
						
						int i=0;
					    for (Map.Entry<String, String> e : nxtNames.entrySet()) {
					    	nxtInfos[i++] = new NXTInfo(NXTCommFactory.BLUETOOTH, e.getValue(), e.getKey());			    							
					    }				    	
				    }
				} else {
					debug("No NXTs found in cache file");
				}
			} catch (NXTCommException ex) {
				log("Failed to load cache file");
			}
		
			// If none found, do a Bluetooth inquiry
			if (nxtInfos.length == 0) {
				log("Searching for " + searchFor + " using Bluetooth inquiry");
				try {
					nxtInfos = nxtComm.search(searchParam);
				} catch (NXTCommException ex) { 
					logException("Error: Search failed.", ex);
				}
				
				debug("Inquiry found " + nxtInfos.length + " NXTs");
				
				// Save the results in the properties file
				for(int i=0;i<nxtInfos.length;i++) {
					log("Name " + i + " = " + nxtInfos[i].name);
					log("Address " + i + " = " + nxtInfos[i].deviceAddress);
					props.put("NXT_" + nxtInfos[i].deviceAddress, nxtInfos[i].name);
				}
				
				debug("Saving cached names");
				try {
					NXTCommFactory.saveNXJCache(props,"Results from Bluetooth inquiry");
				} catch (IOException ex) {
					logException("Error: Failed to write cache file.", ex);
				}
			}
		}
		
		// If nothing found, log a message
		if (nxtInfos.length == 0) {
			log("Failed to find any NXTs");
		}
	
		return nxtInfos;
	}
	
    private void logException(String message, Throwable e)
    {
    	log(message);
    	while (e != null)
    	{
    		log("Caused by "+e.toString());
    		StackTraceElement[] st = e.getStackTrace();
    		if (st != null && st.length > 0)
    			log("\tat "+e.getStackTrace()[0]);
    		e = e.getCause();
    	}
	}

	/**
     * Connect to a NXT
     * 
     * @param nxt the name of the NXT to connect to or <code>null</code> for any
     * @param addr the address of the NXT to connect to or <code>null</code> 
     * @param protocols the protocols to use: <code> {@link NXTCommFactory#ALL_PROTOCOLS}</code>,
     * <code> {@link NXTCommFactory#BLUETOOTH}</code>, or <code> {@link NXTCommFactory#USB}</code>
     * @param mode the NXTComm mode (<code>{@link NXTComm#PACKET}</code>, <code>{@link NXTComm#LCP}</code>, or 
     * <code>{@link NXTComm#RAW}</code>)
     * @return <code>true</code> if the connection succeeded
     */
	public boolean connectTo(String nxt, String addr, int protocols, int mode)
	{
		if (this.nxtComm != null)
			//TODO throw NXTCommException instead
			throw new IllegalStateException("already connected");
		
		// Search for matching NXTs
		NXTInfo[] nxtInfos = search(nxt, addr, protocols);
		
		// Try each available NXT in turn
		for(int i=0;i<nxtInfos.length;i++) {
			if (this.connectTo(nxtInfos[i], mode)) {
				return true;
			}
		}

		log("Failed to connect to any NXT");
		return false;
	}
	
	/**
	 * Connect to a NXT using a <code>NXTInfo</code> instance
	 * @param nxtInfo
	 * @param mode
	 * @return <code>true</code> if the connection succeeded
	 */
	public boolean connectTo(NXTInfo nxtInfo, int mode) {
		if (this.nxtComm != null)
			//TODO throw NXTCommException instead
			throw new IllegalStateException("already connected");
		
		NXTComm nxtComm;
		if (nxtInfo.protocol == NXTCommFactory.USB ) {
			try {
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.USB);
			} catch (NXTCommException e) {
				logException("Error: Failed to load USB comms driver.", e);
				return false;
			}
		} else if (nxtInfo.protocol == NXTCommFactory.BLUETOOTH ) {
			try {
				nxtComm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			} catch (NXTCommException e) {
				logException("Error: Failed to load Bluetooth comms driver.", e);
				return false;
			}
		} else {
			throw new IllegalArgumentException("unknown protocol");
		}
		
		try {
			boolean success = false;
			boolean opened = nxtComm.open(nxtInfo, mode);
			if (!opened) {
				log("Failed to connect to the specified NXT");
				return false;
			}
			try
			{
				this.input = nxtComm.getInputStream();
				this.output = nxtComm.getOutputStream();
				this.nxtInfo = nxtInfo;
				this.nxtComm = nxtComm;
				success = true;
				return true;
			}
			finally
			{
				if (!success)
				{
					nxtComm.close();
					this.nxtComm = null;
					this.nxtInfo = null;
					this.output = null;
					this.input = null;
				}
			}
		} catch (NXTCommException e) {
			logException("Error: Exception connecting to NXT.", e);
			return false;
		} catch (IOException e) {
			logException("Error: Exception connecting to NXT.", e);
			return false;
		}
	}

	/**
	 * Connect to a device by URL
	 * @param deviceURL i.e. <code>btspp://[name], usb://[name]</code>
	 * @param mode the mode (<code>{@link NXTComm#PACKET}</code>, <code>{@link NXTComm#LCP}</code>, or 
     * <code>{@link NXTComm#RAW}</code>)
	 * @return <code>true</code> if the connection succeeded
	 */
	public boolean connectTo(String deviceURL, int mode) {
		String protocolString = "";
		int colonIndex = deviceURL.indexOf(':');
		if (colonIndex >= 0) {
			protocolString = deviceURL.substring(0,colonIndex);
		}
		String addr = null;
		String name = null;
		
		int protocols = NXTCommFactory.ALL_PROTOCOLS;
		if (protocolString.equals("btspp")) protocols = NXTCommFactory.BLUETOOTH;
		if (protocolString.equals("usb")) protocols = NXTCommFactory.USB;
		
		if (colonIndex >= 0) colonIndex +=2; // Skip "//"
		
		String nameString = deviceURL.substring(colonIndex+1);		
		boolean isAddress = isAddress(nameString);
		
		if (isAddress) {
			addr = nameString;
			name = "Unknown";
		} else {
			name = nameString;
			addr = null;
		}
		
		return connectTo(name, addr, protocols, mode);
	}
	
	private boolean isAddress(String s)
	{
		return s != null && s.startsWith("00");
	}
	
	/**
	 * Connect to a device by URL in <code>NXTComm.PACKET</code> mode
	 * @param deviceURL i.e. <code>btspp://[name], usb://[name]</code>
	 * @return <code>true</code> if the connection succeeded
     * @see NXTComm#PACKET
	 */
	public boolean connectTo(String deviceURL) {
		return connectTo(deviceURL, NXTComm.PACKET);
	}
	
	/**
	 * @return the <code>InputStream</code> for this connection;
	 */
	public InputStream getInputStream() {return input;}
    
	/**
	 * @return the <code>OutputStream</code> for this connection;
	 */
	public OutputStream getOutputStream() {return output;}
 
	/**
	 * @return the <code>NXTInfo</code> for this connection
	 */   
	public  NXTInfo getNXTInfo () {return nxtInfo;}
	
	/**
	 * @return the <code>NXTComm</code> for this connection
	 */   
	public  NXTComm getNXTComm () {return nxtComm;}
	
	/**
	 * Close the connection
	 *
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (this.nxtComm != null)
		{
			this.nxtComm.close();
			this.nxtComm = null;
			this.nxtInfo = null;
			this.output = null;
			this.input = null;
		}
	}
	
	private void debug(String msg) {
		if (debugOn) log(msg);
	}
	
	/**
	 * Set debugging on or off
	 * 
	 * @param debug <code>true</code> for on, <code>false</code> for off
	 */
	public void setDebug(boolean debug) {
		debugOn = debug;
	}
}
