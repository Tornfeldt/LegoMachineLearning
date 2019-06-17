package lejos.nxt.addon;

import lejos.nxt.I2CPort;
import lejos.nxt.I2CSensor;
import lejos.util.EndianTools;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/**
 * Supports the angle sensor of HiTechnic.
 * This Java implementation was based on the NXC implementation on http://www.hitechnic.com/cgi-bin/commerce.cgi?preadd=action&key=NAA1030.
 * Works clockwise, i.e. rotating clockwise increases angle value and rotating counter-clockwise decreases angle value.
 *
 * @author Michael Mirwaldt (epcfreak@gmail.com)<br/>
 * date 2nd April 2011
 */

public class AngleSensor extends I2CSensor {
   
   protected static final int REG_ANGLE = 0x42;
   protected static final int REG_ACCUMULATED_ANGLE = 0x44;
   protected static final int REG_SPEED = 0x48;
   protected static final int HTANGLE_MODE_CALIBRATE = 0x43;
   protected static final int HTANGLE_MODE_RESET = 0x52;

   public AngleSensor(I2CPort port) {
      super(port, DEFAULT_I2C_ADDRESS, I2CPort.LEGO_MODE, TYPE_LOWSPEED);
   }
   
   /** 
    * reads the current angle
    *
    * @return angle. Value lies between 0-359
    */
   public int getAngle() {
      byte buf[] = new byte[2];
      getData(REG_ANGLE, buf, 2);
      int bits9to2 = buf[0] & 0xFF;
      int bit1 = buf[1] & 0x01;
      
      return (bits9to2 << 1) | bit1;
   }
   
   /** 
    * @see #getAccAngle()
    *
    */
   public long getAccumulatedAngle() {
      return getAccAngle();
   }
   
   /** 
    * reads the current accumulated angle
    *
    * @return the current accumulated angle. Value lies between -2147483648 to 2147483647.
    */
   public long getAccAngle() {
      byte buf[] = new byte[4];
      getData(REG_ACCUMULATED_ANGLE, buf, 4); 
      
      return EndianTools.decodeIntBE(buf, 0);
   }
   
   /** 
    * reads the current rotations per minute
    *
    * @return current rotations per minute. Value lies between -1000 to 1000.
    */
   public int getRPM() {
      byte buf[] = new byte[2];
      getData(REG_SPEED, buf, 2);
      
      return EndianTools.decodeShortBE(buf, 0);
   }
   
   
   /** 
    * @see #getAccAngle()
    *
    */
   public int getRotationsPerMinute() {
      return getRPM();
   }
   
   /** 
    * @see #resetAccAngle()
    *
    */
   public void resetAccumulatedAngle() {
      resetAccAngle();
   }
   
   
   /** 
    * Reset the rotation count of accumulated angle to zero. 
    * Not saved in EEPORM.
    */
   public void resetAccAngle() {
      sendData(0x41, (byte) HTANGLE_MODE_RESET);
   }
   
   /** 
    * Calibrate the zero position of angle.
    * Zero position is saved in EEPROM on sensor.
    * Thread sleeps for 50ms while that is done.
    */
   public void calibrateAngle() {
      sendData(0x41, (byte) HTANGLE_MODE_CALIBRATE);
      try {
         // Time to allow burning EEPROM
         Thread.sleep(50);
      } catch (InterruptedException e) {
         // ignore because it does not seem to cause trouble
      } 
   }
}
