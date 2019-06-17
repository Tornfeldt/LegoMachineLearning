package lejos.nxt.addon;
      
import lejos.nxt.BasicMotorPort;
import lejos.util.Delay;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports a motor connected to a remote RCX via a mindsensord NRLink adapter
 * 
 * @author Lawrie Griffiths
 *
 */
public class RCXRemoteMotorPort implements BasicMotorPort {
	private RCXLink link;
	private int id;
	private boolean started = false;
	private int oldPower = -1;
	
	public RCXRemoteMotorPort(RCXLink link, int id) {
		this.link = link;
		this.id = id;
	}
	public void controlMotor(int power, int mode) {
		//LCD.drawInt(id, 0, 0);
		//LCD.drawInt(power,4,0,1);
		//LCD.drawInt(mode,0,2);
		//LCD.refresh();
		
		int power7 = (int) (power/12.5);
		
		if (power7 > 7) power7 = 7;
				
		if ((mode == 1 || mode == 2) && !started) {
			link.startMotor(id);
			started = true;
			sleep();
		} else started = false;
		
		if (power != oldPower) {
			link.setMotorPower(id, power7);
			sleep();
		}
		
		if (mode == 1) link.forward(id);
		else if (mode == 2) link.backward(id);
		else if (mode == 3) link.stopMotor(id);
		else if (mode == 4) link.fltMotor(id);
		
		oldPower = power;
	}
	
	private void sleep() {
        Delay.msDelay(50);
	}
	
	public void setPWMMode(int mode) {
	}
}
