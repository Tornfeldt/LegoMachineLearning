package lejos.nxt;

import lejos.nxt.remote.*;
import lejos.pc.comm.NXTCommandConnector;

/**
 * This version of the MotorPort class supports a motor connected to a remote NXT
 * 
 * @author Lawrie Griffiths
 *
 */
public class MotorPort implements NXTProtocol, TachoMotorPort
{
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
	private RemoteMotorPort rmp;
	
	public MotorPort(int id) {
		rmp = new RemoteMotorPort(nxtCommand, id);
	}
	
	public static MotorPort A = new MotorPort(0);
	public static MotorPort B = new MotorPort(1);
	public static MotorPort C = new MotorPort(2);
	
	public void controlMotor(int power, int mode)
	{
		rmp.controlMotor(power, mode);
	}
	
	public  int getTachoCount()
	{
		return rmp.getTachoCount();
	}
	
	public void resetTachoCount()
	{
		rmp.resetTachoCount();
	}
	
	public void setPWMMode(int mode) {
		rmp.setPWMMode(mode);
	}
}

