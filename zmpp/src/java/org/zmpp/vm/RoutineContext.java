/*
 * $Id$
 * 
 * Created on 10/03/2005
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
 * This class holds information about a subroutine.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class RoutineContext {

  /**
   * Set as return variable value if the call is a call_nx.
   */
  public static final char DISCARD_RESULT = 0xffff;
  
  /** The local variables */
  private char[] locals;
  
  /** The return address. */
  private int returnAddress;
  
  /** The return variable number to store the return value to. */
  private char returnVarNum;
  
  /** The stack pointer at invocation time. */
  private char invocationStackPointer;
  
  /** The number of arguments. */
  private int numArgs;
  
  /** The return value. */
  private char returnValue;
  
  /**
   * Constructor.
   * @param startAddress the routine's start address
   * @param numLocalVariables the number of local variables
   */
  public RoutineContext(int numLocalVariables) {    
    locals = new char[numLocalVariables];
  }
  
  /**
   * Sets the number of arguments.
   * @param numArgs the number of arguments
   */
  public void setNumArguments(final int numArgs) {
    this.numArgs = numArgs;
  }
  
  /**
   * Returns the number of arguments.
   * @return the number of arguments
   */
  public int getNumArguments() { return numArgs; }
  
  /**
   * Returns the number of local variables.
   * @return the number of local variables
   */
  public int getNumLocalVariables() {
    return (locals == null) ? 0 : locals.length;
  }
  
  /**
   * Sets a value to the specified local variable.
   * @param localNum the local variable number, starting with 0
   * @param value the value
   */
  public void setLocalVariable(final char localNum, final char value) {
    locals[localNum] = value;
  }

  /**
   * Retrieves the value of the specified local variable.
   * @param localNum the local variable number, starting at 0
   * @return the value of the specified variable
   */
  public char getLocalVariable(final char localNum) {
    return locals[localNum];
  }
  
  /**
   * Returns the routine's return address.
   * @return the routine's return address
   */
  public int getReturnAddress() { return returnAddress; }
  
  /**
   * Sets the return address.
   * @param address the return address
   */
  public void setReturnAddress(final int address) {
    this.returnAddress = address;
  }
  
  /**
   * Returns the routine's return variable number.
   * @return the return variable number or DISCARD_RESULT
   */
  public char getReturnVariable() { return returnVarNum; }
  
  /**
   * Sets the routine's return variable number.
   * @param varnum the return variable number or DISCARD_RESULT
   */
  public void setReturnVariable(final char varnum) { returnVarNum = varnum; }
  
  /**
   * Returns the stack pointer at invocation time.
   * @return the stack pointer at invocation time
   */
  public char getInvocationStackPointer() { return invocationStackPointer; }
  
  /**
   * Sets the stack pointer at invocation time.
   * @param stackpointer the stack pointer at invocation time.
   */
  public void setInvocationStackPointer(final char stackpointer) {
    invocationStackPointer = stackpointer;
  }
  
  /**
   * Returns the return value.
   * @return the return value
   */
  public char getReturnValue() { return returnValue; }
  
  /**
   * Sets the return value.
   * @param value the return value
   */
  public void setReturnValue(final char value) { returnValue = value; }
}
