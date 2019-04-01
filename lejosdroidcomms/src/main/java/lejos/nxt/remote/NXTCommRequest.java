package lejos.nxt.remote;

import java.io.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Interface that all NXTComm implementation classes must implement for low-level communication
 * with the NXT.
 *
 */
public interface NXTCommRequest {

	/**
	 * Close the connection
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Send an LCP message to the NXT and receive a reply
	 * 
	 * @param message the LCP message
	 * @param replyLen the reply length expected
	 * @return the reply
	 * @throws IOException
	 */
	public byte[] sendRequest(byte [] message, int replyLen) throws IOException;

}


