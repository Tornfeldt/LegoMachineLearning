package lejos.pc.comm;

import java.io.IOException;
import java.util.ArrayList;
import lejos.nxt.remote.*;


public class NXTConnectionManager {

	private ArrayList<NXTCommand> nxtCommands;
	private NXTConnector nxtConnector;

	public NXTConnectionManager() {
		nxtConnector = new NXTConnector();
		nxtCommands = new ArrayList<NXTCommand>();
	}

	/**
	 * searches for available NXT bricks automatically connects to the first one
	 * found (TODO: correct?)
	 * 
	 * @return an array of available NXTInfo objects or null, if no brick is
	 *         found
	 */
	public NXTInfo[] search() {
		return nxtConnector.search(null,null,NXTCommFactory.ALL_PROTOCOLS);
	}

	/**
	 * connects to a NXT brick
	 * 
	 * @param nxtInfo
	 * @return true, if connection was successful
	 */
	public boolean connectToBrick(NXTInfo nxtInfo) {
		boolean brickConnected = false;
		if (nxtInfo != null)
			brickConnected = nxtConnector.connectTo(nxtInfo, NXTComm.LCP);
		return brickConnected;
	}

	public void closeAll() throws IOException {
		for (NXTCommand nxtCommand : nxtCommands) {
			nxtCommand.close();
		}
	}

	/**
	 * register log listener
	 * 
	 * @param listener
	 *            the log listener
	 */
	public void addLogListener(NXTCommLogListener listener) {
		nxtConnector.addLogListener(listener);
	}

	/**
	 * unregister log listener
	 * 
	 * @param listener
	 *            the log listener
	 */
	public void removeLogListener(NXTCommLogListener listener) {
		nxtConnector.removeLogListener(listener);
	}

	/*
	 * public NXTInfo connectedNXT;
	 * 
	 * public NXTInfo getConnectedNXT() { return connectedNXT; }
	 * 
	 * public NXTInfo[] searchForNXTBricks() { // disconnect NXTCommand
	 * nxtCommand = NXTCommand.getSingleton(); try { nxtCommand.close();
	 * connectedNXT = null; } catch (IOException ioe) { LeJOSNXJUtil.message(
	 * "something went wrong when trying to close the connection to NXT bricks"
	 * + ioe.getMessage()); } // search for bricks NXTInfo[] nxtBricks = null;
	 * NXTInfo[] nxtUSBBricks = null; NXTInfo[] nxtBluetoothBricks = null; try {
	 * nxtUSBBricks = nxtCommand.search(null, NXTCommFactory.USB); } catch
	 * (NXTCommException nce) { LeJOSNXJUtil
	 * .message("something went wrong when searching for NXT bricks via USB: " +
	 * nce.getMessage()); } try { nxtBluetoothBricks = nxtCommand.search(null,
	 * NXTCommFactory.BLUETOOTH); } catch (NXTCommException nce) { LeJOSNXJUtil
	 * .
	 * message("something went wrong when searching for NXT bricks via Bluetooth: "
	 * + nce.getMessage()); } int noOfUSBBricksFound = 0; if (nxtUSBBricks !=
	 * null) { noOfUSBBricksFound = nxtUSBBricks.length; } int
	 * noOfBluetoothBricksFound = 0; if (nxtBluetoothBricks != null) {
	 * noOfBluetoothBricksFound = nxtBluetoothBricks.length; } int
	 * noOfBricksFound = noOfUSBBricksFound + noOfBluetoothBricksFound; if
	 * (noOfBricksFound > 0) { nxtBricks = new NXTInfo[noOfBricksFound]; int i =
	 * 0; for (int j = 0; j < noOfUSBBricksFound; j++) { nxtBricks[i++] = new
	 * NXTInfo(nxtUSBBricks[j]); } for (int j = 0; j < noOfBluetoothBricksFound;
	 * j++) { nxtBricks[i++] = new NXTInfo(nxtBluetoothBricks[j]); } } return
	 * nxtBricks; }
	 * 
	 * public boolean connectToBrick(NXTInfo browserInfo) { boolean
	 * brickConnected = false; try { brickConnected =
	 * NXTCommand.getSingleton().open(browserInfo); } catch (Throwable t) {
	 * LeJOSNXJUtil.message(t); } if(brickConnected) connectedNXT = browserInfo;
	 * return brickConnected; }
	 * 
	 * public void detachFromBricks() { try { NXTCommand.getSingleton().close();
	 * connectedNXT = null; } catch (Throwable t) { LeJOSNXJUtil.message(t); } }
	 */
}
