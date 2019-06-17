package lejos.nxt.remote;

import java.io.*;

import lejos.robotics.DCMotor;
import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Motor class. Contains three instances of Motor.
 * Usage: Motor.A.forward(500);
 *  
 * @author <a href="mailto:bbagnall@mts.net">Brian Bagnall</a>
 *
 */
public class RemoteMotor implements RegulatedMotor, DCMotor, NXTProtocol {
	
	private int id;
	private byte power;
	private int mode;
	private int regulationMode;
	public byte turnRatio;
	private int runState;	
	private boolean _rotating = false;
	private NXTCommand nxtCommand;
	private RemoteBattery battery;
    protected RegulatedMotorListener listener = null;
	
	public RemoteMotor(NXTCommand nxtCommand, int id) {
		this.id = id;
		this.power = 80; // 80% power by default. Is this speed too?
		this.mode = BRAKE + REGULATED; // Brake mode and regulation default
		this.regulationMode = REGULATION_MODE_MOTOR_SPEED;
		this.turnRatio = 0; // 0 = even power/speed distro between motors
		this.runState = MOTOR_RUN_STATE_IDLE;
		this.nxtCommand = nxtCommand;
		battery = new RemoteBattery(nxtCommand);
	}
	
	/**
	* Get the ID of the motor. One of 'A', 'B' or 'C'.
	*/
	public final char getId() {
		
		char port = 'A';
		switch(id) {
			case 0:
				port='A';
				break;
			case 1:
				port='B';
				break;
			case 2:
				port='C';
				break;	
		}
		return port;
	}

	public void forward() {
		this.runState = MOTOR_RUN_STATE_RUNNING;
		try {
			nxtCommand.setOutputState(id, power, this.mode + MOTORON, regulationMode, turnRatio, runState, 0);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			//return -1;
		}
	}
	
	public void backward() {
		this.runState = MOTOR_RUN_STATE_RUNNING;
		try {
			nxtCommand.setOutputState(id, (byte)-power, this.mode + MOTORON, regulationMode, turnRatio, runState, 0);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			//return -1;
		}
	}
	
	public void setSpeed(int speed) {
		
		if(speed > 900 || speed < 0)
			return;
		speed = (speed * 100) / 900;
		this.power = (byte)speed;
	}
	
	/**
	 * Sets the power of the motor
	 * @param power the power (-100 to +100)  
	 */
	public void setPower(int power) {
		this.power = (byte)power;
	}
	
	public int getSpeed() {
		return (this.power * 900) / 100;
	}
	
	/**
	 * Return the power that the motor is set to
	 * @return the power (-100 to +100)
	 */
	public int getPower() {
		return power;
	}
	
