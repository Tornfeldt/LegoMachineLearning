package lejos.pc.comm;

/**
 * Structure containing information about a specific NXT
 * and the potential connections to it. 
 * 
 * Returned from the <code>{@link NXTComm#search}</code> method.
 * @see NXTComm
 */
public class NXTInfo {
	
	/**
	 * Friendly name of the NXT.
	 */
	public String name;
	
	/**
	 * The device address.
	 */
	public String deviceAddress;
	
	/**
	 * A string used to locate the NXT. Dependent on
	 * the version of NXTComm used.
	 */
	String btResourceString;
	
	/**
	 * The protocol used to connect to the NXT: USB or BLUETOOTH.
	 */
	public int protocol = 0;

	/**
	 * the present connection state of the NXT
	 */
	public NXTConnectionState connectionState = NXTConnectionState.UNKNOWN;
	
	public NXTInfo() {}
	
	/**
	 * Create a <code>NXTInfo</code> that is used to connect to 
	 * a NXT via Bluetooth using the Bluetooth address.
	 * 
	 * @param protocol the protocol to use: <code>{@link NXTCommFactory#USB}</code> or <code>{@link NXTCommFactory#BLUETOOTH}</code>
	 * @param name the name of the NXT
	 * @param address the Bluetooth address with optional colons between hex pairs.
	 */	
	public NXTInfo(int protocol, String name, String address) {
		this.name = name;
		this.deviceAddress = address;
		this.protocol = protocol;
	}

	//TODO remove connectionState, it doesn't belong here
	
}
