package lejos.nxt.remote;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Firmware information for a remote NXT accessed via LCP.
 *
 */
public class FirmwareInfo {
	public byte status;
	public String protocolVersion;
	public String firmwareVersion;
}