	public int getTachoCount() {
		try {
			OutputState state = nxtCommand.getOutputState(id);
			return state.rotationCount;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
	}
	
	/**
	 * Returns the rotation count for the motor. The rotation count is something
	 * like the trip odometer on your car.  This count is reset each time a new function
	 * is called in Pilot.
	 * @deprecated
	 * @return rotation count.
	 */
    @Deprecated
	public int getRotationCount() {
		// !! Consider making this protected to keep off limits from users.
		return getTachoCount();
	}
	
	/**
	 * Block Encoder Count is the count used to synchronize motors
	 * with one another. 
	 * NOTE: If you are using leJOS NXJ firmware this will
	 * always return 0 because this variable is not used in 
	 * in leJOS NXJ firmware. Use getRotationCount() instead.
	 * @deprecated
	 * @return Block Encoder count.
	 */
    @Deprecated
	public int getBlockTacho() {
		try {
			OutputState state = nxtCommand.getOutputState(id);
			return state.blockTachoCount;
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return 0;
		}	
	}
	
	public void rotate(int count, boolean returnNow) {
		this.runState = MOTOR_RUN_STATE_RUNNING;
		// ** Really this can accept a ULONG value for count. Too lazy to properly convert right now:
		// !! This used to say power > 0, apparently not working.
		//if(power > 0)
		// We must not attempt to perform a rotation of zero degrees. The LCP implementation uses
		// none zero values to indicate that a limit is required. Sending 0 results in the motor
		// running forever!
		if (count == 0)
		    return;
		try {
			if(count > 0)
				nxtCommand.setOutputState(id, power, this.mode + MOTORON, regulationMode, turnRatio, runState, count); // Note using tachoLimit with Lego FW
			else
				nxtCommand.setOutputState(id, (byte)-power, this.mode + MOTORON, regulationMode, turnRatio, runState, Math.abs(count)); // Note using tachoLimit with Lego FW			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
		if(!returnNow) {
			// Check if mode is moving until done
			waitComplete();
		}
	}
	
	public boolean isMoving() {
		try {
			OutputState o = nxtCommand.getOutputState(id);
			// return ((MOTORON & o.mode) == MOTORON);
			return o.runState != MOTOR_RUN_STATE_IDLE; // Peter's bug fix
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return false;
		}
	}

    public void waitComplete()
    {
        while (isMoving())
            Thread.yield();
        if (listener != null) listener.rotationStopped(this, getTachoCount(), false, System.currentTimeMillis());
    }
	
	/**
	 * CURRENTLY NOT IMPLEMENTED! Use isMoving() for now.
	   *returns true when motor is rotating toward a specified angle
	   */ 
	  public boolean isRotating()
	  {
		  // Should probably use Tacho Limit value from
		  // get output state
	  	return  _rotating;
	  }
	
	public void rotate(int count) {
		rotate(count, false);
	}
	
	/**
	 * This method determines if and how the motor will be regulated.
	 * REGULATION_MODE_IDLE turns off regulation
	 * REGULATION_MODE_MOTOR_SPEED regulates the speed (I think)
	 * REGULATION_MODE_MOTOR_SYNC synchronizes this and any other motor with SYNC enabled.
	 * @param mode See NXTProtocol for enumerations: REGULATION_MODE_MOTOR_SYNC, 
	 *  REGULATION_MODE_MOTOR_SPEED,  REGULATION_MODE_IDLE
	 */
	public void setRegulationMode(int mode) {
		// !! Consider removing this method! No need, confusing, makes other forward methods unreliable.
		this.regulationMode = mode;
	}
	
	public void rotateTo(int limitAngle) {
		rotateTo(limitAngle, false);
		
	}
	
	public void rotateTo(int limitAngle, boolean returnNow) {
		// !! Probably inaccuracy can creep into this if
		// rotateTo is called while motor moving.
		int tachometer = this.getTachoCount();
		rotate(limitAngle - tachometer, returnNow);
	}
	
    /**
     * RegulatedMotor for  NXT need this
     * @return the limit angle
     */
    public int getLimitAngle()
    {
    	return 0;
    }
        
	public void resetTachoCount() {
		try {
			nxtCommand.resetMotorPosition(this.id, false);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			//return -1;
		}
	}
	
	/**
	 * Resets the block tachometer.
	 * NOTE: If you are using leJOS NXJ firmware this will not do anything
	 * because BlockTacho is not used in the leJOS NXJ firmware.
	 * Use resetRotationCounter() instead.
	 * @deprecated
	 * @return Error value. 0 means success. See lejos.pc.comm.ErrorMessages for details.
	 */
    @Deprecated
	public int resetBlockTacho() {
		// Note: This method can also reset tachometer relative to last position.
		// I didn't include this because it seems unintuitive, but the 
		// functionality could be added, maybe with a resetTachoRelative() method.
		// Just change false to true in statement below for relative reset.
		// @param relative TRUE: position relative to last movement, FALSE: absolute position
		 
		try {
			nxtCommand.resetMotorPosition(this.id, true);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			return -1;
		}
		return 0;
	}
	public void stop(boolean returnNow) {
		this.runState = MOTOR_RUN_STATE_RUNNING;
		//this.regulationMode = REGULATION_MODE_MOTOR_SPEED;
		try {
			// NOTE: Setting power to 0 seems to make it lock motor, not float it.
			nxtCommand.setOutputState(id, (byte)0, BRAKE + MOTORON + REGULATED, regulationMode, turnRatio, runState, 0);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
			//return -1;
		}
        if (!returnNow)
            waitComplete();
	}

	
	public void stop() {
        stop(true);
	}
	
	public void flt(boolean returnNow) {
		this.runState = MOTOR_RUN_STATE_IDLE;
		//this.regulationMode = REGULATION_MODE_MOTOR_SPEED;
		this.mode = MOTOR_RUN_STATE_IDLE;
		try {
			nxtCommand.setOutputState(id, (byte)0, 0x00, regulationMode, turnRatio, runState, 0);
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
        if (!returnNow)
            waitComplete();
	}

	public void flt() {
        flt(true);
    }

	public void regulateSpeed(boolean yes) {
		// TODO Currently a dummy for remote motors.
	}
	
	public void smoothAcceleration(boolean yes) {
		// TODO Currently a dummy for remote motors.
	}
	
	public int getRotationSpeed()	{
		// TODO Currently dummy for remote motors - returns the speed that has been set.
	     return getSpeed();
	}

	/**
	 * Currently not completely implemented in RemoteMotor!
	 */
	public void addListener(RegulatedMotorListener listener) {
		this.listener = listener;
		// TODO: Currently not completely implemented in RemoteMotor!
	}
	
	public RegulatedMotorListener removeListener() {
		RegulatedMotorListener old = this.listener;
		this.listener = null;
		return old;
	}
	
    public boolean isStalled()
    {
      return false;
      // TODO can the stall detection be implemented in this class?
      // in the real Motor, the regulator does it;
    }
	
	public void setAcceleration(int acceleration){};
	
	public float getMaxSpeed() {
	    // It is generally assumed, that the maximum accurate speed of Motor is
	    // 100 degree/second * Voltage
		return battery.getVoltage() * 100.0f;
	}

	public void setStallThreshold(int error, int time) {
		// TODO Auto-generated method stub
		
	}
}