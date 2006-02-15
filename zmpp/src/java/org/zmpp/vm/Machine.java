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

import org.zmpp.media.SoundSystem;

/**
 * This interface acts as a central access point to the Z-Machine's components.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface Machine {

  /**
   * Initialization function.
   * 
   * @param machineConfig a configuration object
   * @param decoder the instruction decoder
   */
  void initialize(GameData machineConfig, InstructionDecoder decoder);  
  
  // **********************************************************************
  // **** Main machine objects
  // *******************************
  
  /**
   * Returns the GameData object.
   * 
   * @return the GameData object
   */
  GameData getGameData();
  
  /**
   * Returns the Cpu object.
   * 
   * @return the Cpu object
   */
  Cpu getCpu();
  
  /**
   * Returns the Output object.
   * 
   * @return the Output object
   */
  Output getOutput();
  
  /**
   * Returns the Input object.
   * 
   * @return the Input object
   */
  Input getInput();  
  
  // ************************************************************************
  // ****** Control functions
  // ************************************************

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
   * Outputs a warning message.
   *  
   * @param msg
   */
  void warn(String msg);
  
  // **********************************************************************
  // **** Services
  // *******************************
  
  /**
   * Returns an input functions object.
   * 
   * @return an input functions object
   */
  InputFunctions getInputFunctions();
  
  /**
   * Returns the tokenizer.
   * 
   * @return the tokenizer
   */
  Tokenizer getTokenizer();
  
  /**
   * Returns the sound system.
   * 
   * @return the sound system
   */
  SoundSystem getSoundSystem();
  
  /**
   * Generates a number in the range between 1 and <i>range</i>. If range is
   * negative, the random generator will be seeded to abs(range), if
   * range is 0, the random generator will be initialized to a new
   * random seed. In both latter cases, the result will be 0.
   * 
   * @param range the range
   * @return a random number
   */
  short random(short range);  

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
}
