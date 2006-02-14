/*
 * $Id$
 * 
 * Created on 10/03/2005
 * Copyright 2005-2006 by Wei-ju Wu
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

import org.zmpp.encoding.ZsciiString;
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
   * Returns the services interface.
   * 
   * @return the services
   */
  MachineServices getServices();
  
  /**
   * Returns the GameData interface.
   * 
   * @return the GameData interface
   */
  GameData getGameData();
  
  /**
   * Returns the Cpu interface.
   * 
   * @return the Cpu interface
   */
  Cpu getCpu();
  
  
  /**
   * Returns true, if the checksum validation was successful.
   * 
   * @return true if checksum is valid
   */
  boolean hasValidChecksum();
  
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
  void print(ZsciiString str);
  
  /**
   * Prints a newline to the active output streams.
   */
  void newline();
  
  /**
   * Prints the specified ZSCII character.
   * 
   * @param zchar the ZSCII character to print
   * @param isInput true if this is echoing input
   */
  void printZsciiChar(short zchar, boolean isInput);
  
  /**
   * Deletes the specified ZSCII character. This implements a backspace.
   * 
   * @param zchar the character to delete
   */
  void deletePreviousZsciiChar(short zchar);
  
  /**
   * Prints the specified signed number.
   * 
   * @param num the number to print?
   */
  void printNumber(short num);
  
  /**
   * Flushes the active output streams.
   */
  void flushOutput();
  
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
   * Initialization function.
   * 
   * @param machineConfig a configuration object
   * @param decoder the instruction decoder
   */
  void initialize(GameData machineConfig, InstructionDecoder decoder);
  
  /**
   * Sets the save game data store.
   * 
   * @param datastore the data store
   */
  void setSaveGameDataStore(SaveGameDataStore datastore);
  
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
   * Outputs a warning message.
   *  
   * @param msg
   */
  void warn(String msg);
  
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
   * Exists the virtual machine.
   */
  void quit();
  
}
