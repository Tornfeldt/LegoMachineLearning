package lejos.nxt.addon;

import lejos.nxt.addon.NXTMMX;

import lejos.robotics.EncoderMotor;

import lejos.util.Delay;


/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Abstraction to drive a basic encoder motor with the NXTMMX motor multiplexer. The 
 * NXTMMX motor multiplexer device allows you to connect two 
 * additional motors to your robot using a sensor port. Multiple NXTMMXs can be chained together.
 * <p>
 * Create an instance of this class passing a <code>NXTMMX</code> instance and Motor ID 
 * (<code>{@link NXTMMX#MMX_MOTOR_1 MMX_MOTOR_1}</code> or <code>{@link NXTMMX#MMX_MOTOR_2 MMX_MOTOR_2}</code>)
 * in the constructor.
 * 
 * @see NXTMMX
 * @see MMXRegulatedMotor
 * @author Kirk P. Thompson  
 *
 */
public class MMXMotor implements EncoderMotor{
    // masks for the command register: REG_MotorCommandRegAB  
    static final int CONTROL_SPEED =       0x01; //0
    private static final int CONTROL_RAMP =        0x02; //1
    private static final int CONTROL_GO =          0x80; //7
    
    NXTMMX mux;
    boolean rampUp = true; 
    private final boolean controlSpeed = true;  // no public accessor so I made final to ensure. when cleared, fwd/bckwrd doesn't work
    byte[] buffer = new byte[4];
     
    // motor registers                     A         B
    int REG_RotateTo =           0x42;//   0x4A
    private int REG_MotorSpeed =         0x46;//   0x4e
    private int REG_MotorRunTime =       0x47;//   0x4F
    private int REG_MotorCommandRegAB =   0x49;//   0x51
//    private int REG_MotorCommandRegB =   0x48;//   0x51 command register B not used
    private int REG_TacPos =             0x62;//   0x66
    int REG_Status =             0x72;//   0x73 
    private int REG_Tasks =              0x76;//   0x77
    
    static final int REG_MUX_Command =        0x41;
    // Commands for command register: REG_MUX_Command
    int COMMAND_ResetTaco =      0x72;//   0x73 
    int COMMAND_Stop =           'A';//    'B'
    int COMMAND_Float =          'a';//    'b' 
    
    private static final int POWER_INIT = -9999;
    volatile boolean _isRunCmd = false;
    int currentPower = POWER_INIT;
    int _direction=1; // 1=forward, -1=backward

    /**
     * Create an instance of a <code>MMXMotor</code>.
     * 
     * @param mux the motor multiplexor <code>NXTMMX</code> instance to bind this motor to.
     * @param motor the index of the motor connected to the NXTMMX: <code>NXTMMX.MMX_MOTOR_1</code> or <code>NXTMMX.MMX_MOTOR_2</code>
     * @see NXTMMX
     * @see NXTMMX#MMX_MOTOR_1
     * @see NXTMMX#MMX_MOTOR_2
     */
    public MMXMotor (NXTMMX mux, int motor){
        this.mux = mux;
        REG_RotateTo = REG_RotateTo + (motor * 8);
        REG_MotorSpeed = REG_MotorSpeed + (motor * 8);
        REG_MotorRunTime = REG_MotorRunTime + (motor * 8);
        REG_MotorCommandRegAB = REG_MotorCommandRegAB + (motor * 8);
        REG_TacPos = REG_TacPos + (motor * 4);
        REG_Status = REG_Status + motor;
        REG_Tasks = REG_Tasks + motor;
        
        COMMAND_ResetTaco = COMMAND_ResetTaco + motor;
        COMMAND_Stop = COMMAND_Stop + motor;
        COMMAND_Float = COMMAND_Float + motor;
    }
    
//    void dbg(String msg){
//        if (!msg.equals("")) System.out.println(msg);
//        Button.waitForAnyPress();
//        Delay.msDelay(200);
//    }

    /**
     * sets motor command mask to the NXTMMX motor command register with appropriate bit masks set on passed command value
     * @param command additional bit masks for the motor command register
     */
    void motorGO(int command){
        if(this.controlSpeed) command |= CONTROL_SPEED; //0
        if(this.rampUp) command |= CONTROL_RAMP; //1  
        // if authstart or we already have started [a non-rotate] and need to effect power change
        if(mux.isAutoStart() || _isRunCmd) command |= CONTROL_GO; // 7
        // send the command
        mux.sendData(REG_MotorCommandRegAB, (byte)command);
    }
    
    /**
     * Set the power level 0-100% to be applied to the motor. 
     * 
     * @param power new motor power 0-100%
     * @see #getPower
     */
    public void setPower(int power){
        power = Math.abs(power);
        if (power > 100) power = 100;
        power *= _direction;
        // this is why we use this.currentPower==POWER_INIT on intialization: If power is not sent, the MMX runs @ 100% by default
        // on powerup. This forces an i2c send to set the power.
        if (this.currentPower!=power) {
            // send the new power value. This needs to be done for reverses as well
            mux.sendData(REG_MotorSpeed, (byte)power);
            this.currentPower=power;
            // if motor is running on non-rotate/timing command, effect power change immediately
            if (_isRunCmd) {
                motorGO(0);
            }
        }
    }
    
    /**
     * Returns the current motor power setting. 
     * @return current power 0-100%
     * @see #setPower
     */
    public int getPower() {
        int power = (this.currentPower==POWER_INIT)?0:Math.abs(this.currentPower);
        return power;
    }
    
    /**
     * Causes motor to rotate forward or backward .
     */
    void doMotorDirection(int direction) {
        _isRunCmd = true;
        if (_direction!=direction) { 
            _direction=direction; // 1=forward, -1=backward
            // effect power change
            setPower(this.currentPower); // setPower() does a motorGO();
        } else {
            motorGO(0);
        }
    }
    
    /**
     * Causes motor to rotate forward.
     * @see #backward
     */
    public void forward(){
        doMotorDirection(1);
    }
    
    /**
     * Causes motor to rotate backwards.
     * @see #forward
     */
    public void backward() {
        doMotorDirection(-1);
    }
    
    /**
     * Causes motor to float. This will stop the motor without braking
     * and the position of the motor will not be maintained.
     * 
     */
    public void flt() {
        if (!_isRunCmd) return;
        doFlt();
        _isRunCmd = false;
    }
    
//    public void setKpSpeed(int kp){
//        final int REG_KP_SPEED = 0x80;
//        buffer = intToByteArray(kp);
//        mux.sendData(REG_KP_SPEED, buffer, 2);
//    }
    
    void doFlt(){
        mux.sendData(REG_MUX_Command, (byte) COMMAND_Float);
    }
    
    /**
     * Causes motor to stop pretty much instantaneously. In other words the
     * motor doesn't just stop, it will resist any further motion.
     */
    public void stop() {
        if (!_isRunCmd) return;
        doStop();
        _isRunCmd = false;
    }

    void doStop(){
        mux.sendData(REG_MUX_Command, (byte) COMMAND_Stop);
    }
    
    int getTacho() {
        int i2cTVal;
        
        i2cTVal=mux.getData(REG_TacPos, buffer, 4);
        while (i2cTVal<0){
            Delay.msDelay(20);
            i2cTVal=mux.getData(REG_TacPos, buffer, 4);
        }
        return byteArrayToInt(buffer);
    }
    
    /**
     * Returns the tachometer count.
     * @return tachometer count in degrees
     * @see #resetTachoCount
     */
    public int getTachoCount() { 
        return getTacho();
    }
    
    /**
     * Resets the tachometer count to zero. 
     * @see #getTachoCount
     */
    public void resetTachoCount() { 
        mux.sendData(REG_MUX_Command, (byte)COMMAND_ResetTaco);
    }
    
    /**
    * This method returns <code>true</code> if the motor has been commanded with the <code>forward()</code> or 
    * <code>backward()</code> methods.
    * 
    * 
    * @return <code>true</code> if the motor has been commanded to move, <code>false</code> otherwise.
    * @see #flt()
    */
    public boolean isMoving(){ 
        return _isRunCmd;
    }
   
    byte[] intToByteArray(int value) {
        return new byte[] {
            (byte)(value),
            (byte)(value >>> 8),
            (byte)(value >>> 16),
            (byte)(value >>> 24)} ;
    }
    
    int byteArrayToInt( byte[] buffer){
        return (buffer[3] << 24)
        + ((buffer[2] & 0xFF) << 16)
        + ((buffer[1] & 0xFF) << 8)
        + (buffer[0] & 0xFF); 
    }
}
