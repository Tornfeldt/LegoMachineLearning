package lejos.nxt.rcxcomm;

import lejos.nxt.*;
import java.io.IOException;

/*
 * WARNING: THIS CLASS IS SHARED BETWEEN THE classes AND pccomms PROJECTS.
 * DO NOT EDIT THE VERSION IN pccomms AS IT WILL BE OVERWRITTEN WHEN THE PROJECT IS BUILT.
 */

/** 
 * RCXPort provides an interface similar to java.net.Socket
 * Adapted from original code created by the LEGO3 Team at DTU-IAU
 * Uses Reliable low-level comms for communication.
 * This is a two-layer comms stack consisting of LLCReliableHandler
 * and LLCHandler. It ensures that all packets get through.
 * Communication will stop when the IR tower is not in view or in range,
 * and will resume when it comes back into view.
 * RCXPort does not support addressing - it broadcasts messages to all devices.
 * 
 * @author Brian Bagnall
 * @author Lawrie Griffiths
 * 
 */
public class RCXPort extends RCXAbstractPort {
  public RCXPort(SensorPort port) throws IOException {
    super((PacketHandler) new LLCReliableHandler(
                       (PacketHandler) new LLCHandler(port)));
  }
}


