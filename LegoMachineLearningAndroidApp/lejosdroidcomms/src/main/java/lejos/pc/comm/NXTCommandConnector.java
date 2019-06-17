package lejos.pc.comm;

import java.io.IOException;
import lejos.nxt.remote.*;

/**
 * Used by remote execution leJOS API classes to create a connection to a 
 * NXTCommand (LCP) connection to the NXT.
 * 
 * @author Lawrie Griffiths
 *
 */
public class NXTCommandConnector {
	private static NXTConnector conn = new NXTConnector();
	private static NXTCommand currentNXTCommand = null;
	
	/**
	 * Open any available NXT.
	 * 
	 * @return true if connected
	 */
	public static NXTComm open() throws IOException {
		boolean connected = conn.connectTo(NXTComm.LCP);
		
		return (connected ? conn.getNXTComm() : null);
	}
	
	/**
	 * Ensure that the singleton NXTCommand object has been opened
	 * and return it.
	 * 
	 * Used by leJOS API remote execution classes. 
	 * A message is sent to System.err and the program is exited if the
	 * open fails.
	 * 
	 * @return the singleton NXTCommand instance
	 */
	public static NXTCommand getSingletonOpen() {
		if (currentNXTCommand == null) {
			try {
				NXTComm nxtComm = open();
				if (nxtComm == null) throw new IOException();
				currentNXTCommand = new NXTCommand(nxtComm);
			} catch (IOException ioe) {
				System.err.println("Failed to open connection to the NXT");
				System.exit(1);
			}
		}
		return currentNXTCommand;
	}
	
	public static void close() throws IOException {
		if (currentNXTCommand != null) currentNXTCommand.close();
	}

	public static void setNXTCommand(NXTCommand nxtCommand) {
		currentNXTCommand = nxtCommand;		
	}
}
