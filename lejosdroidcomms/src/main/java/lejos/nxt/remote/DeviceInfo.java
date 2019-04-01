package lejos.nxt.remote;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Represents a remote NXT accessed via LCP.
 *
 */
public class DeviceInfo {
	public byte status;
	public String NXTname;
	public String bluetoothAddress;
	public int signalStrength;
	public int freeFlash;
}
