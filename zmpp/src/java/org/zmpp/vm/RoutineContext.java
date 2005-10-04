/*
 * $Id$
 * 
 * Created on 03.10.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.zmpp.vm;

/**
 * This class holds information about a subroutine.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class RoutineContext {

  /**
   * The start address of the routine's code.
   */
  private int startAddress;
  
  /**
   * The local variables.
   */
  private int[] locals;
  
  /**
   * The return address.
   */
  private int returnAddress;
  
  /**
   * The return variable number to store the return value to.
   */
  private int returnVarNum;
  
  /**
   * Constructor.
   * 
   * @param startAddress the routine's start address
   * @param numLocalVariables the number of local variables
   */
  public RoutineContext(int startAddress, int numLocalVariables) {
    
    this.startAddress = startAddress;
    if (numLocalVariables > 0) {
      
      locals = new int[numLocalVariables];
    }
  }
  
  /**
   * Returns the number of local variables.
   * 
   * @return the number of local variables
   */
  public int getNumLocalVariables() {
    
    return (locals != null) ? locals.length : 0;
  }
  
  /**
   * Sets a value to the specified local variable.
   *  
   * @param localNum the local variable number, starting with 0
   * @param value the value
   */
  public void setLocalVariable(int localNum, int value) {
    
    locals[localNum] = value;
  }

  /**
   * Retrieves the value of the specified local variable.
   * 
   * @param localNum the local variable number, starting at 0
   * @return the value of the specified variable
   */
  public int getLocalVariable(int localNum) {
    
    return locals[localNum];
  }
  
  /**
   * Returns this routine's start address.
   * 
   * @return the start address
   */
  public int getStartAddress() {
    
    return startAddress;
  }
  
  /**
   * Returns the routine's return address.
   * 
   * @return the routine's return address
   */
  public int getReturnAddress() {
    
    return returnAddress;
  }
  
  /**
   * Sets the return address.
   * 
   * @param address the return address
   */
  public void setReturnAddress(int address) {
    
    this.returnAddress = address;
  }
  
  /**
   * Returns the routine's return variable number.
   * 
   * @return the return variable number
   */
  public int getReturnVariable() {
    
    return returnVarNum;
  }
  
  /**
   * Sets the routine's return variable number.
   * 
   * @param varnum the return variable number
   */
  public void setReturnVariable(short varnum) {
    
    returnVarNum = varnum;
  }
}
