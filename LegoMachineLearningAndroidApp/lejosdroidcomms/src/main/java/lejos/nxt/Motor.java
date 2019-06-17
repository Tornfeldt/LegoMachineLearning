package lejos.nxt;

import lejos.pc.comm.*;
import lejos.nxt.remote.*;

/**
 * Motor class. Contains three instances of Motor.
 * Usage: Motor.A.forward(5000);
 * 
 * This version of the Motor class supports remote execution.
 *  
 *
 */
public class Motor {
	private static final NXTCommand nxtCommand = NXTCommandConnector.getSingletonOpen();
	
	/**
	 * Motor A.
	 */
	public static final RemoteMotor A = new RemoteMotor (nxtCommand, 0);
	/**
	 * Motor B.
	 */
	public static final RemoteMotor B = new RemoteMotor (nxtCommand, 1);
	/**
	 * Motor C.
	 */
	public static final RemoteMotor C = new RemoteMotor (nxtCommand, 2);
	
    private Motor() {
    	// Motor class cannot be instantiated
    }
}