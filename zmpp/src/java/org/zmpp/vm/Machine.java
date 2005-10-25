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

import org.zmpp.base.MemoryAccess;

/**
 * This interface gives the instructions an abstract access to the current
 * Z-machine's state.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface Machine {

  /**
   * The possible variable types.
   */
  enum VariableType { STACK, LOCAL, GLOBAL };
  
  /**
   * Returns true, if the checksum validation was successful.
   * 
   * @return true if checksum is valid
   */
  boolean hasValidChecksum();
  
  /**
   * Returns the current program counter.
   * 
   * @return the current program counter
   */
  int getProgramCounter();
  
  /**
   * Sets the program counter to a new address.
   * 
   * @param address the new address
   */
  void setProgramCounter(int address);
  
  // ********************************************************************
  // ***** Stack operations
  // ***************************************
  /**
   * Returns the global stack pointer.
   * 
   * @return the stack pointer
   */
  int getStackPointer();
  
  /**
   * Sets the global stack pointer to the specified value.
   * 
   * @param stackpointer the new stack pointer value
   */
  void setStackPointer(int stackpointer);
  
  /**
   * Returns the value at the top of the stack without removing it.
   * 
   * @return the stack top element
   */
  short getStackTopElement();
  
  /**
   * Sets the value of the element at the top of the stack without
   * incrementing the stack pointer.
   * 
   * @param value the value to set
   */
  void setStackTopElement(short value);
  
  /**
   * Returns the reference to the memory access object.
   * 
   * @return the reference to the MemoryAccess object
   */
  MemoryAccess getMemoryAccess();
  
  /**
   * Returns the value of the specified variable. 0 is the stack pointer,
   * 0x01-0x0f are local variables, and 0x10-0xff are global variables.
   * If the stack pointer is read from, its top value will be popped off.
   * 
   * @param variableNumber the variable number
   * @return the value of the variable
   * @throws IllegalStateException if a local variable is accessed without
   * a subroutine context or if a non-existent local variable is accessed
   */
  short getVariable(int variableNumber);
  
  /**
   * Sets the value of the specified variable. If the stack pointer is written
   * to, the stack will contain one more value.
   * 
   * @param variableNumber the variable number
   * @param value the value to write
   * @throws IllegalStateException if a local variable is accessed without
   * a subroutine context or if a non-existent local variable is accessed
   */
  void setVariable(int variableNumber, short value);

  /**
   * Pushes a new routine context onto the routine context stack.
   * 
   * @param routineContext the routine context object
   */
  void pushRoutineContext(RoutineContext routineContext);
  
  /**
   * Pops the current routine context from the stack. It will also
   * restore the state before the invocation of the routine, i.e. it
   * will restore the program counter and the stack pointers.
   * 
   * @return the last routine context
   * @throws IllegalStateException if no RoutineContext exists
   */
  RoutineContext popRoutineContext();
  
  /**
   * Returns the current routine context without affecting the state
   * of the machine.
   * 
   * @return the current routine context
   */
  RoutineContext getCurrentRoutineContext();
  
  /**
   * Returns the dictonary.
   * 
   * @return the dictionary
   */
  Dictionary getDictionary();
  
  /**
   * Returns the object tree.
   * 
   * @return the object tree
   */
  ObjectTree getObjectTree();
  
  /**  
   * Sets the output stream to the specified number.
   * 
   * @param streamnumber the stream number
   * @param stream the output stream
   */
  void setOutputStream(int streamnumber, OutputStream stream);
  
  /**
   * Enables or disables the specified output stream.
   * 
   * @param streamnumber the output stream number
   * @param flag true to enable, false to disable
   */
  void enableOutputStream(int streamnumber, boolean flag);
  
  /**
   * Sets an input stream to the specified number.
   * 
   * @param streamnumber the input stream number
   * @param stream the input stream to set
   */
  void setInputStream(int streamnumber, InputStream stream);
  
  /**
   * Selects an input stream.
   * 
   * @param streamnumber the input stream number to select
   */
  void selectInputStream(int streamnumber);
  
  /**
   * Reads a string from the selected input stream.
   * 
   * @param address the start address in memory
   * @param bufferlen the length of the buffer
   */
  void readLine(int address, int bufferlen);
  
  /**
   * Prints the ZSCII string at the specified address to the active
   * output streams.
   * 
   * @param stringAddress the address of an ZSCII string
   */
  void printZsciiString(int stringAddress);
  
  /**
   * Prints the specified string to the active output streams.
   * 
   * @param str the string to print
   */
  void print(String str);
  
  /**
   * Prints a newline to the active output streams.
   */
  void newline();
  
  /**
   * Prints the specified ZSCII character.
   * 
   * @param zchar the ZSCII character to print
   */
  void printZchar(short zchar);
  
  /**
   * Prints the specified signed number.
   * 
   * @param num the number to print§
   */
  void printNumber(short num);
  
  /**
   * Translates a packed address into a byte address.
   * 
   * @param packedAddress the packed address
   * @return the translated byte address
   */
  int translatePackedAddress(int packedAddress);
  
  /**
   * Generates a number in the range between 1 and range. If range is
   * negative, the random generator will be seeded to abs(range), if
   * range is 0, the random generator will be initialized to a new
   * random seed. In both latter cases, the result will be 0.
   * 
   * @param range the range
   * @return a random number
   */
  short random(short range);
  
  // ************************************************************************
  // ****** Control functions
  // ************************************************

  /**
   * Updates the status line.
   */
  void updateStatusLine();
  
  /**
   * Sets the Z-machine's status line.
   * 
   * @param statusLine the status line
   */
  void setStatusLine(StatusLine statusline);
  
  /**
   * Initialization function.
   * 
   * @param memaccess the MemoryAccess object
   * @param fileheader the story file header
   */
  void initialize(MemoryAccess memaccess, StoryFileHeader fileheader);
  
  /**
   * Halts the machine with the specified error message.
   * 
   * @param errormsg the error message
   */
  void halt(String errormsg);
  
  /**
   * Saves the current state.
   */
  boolean save();
  
  /**
   * Restores a previously saved state.
   */
  boolean restore();
  
  /**
   * Restarts the virtual machine.
   */
  void restart();
  
  /**
   * Starts the virtual machine.
   */
  void start();
  
  /**
   * Exists the virtual machine.
   */
  void quit();
  
  /**
   * Indicates if the virtual machine is running.
   * 
   * @return true if the machine is running, false, otherwise
   */
  boolean isRunning();
}
