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

import java.util.List;

import org.zmpp.base.MemoryAccess;
import org.zmpp.io.InputStream;
import org.zmpp.io.OutputStream;

/**
 * This interface gives the instructions an abstract access to the current
 * Z-machine's state.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface Machine {

  /**
   * The output stream number for the screen.
   */
  final static int OUTPUTSTREAM_SCREEN = 1;
  
  /**
   * The output stream number for the transcript.
   */
  final static int OUTPUTSTREAM_TRANSCRIPT = 2;
  
  /**
   * The output stream number for the memory stream.
   */
  final static int OUTPUTSTREAM_MEMORY = 3;
  
  /**
   * The input stream number for the keyboard.
   */
  final static int INPUTSTREAM_KEYBOARD = 0;
  
  /**
   * The input stream number for file input.
   */
  final static int INPUTSTREAM_FILE = 1;
  
  /**
   * The possible variable types.
   */
  enum VariableType { STACK, LOCAL, GLOBAL };
  
  /**
   * Returns the story file header.
   * 
   * @return the story file header
   */
  StoryFileHeader getStoryFileHeader();
  
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
   * Returns the global stack pointer. Equals the stack size.
   * 
   * @return the stack pointer
   */
  int getStackPointer();
  
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
   * Returns the evaluation stack element at the specified index.
   * 
   * @param index an index
   * @return the stack value at the specified index
   */
  short getStackElement(int index);
  
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
   * will restore the program counter and the stack pointers and set
   * the specfied return value to the return variable.
   * 
   * @param returnValue the return value
   * @throws IllegalStateException if no RoutineContext exists
   */
  void popRoutineContext(short returnValue);
  
  /**
   * Returns the state of the current routine context stack as a non-
   * modifiable List. This is exposed to PortableGameState to take a
   * machine state snapshot.
   * 
   * @return the list of routine contexts
   */
  List<RoutineContext> getRoutineContexts();
  
  /**
   * Copies the list of routine contexts into this machine's routine context
   * stack. This is a consequence of a restore operation.
   * 
   * @param contexts a list of routine contexts
   */
  void setRoutineContexts(List<RoutineContext> contexts);
  
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
   * Selects/unselects the specified output stream. If the streamnumber
   * is negative, |streamnumber| is deselected, if positive, it is selected.
   * Stream 3 (the memory stream) can not be selected by this function,
   * but can be deselected here.
   * 
   * @param streamnumber the output stream number
   * @param flag true to enable, false to disable
   */
  void selectOutputStream(int streamnumber, boolean flag);
  
  /**
   * Selects the output stream 3 which writes to memory.
   * 
   * @param tableAddress the table address to write to
   */
  void selectOutputStream3(int tableAddress);
  
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
   * Returns the selected input stream.
   * 
   * @return the selected input stream
   */
  InputStream getSelectedInputStream();
  
  /**
   * Returns an input functions object.
   * 
   * @return an input functions object
   */
  InputFunctions getInputFunctions();
  
  /**
   * Prints the ZSCII string at the specified address to the active
   * output streams.
   * 
   * @param stringAddress the address of an ZSCII string
   */
  void printZString(int stringAddress);
  
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
   * Prints the specified ZSCII characoter.
   * 
   * @param zchar the ZSCII character to print
   * @param isInput true if this is echoing input
   */
  void printZsciiChar(short zchar, boolean isInput);
  
  /**
   * Prints the specified signed number.
   * 
   * @param num the number to print§
   */
  void printNumber(short num);
  
  /**
   * Flushes the active output streams.
   */
  void flushOutput();
  
  /**
   * Translates a packed address into a byte address.
   * 
   * @param packedAddress the packed address
   * @param isCall if true then this is a call address, if false, this is
   * a string address
   * @return the translated byte address
   */
  int translatePackedAddress(int packedAddress, boolean isCall);
  
  /**
   * Computes a branch target from an offset.
   * 
   * @return the resulting branch target
   */
  int computeBranchTarget(short offset, int instructionLength);
  
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
   * @param statusline the status line
   */
  void setStatusLine(StatusLine statusline);
  
  /**
   * Sets the game screen.
   * 
   * @param screen the screen model
   */
  void setScreen(ScreenModel screen);
  
  /**
   * Gets the game screen.
   * 
   * @return the game screen
   */
  ScreenModel getScreen();
  
  /**
   * Plays a sound effect.
   * 
   * @param soundnum the sound number
   * @param effect the effect
   * @param volume the volume
   * @param routine the routine
   */
  void playSoundEffect(int soundnum, int effect, int volume, int routine);  
  
  /**
   * Initialization function.
   * 
   * @param machineConfig a configuration object
   * @param decoder the instruction decoder
   */
  void initialize(MachineConfig machineConfig, InstructionDecoder decoder);
  
  /**
   * Sets the save game data store.
   * 
   * @param datastore the data store
   */
  void setSaveGameDataStore(SaveGameDataStore datastore);
  
  /**
   * Halts the machine with the specified error message.
   * 
   * @param errormsg the error message
   */
  void halt(String errormsg);
  
  /**
   * Outputs a warning message.
   *  
   * @param msg
   */
  void warn(String msg);
  
  /**
   * Saves the current state.

   * @param savepc the save pc
   * @return true on success, false otherwise
   */
  boolean save(int savepc);
  
  /**
   * Saves the current state in memory.
   * 
   * @param savepc the save pc
   * @return true on success, false otherwise
   */
  boolean save_undo(int savepc);
  
  /**
   * Restores a previously saved state.
   * 
   * @return the portable game state
   */
  PortableGameState restore();
  
  /**
   * Restores a previously saved state from memory.
   * 
   * @return the portable game state
   */
  PortableGameState restore_undo();
  
  /**
   * Restarts the virtual machine.
   */
  void restart();
  
  /**
   * Starts the virtual machine.
   */
  void start();
  
  /**
   * Returns the next instruction.
   * 
   * @return the next instruction
   */
  Instruction nextStep();
  
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
  
  /**
   * Performs a routine call.
   * 
   * @param routineAddress the packed routine address
   * @param returnAddress the return address
   * @param args the argument list
   * @param returnVariable the return variable or DISCARD_RESULT
   * @return the routine context created
   */
  RoutineContext call(int routineAddress, int returnAddress, short[] args,
      short returnVariable);
}
