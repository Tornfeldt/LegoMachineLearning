package lejos.nxt.addon;

import lejos.nxt.*;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * MotorPort for PF Motors using HiTechnic IRLink
 * 
 * @author Lawrie Griffiths
 *
 */
public class PFMotorPort implements BasicMotorPort {
	private int channel, slot;
	private IRLink link;
	private static final int[] modeTranslation = {1,2,3,0};
	
	public PFMotorPort(IRLink link, int channel, int slot) {
		this.channel = channel;
		this.slot = slot;
		this.link = link;
	}
	
	public void controlMotor(int power, int mode) {
		if (mode < 1 || mode > 4) return;
		link.sendPFComboDirect(channel, (slot == 0 ? modeTranslation[mode-1] : 0), (slot == 1 ? modeTranslation[mode-1] : 0));
	}

	public void setPWMMode(int mode) {
		// Not implemented
	}
}
