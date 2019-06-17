package lejos.nxt.addon;

import lejos.nxt.addon.NXTMMX;

import lejos.robotics.RegulatedMotor;
import lejos.robotics.RegulatedMotorListener;

import lejos.util.Delay;


/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction to drive a regulated encoder motor with the NXTMMX motor multiplexer. 
 * The 
 * NXTMMX motor multiplexer device allows you to connect two 
 * additional motors to your robot using a sensor port. Multiple NXTMMXs can be chained together.
 * <p>
 * Create an instance of this class passing a <code>NXTMMX</code> instance and Motor ID 
 * (<code>{@link NXTMMX#MMX_MOTOR_1 MMX_MOTOR_1}</code> or <code>{@link NXTMMX#MMX_MOTOR_2 MMX_MOTOR_2}</code>)
 * in the constructor.
 * 
 * @see NXTMMX
 * @author Michael D. Smith &lt;mdsmitty@gmail.com&gt;
 * @author Kirk P. Thompson  
 *
 */
public class MMXRegulatedMotor extends MMXMotor implements RegulatedMotor{
    // masks for the command register: REG_MotorCommandRegAB  
    private static final int CONTROL_RELATIVE =    0x04; //2
    private static final int CONTROL_TACHO =       0x08; //3
    private static final int CONTROL_TACHO_BRAKE = 0x10; //4
    private static final int CONTROL_TACHO_LOCK =  0x20; //5
//    private static final int CONTROL_TIME =        0x40; //6
    private static final int LISTENER_STATE_STARTED = 0;
    private static final int LISTENER_STATE_STOPPED = 1;
    private static final int TACHO_LOOP_WAIT = 100;
    
    // masks for status register: REG_Status
//    privatestatic final int STATUS_SPEED =        0x01; //0
//    private static final int STATUS_RAMP =         0x02; //1
//    private static final int STATUS_POWERED =      0x04; //2
    private static final int STATUS_POSITIONAL =   0x08; //3
//    private static final int STATUS_BREAK =        0x10; //4
//    private static final int STATUS_OVERLOAD =     0x20; //5
//    private static final int STATUS_TIME =         0x40; //6
//    private static final int STATUS_STALL = 0x80; //7

    /** Use to specify motor float when a rotate method completes.
     * @see #setRotateStopMode
     */
    public static final int ROTSTOP_FLOAT = 1;

    /** Use to specify motor brake when a rotate method completes.
     * @see #setRotateStopMode
     */
    public static final int ROTSTOP_BRAKE = 2;

    /** Use to specify active hold when a rotate method completes. The NXTMMX will actively attempt to hold the motor angle.
     * @see #setRotateStopMode
     */
    public static final int ROTSTOP_LOCK = 4;
 
    private boolean tachoLock = false;
    private boolean tachoBrake = true;
    
    private volatile boolean _isRotateCmd = false;
    private volatile boolean startStalled  = false;
    private final int MOTOR_MAX_DPS; 
    private final TachoStatusMonitor tachoMonitor = new TachoStatusMonitor();
    private int _limitAngle=0;
    private RegulatedMotorListener listener = null;
    private Thread rotateMonitor=null;
    
    /** calcs degrees/sec. checks status.
     */
    private class TachoStatusMonitor implements Runnable {
        private volatile boolean resetTacho = false;
        private volatile int tachoCount = 0;
        private volatile boolean isMoving = false;
        private volatile float degpersec = 0f;
     
        public void run() {
            final int WEIRDO_TACHO = (int)(.001f*MOTOR_MAX_DPS*TACHO_LOOP_WAIT*2); //  per loop iteration time
            float degpersecAccum = 0f;
            int tc, tcBegin, tcDelta, index = 0;
            long stime, etime, tdelta;
            float[] samples = { 0f, 0f };

            stime = System.currentTimeMillis();
            this.tachoCount = tcBegin = getTacho();
            
            while (true) {
                if (resetTacho) {
                    resetTacho = false;
                    stime = System.currentTimeMillis();
                    this.tachoCount = tcBegin = getTacho();
                    synchronized (this) {
                        // wake up the resetTacho() method
                        this.notify();
                    }
                }
                Delay.msDelay(TACHO_LOOP_WAIT);
                
                // baseline tacho
                tc = getTacho();
                //System.out.println(this.tachoCount);
                tcDelta = Math.abs(tc - tcBegin);
                // ignore weirdness in tacho readings. never should exceed MOTOR_MAX_DPS * 2
                if (tcDelta>WEIRDO_TACHO) continue;
//                if (tcDelta>0) System.out.println("tcDelta=" + tcDelta);
                tcBegin = this.tachoCount = tc;
                
                // baseline time
                etime = System.currentTimeMillis();
                tdelta = etime - stime;
                stime = etime;

                // save a dps sample
                samples[index] = (float)tcDelta / tdelta * 1000;
                
                // set if moving
                this.isMoving = (Math.round(samples[index])!=0); 
//                dbg("ismov=" + isMoving);
                
                // if a movement is running...
                if (_isRunCmd || _isRotateCmd) {
                    // wake up any waits in waitComplete() if we are not moving during a fwd, bkwrd, or rotate (and complete)
                    if (!this.isMoving) {
                        // if rotate command, don't notify until status is 0 (we reached the target angle)
                        if (!(_isRotateCmd && ((getStatus() & STATUS_POSITIONAL) == STATUS_POSITIONAL))) {
                            synchronized (MMXRegulatedMotor.this) {
                                MMXRegulatedMotor.this.notifyAll();
                            }
                        }
                    } else {
                        if (MMXRegulatedMotor.this.startStalled) MMXRegulatedMotor.this.startStalled = false;
                    }
                }
                
                // do 3 pt moving average
                index++;
                if (index >= samples.length)
                    index = 0;
                // defeat moving avg on stall. Note that this really only works well/fast for unlimited duration runs because we have to
                // wait for NXTMMX status on rotates.
                if (!this.isMoving) {
                    this.degpersec = 0f;
                } else {
                    // average the samples and the last result (this.degpersec)
                    for (int i = 0; i < samples.length; i++) {
                        degpersecAccum += samples[i];
                    }
                    this.degpersec = degpersecAccum / (samples.length + 1);
                }
                degpersecAccum = this.degpersec;
            }
        }

        synchronized void resetTachoCount() { // TODO change if tachoMonitor is killed
            resetTacho = true;
            // need to wait until actually done. up to TACHO_LOOP_WAIT ms latency in tachomonitor thread
            while (resetTacho) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    ; // do nothing with e
                }
            }
        }
        
        synchronized boolean isMoving() {
            return this.isMoving;
        }
        
        synchronized boolean isStalled() {
            return !this.isMoving && (_isRunCmd || _isRotateCmd);
        }
        
        synchronized float getDPS() {
            return this.degpersec;
        }
        
        synchronized int getTachoCount() {
            return this.tachoCount;
        }
    }
    
    // when a rotate is issued, an instance uses waitComplete() to wait to be woken
    private class RotateMonitor extends Thread {
        RotateMonitor() {
            this.setDaemon(true);
            rotateMonitor=this;
        }
        public void run() {
            waitComplete();
            setStopState(); 
            rotateMonitor=null;
        }
    }
 
    /**
     * Create an instance of a <code>MMXRegulatedMotor</code>.
     * 
     * @param mux the motor multiplexor <code>NXTMMX</code> instance to bind this motor to.
     * @param motor the index of the motor connected to the NXTMMX: <code>NXTMMX.MMX_MOTOR_1</code> or <code>NXTMMX.MMX_MOTOR_2</code>
     * @see NXTMMX
     * @see NXTMMX#MMX_MOTOR_1
     * @see NXTMMX#MMX_MOTOR_2
     */
    public MMXRegulatedMotor (NXTMMX mux, int motor){
        super(mux, motor);
        
        MOTOR_MAX_DPS=(int)(mux.getVoltage()*100);
        
        // start the tachomonitor
        Thread monitorThread;
        monitorThread = new Thread(this.tachoMonitor);
        monitorThread.setDaemon(true);
        monitorThread.start();
    }
    
    /**
     * Add a single motor listener. Move operations will be reported to this object.
     * @param listener An instance of type <code>RegulatedMotorListener</code>
     * @see RegulatedMotorListener
     */
    public void addListener(RegulatedMotorListener listener) {
        this.listener = listener;
    }

    /** Remove the registered <code>RegulatedMotorListener</code>. 
     * @return The registered motor listener. <code>null</code> if none registered.
     */
    public RegulatedMotorListener removeListener() {
		RegulatedMotorListener old = this.listener;
		this.listener = null;
		return old;
	}

    
    /**	 Return the maximum speed of the motor. It is a general assumption that the maximum speed of a Motor is
	 *    100 degrees/second * Voltage.
     * @return The maximum speed in degrees per second
     */
    public float getMaxSpeed() {        
        return MOTOR_MAX_DPS;
    }
    
    /**
     * Returns the speed the motor is moving at. Note that this is estimated because the NXTMMX does not provide this
     * natively.
     * 
     * @return speed in degrees per second. Negative value means motor is rotating backward.
     */
    public int getRotationSpeed() { 
        return (int)this.tachoMonitor.getDPS() * _direction; 
    }
    
    /**
     * Wait until the current movement operation is complete. This can include
     * the motor stalling only on a <code>forward()</code> or <code>backward()</code> call.
     */
    public synchronized void waitComplete() { 
        if (!_isRunCmd && !_isRotateCmd) return;
        
        while(true) {
            try {
                this.wait(); // notify is in TachoStatusMonitor.run()
            } catch (InterruptedException e) {
                ;// do nothing
            }
            if (!this.tachoMonitor.isMoving()) break;
        }
    }

    /**
     * @return true if no timeout
     */
    private boolean waitForMotorMovement() { 
        final int WAITTIME = TACHO_LOOP_WAIT/4;
        final int MAXTIME = TACHO_LOOP_WAIT*3;
        long stime = System.currentTimeMillis();
        
        // busy wait until the motor is moving or timeout occurs
        while (System.currentTimeMillis() - stime < MAXTIME) {
            if (this.tachoMonitor.isMoving()) {
                break;
            }
            Delay.msDelay(WAITTIME);
        }
        if(System.currentTimeMillis() - stime >= MAXTIME){
            return false;
        }
        return true;
    }
    
    private void motorRotate(boolean relative, boolean waitForCompletion){
        int command = 0;
        if (_isRunCmd){
            notifyListener(LISTENER_STATE_STOPPED); // fwd/bckwd rotation stopped
        }
        _isRunCmd = false;
        // ensure any rotate monitor thread completes before preceding
        if (_isRotateCmd && rotateMonitor!=null) {
            synchronized (rotateMonitor) {
                try {
                    rotateMonitor.join();
                } catch (InterruptedException e) {
                    // TODO
                }
            }
        }
        _isRotateCmd=true;
        if (relative) command |= CONTROL_RELATIVE; //2
        command |= CONTROL_TACHO; //3
        if(tachoBrake) command |= CONTROL_TACHO_BRAKE; //4
        if(tachoLock) command |= CONTROL_TACHO_LOCK; //5
        setRotateAngle(_limitAngle);
        motorGO(command);
        
        this.startStalled=false;
        // let that puppy get a move on
        this.startStalled = !waitForMotorMovement();
        notifyListener(LISTENER_STATE_STARTED); // rotation started
        
        // wait until command completes if flagged
        if(waitForCompletion) {
            waitComplete();
            setStopState();
        } else {
            // do thread to notify listener
//            this.rotateCompleteThread = new RotateMonitor();
//            this.rotateCompleteThread.start();
            new RotateMonitor().start();
        }
    }
    
    private void setRotateAngle(int angle){
        buffer = intToByteArray(angle);
        mux.sendData(REG_RotateTo, buffer, 4);
    }
    
    /**
     * Rotate by the requested number of degrees. Negative values rotate opposite positive ones.
     * 
     * @param angle number of degrees to rotate relative to the current position
     * @param immediateReturn <code>true</code> will not block, <code>false</code> will wait until completion or stall.
     * @see #rotate(int)
     * @see #setRotateStopMode
     */
    public void rotate(int angle, boolean immediateReturn) {
        _limitAngle=angle;
        motorRotate(true, !immediateReturn); // use relative mode
}

    /**  Rotate by the requested number of degrees. Wait for the move to complete.
     * @param angle number of degrees to rotate relative to the current position
     * @see #rotate(int,boolean)
     * @see #setRotateStopMode
     */
    public void rotate(int angle){
        rotate(angle, false);
    }
    
    /**
     * Rotate to the target angle. If <code>immediateReturn</code> is <code>true</code>, the method returns immediately and the motor 
     * stops by itself and <code>getTachoCount()</code> should be within +- 2 degrees if the limit angle. If any motor method is called before 
     * the limit is reached, the rotation is canceled. 
     * <p>
     * When the angle is reached and the motor stops completely, the method 
     * <code>isMoving()</code> returns  <code>false</code>.
     * @param limitAngle Angle to rotate to.
     * @see #getTachoCount
     * @see #setRotateStopMode
     */
    public void rotateTo(int limitAngle, boolean immediateReturn) {
        _limitAngle=limitAngle;
        motorRotate(false, !immediateReturn); // use absolute mode
    }
    
    /**
     * Rotate to the target angle. Do not return until the move is complete. 
     * @param limitAngle Angle to rotate to.
     * @see #rotateTo(int, boolean)
     * @see #setRotateStopMode
     */
    public void rotateTo(int limitAngle)
    {
        rotateTo(limitAngle, false);
    }
    
    /**
     * Return the absolute angle that this Motor is rotating to. 
     * @return angle in degrees. 0 if no rotate method has been intiated.
     */
    public int getLimitAngle()
    {
        return (!_isRotateCmd)?0:getTachoCount()+_limitAngle;
    }
    
    /**
     * Return the current target speed.
     * @return Motor speed in degrees per second.
     * @see #setSpeed
     * @see #getPower
     */
    public int getSpeed(){
        return Math.round(8.1551f*this.currentPower+32.253f);
    }
    
    /**
     * Sets desired motor speed, in degrees per second. 
     * <p>
     * The NXTMMX does not provide speed control per se (just power) so we approximate the power value used
     * based on the requested degress/sec (dps) passed in <code>speed</code>. This means if you request 400 dps, the actual dps value
     * may not reflect that. Setting speed during a rotate method will have no effect on the running rotate but will on the next rotate
     * method call.
     * <p> 
     * experimental data gives: dps=8.1551*power+32.253 (unloaded @ 8.83V)
     * <p>
     * <b>Note:</b>The NXTMMX doesn't seem to want to drive the motor below ~40 dps.
     * @param speed Motor speed in degrees per second
     * @see #getSpeed
     * @see #setPower
     */
    public void setSpeed(int speed) {
        speed=Math.abs(speed);
        if (speed > MOTOR_MAX_DPS) speed=MOTOR_MAX_DPS;
        float power=(speed-32.253f)/8.1551f;
        if (power<0) power=0;
        setPower(Math.round(power));
    }

    
    /**
     * Causes motor to rotate forward or backward .
     */
    @Override
    final void doMotorDirection(int direction) {
        if (_isRunCmd){
            notifyListener(LISTENER_STATE_STOPPED); // fwd/bckwd rotation stopped
        }
        _isRunCmd = true;
        // ensure any rotate monitor thread completes before preceding
        if (_isRotateCmd && rotateMonitor!=null) {
            synchronized (rotateMonitor) {
                try {
                    rotateMonitor.join();
                } catch (InterruptedException e) {
                    // TODO
                }
            }
        }
        _isRotateCmd=false;
        _limitAngle=0;
        this.startStalled=false;
        
        boolean switchDirection=(_direction!=direction);
        
        super.doMotorDirection(direction);
        if (!switchDirection) {
            this.startStalled = !waitForMotorMovement(); 
        }
        notifyListener(LISTENER_STATE_STARTED); // rotation started
    }

         
    /**
     * Causes motor to rotate forward.
     * @see #backward
     */
    @Override
    final public void forward(){
        doMotorDirection(1);
    }
    
    /**
     * Causes motor to rotate backwards.
     * @see #forward
     */
    @Override
    final public void backward() {
        doMotorDirection(-1);
    }
    
    /**
     * Causes motor to float. This will stop the motor without braking
     * and the position of the motor will not be maintained.
     * 
     * @param immediateReturn If <code>true</code>, do not wait for the motor to actually stop
     * @see #flt()
     */
    public void flt(boolean immediateReturn) {
        this.startStalled=false;
        if (!_isRunCmd && !_isRotateCmd) return;
        // need to not use super.flt() because it messes with _isRunCmd which needs to be un-adulterated for setStopState()
        super.doFlt();
        if (!immediateReturn) {
            waitComplete(); 
        }
        setStopState();
    }
    
    /**
     * Causes motor to float. This will stop the motor without braking
     * and the position of the motor will not be maintained. This method will not wait for the motor to stop rotating
     * before returning.
     * 
     * @see #flt(boolean)
     * @see #lock()
     * @see #stop()
     */
    @Override
    final public void flt() {
        flt(true);
    }
    
    /**
     * Causes motor to stop pretty much instantaneously. In other words, the
     * motor doesn't just stop; it will resist any further motion.
     * <p>
     * Cancels any <code>rotate()</code> orders in progress.
     * 
     * @param immediateReturn if <code>true</code>, do not wait for the motor to actually stop
     * @see #stop()
     */
    final public void stop(boolean immediateReturn) {
        this.startStalled=false;
        if (!_isRunCmd && !_isRotateCmd) return;
        // need to not use super.stop() because it messes with _isRunCmd which needs to be un-adulterated for setStopState()
        super.doStop(); 
        if (!immediateReturn) {
            waitComplete();
        }
        setStopState();      
    }
    
    /** Causes motor to stop pretty much instantaneously. In other words, the
     * motor doesn't just stop; it will resist any further motion. The motor must stop rotating before <code>stop()</code> is
     * complete.
     * <p>
     * Cancels any <code>rotate()</code> orders in progress. 
     * @see #stop(boolean)
     * @see #flt()
     * @see #lock()
     */
    @Override
    final public void stop() {
        stop(false);
    }
    
    private synchronized void setStopState(){
        boolean doListener = _isRunCmd || _isRotateCmd;
        _isRotateCmd=false;
        _isRunCmd=false;
        _limitAngle=0;
        // need to notify listener after _isRunCmd and _isRotateCmd are cleared so stalled status is correct
        if (doListener) {
            notifyListener(LISTENER_STATE_STOPPED); // fwd/bckwd rotation stopped
        }
    }
    
    /**
     * Sets the motor stopping mode used for the rotate methods after rotation completion.
     * <p>
     * Default on instantiation is <code>ROTSTOP_BRAKE</code>.
     * @param mode <code>{@link #ROTSTOP_FLOAT}</code>, <code>{@link #ROTSTOP_BRAKE}</code>, or
     * <code>{@link #ROTSTOP_LOCK}</code>
     * @see #rotate(int)
     * @see #rotateTo(int)
     */
    final public void setRotateStopMode(int mode){ // TODO disable if tachoMonitor is killed
        tachoBrake=true;
        tachoLock=true;
        switch (mode){
            case ROTSTOP_FLOAT: 
                tachoBrake=false;
            case ROTSTOP_BRAKE:
                tachoLock=false;
                break;
            case ROTSTOP_LOCK:
                break;
            default:
                tachoLock=false;
        }
    }
    
    /**
     * Locks the motor in current position. Uses active feed back to hold it. 
     * @see #stop()
     * @see #flt()
     */
    final public void lock(){
        int command = 0;
        stop(true);
        Delay.msDelay(50);
        int position = getTacho();
        setRotateAngle(position);
        command |= CONTROL_TACHO; //3
        command |= CONTROL_TACHO_BRAKE; //4
        command |= CONTROL_TACHO_LOCK; //5
        _isRunCmd = true; // trick motorGO() to send GO command
        motorGO(command);
        _isRunCmd=false;
    }
    
    
    /**
     * Returns the tachometer count.
     * @return tachometer count in degrees
     * @see #resetTachoCount
     */
    @Override
    final public int getTachoCount() { 
        return this.tachoMonitor.getTachoCount();
    }
    
    /**
     * Resets the tachometer count to zero. 
     * @see #getTachoCount
     */
    @Override
    final public void resetTachoCount() { 
        super.resetTachoCount();
        // wait until tachmonitor resets
        this.tachoMonitor.resetTachoCount();
    }
    
     /**
      * Sets speed ramping is enabled/disabled for this motor. The <code>RegulatedMotor</code> interface specifies this in degrees/sec/sec
      * but the NXTMMX does not allow the rate to be changed, just if the motor uses smooth acceleration or not so we use the <code>acceleration</code>
      * parameter to specify ramping state. <p>Default at instantiation is ramping enabled.
      * @param acceleration >0 means NXTMMX internal ramping is enabled otherwise disabled
      */
    public void setAcceleration(int acceleration){
        this.rampUp=(acceleration>0);
    }
    
    /**
     * Return <code>true</code> if the motor has stalled after a motor action method was executed. The stalled status
     * will persist until movement occurs or a new movement or stop/flt command is issued. 
     * 
     * @return <code>true</code> if the motor is stalled, else <code>false</code>.
     * @see #forward
     * @see #backward
     * @see #rotate(int)
     */
    public boolean isStalled(){
        return this.startStalled || this.tachoMonitor.isStalled(); 
    }
    
    /**
    * This method returns <code>true</code> if the motor is rotating, whether under power or not.
    * The return value corresponds to the actual motor movement so if something external is rotating the motor,
    * <code>isMoving()</code>  will return <code>true</code>. 
    * After <code>flt()</code> is called, this method will return <code>true</code> until the motor
    * axle stops rotating by inertia, etc.
    * 
    * @return <code>true</code> if the motor is rotating, <code>false</code> otherwise.
    * @see #flt()
    */
    @Override
    final public boolean isMoving(){ 
        return this.tachoMonitor.isMoving();
    }

    private int getStatus(){ 
        mux.getData(REG_Status, buffer, 1);
        return buffer[0] & 0xff;
    }
    
    private void notifyListener(int state){ 
        if (this.listener==null) return;
        if (state==LISTENER_STATE_STOPPED)
            this.listener.rotationStopped(this, getTachoCount(), isStalled(), System.currentTimeMillis());
        else if(state==LISTENER_STATE_STARTED)
            this.listener.rotationStarted(this, getTachoCount(), isStalled(), System.currentTimeMillis());
    }

    /** 
     * Not implemented in the NXTMMX.
     * @param error ignored
     * @param time ignored
     */
    public void setStallThreshold(int error, int time) {
        // do nothing
	}
   
}
