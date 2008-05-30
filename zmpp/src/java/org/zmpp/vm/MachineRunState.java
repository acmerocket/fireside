/*
 * $Id$
 * 
 * Created on 05/27/2008
 * Copyright 2005-2008 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.vm;

/**
 * This class models a machine run state that also stores data for timed
 * input, so a client application can call an interrupt method on the machine.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MachineRunState {

  /**
   * Reading modes.
   */
  private enum ReadMode { NONE, READ_CHAR, READ_LINE };
  private int time, routine;
  private ReadMode readMode = ReadMode.NONE;
  
  /**
   * Default constructor.
   */
  private MachineRunState() { }

  /**
   * Constructor for reading modes.
   * @param readMode the read mode
   * @param time the interrupt routine time interval
   * @param routine the packed interrupt routine address
   */
  private MachineRunState(ReadMode readMode, int time, int routine) {
    this.readMode = readMode;
    this.time = time;
    this.routine = routine;
  }
  
  /**
   * Returns the interrupt interval.
   * @return the interrupt interval
   */
  public int getTime() { return time; }
  
  /**
   * Returns the packed address of the interrupt address.
   * @return packed interrupt routine address
   */
  public int getRoutine() { return routine; }
  
  /**
   * Returns true if machine is waiting for input.
   * @return true if waiting for input, false otherwise
   */
  public boolean isWaitingForInput() { return readMode != ReadMode.NONE; }
  
  /**
   * Returns true if machine is in read character mode.
   * @return true if read character mode, false otherwise
   */
  public boolean isReadChar() { return readMode == ReadMode.READ_CHAR; }
  
  /**
   * Returns true if machine is in read line mode.
   * @return true if read line mode, false otherwise
   */
  public boolean isReadLine() { return readMode == ReadMode.READ_LINE; }

  /**
   * Running state.
   */
  public static MachineRunState RUNNING = new MachineRunState();
  
  /**
   * Stopped state.
   */
  public static MachineRunState STOPPED = new MachineRunState();
  
  /**
   * Creates a read line mode object with the specified interrup data.
   * @param time interrupt interval
   * @param routine interrupt routine
   * @return machine run state object
   */
  public static MachineRunState createReadLine(int time, int routine) {
    return new MachineRunState(ReadMode.READ_LINE, time, routine);
  }
  
  /**
   * Creates a read character mode object with the specified interrupt data.
   * @param time interrupt interval
   * @param routine interrupt routine
   * @return machine state
   */
  public static MachineRunState createReadChar(int time, int routine) {
    return new MachineRunState(ReadMode.READ_CHAR, time, routine);
  }
}