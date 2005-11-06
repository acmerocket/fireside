/*
 * $Id$
 * 
 * Created on 2005/09/23
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
 * This interface defines the structure of a story file header in the Z-machine.
 * It is designed as a read only view to the byte array containing the
 * story file data.
 * By this means, changes in the memory map will be implicitly change
 * the header structure.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public interface StoryFileHeader {

  /**
   * Returns the story file version.
   * 
   * @return the story file version
   */
  int getVersion();
  
  /**
   * Returns the flags1 field.
   * 
   * @return the flags1 field
   */
  int getFlags1();
  
  /**
   * Returns the release number.
   * 
   * @return the release number
   */
  int getRelease();
  
  /**
   * Returns the high memory start address.
   * 
   * @return the start of the high memory
   */
  int getHighMemAddress();
  
  /**
   * Returns the program counter start address.
   * 
   * @return the PC start address
   */
  int getProgramStart();
  
  /**
   * Returns the dictionary's start address.
   * 
   * @return the dictionary start address
   */  
  int getDictionaryAddress();
  
  /**
   * Returns the object table's start address.
   * 
   * @return the object table's start address
   */
  int getObjectTableAddress();
  
  /**
   * Returns the address of the global variables.
   * 
   * @return the global variables section
   */
  int getGlobalsAddress();
  
  /**
   * Returns the static memory start address.
   * 
   * @return the start address of the static memory
   */
  int getStaticsAddress();
  
  /**
   * Returns the flags2 field.
   * 
   * @return the flags2 field
   */
  int getFlags2();
  
  /**
   * Returns this game's serial number.
   * 
   * @return the serial number
   */
  String getSerialNumber();
  
  /**
   * Returns the start address of the abbreviations section.
   * 
   * @return the abbreviations start address
   */
  int getAbbreviationsAddress();
  
  /**
   * Returns this story file's length.
   * 
   * @return the file length
   */
  int getFileLength();
  
  /**
   * Returns the checksum for the story file.
   * 
   * @return the checksum
   */
  int getChecksum();
  
  /**
   * Returns the interpreter number.
   * 
   * @return the interpreter number
   */
  int getInterpreter();
  
  /**
   * Returns the interpreter version.
   * 
   * @return the interpreter version
   */
  int getInterpreterVersion();
  
  /**
   * Returns the revision number.
   * 
   * @return the revision number
   */
  int getRevision();
  
  // ********************************************************************
  // ****** Flags 1 bits
  // **********************************
  
  /**
   * Returns true if the game is a score game.
   * 
   * @return true if score game
   */
  boolean isScoreGame();

  /**
   * Set by the interpreter to indicate if a status line is supported.
   * 
   * @param flag true if status line is supported, false, otherwise
   */
  void setStatusLineAvailable(boolean flag);
  
  /**
   * Set by the interpreter to indicate if screen splitting is supported.
   * 
   * @param flag true if splitting is supported, false, otherwise
   */
  void setScreenSplittingAvailable(boolean flag);
  
  /**
   * Set by the interpreter to indicate if the default font is variable
   * pitch.
   * 
   * @param flag true if default font is variable pitch, false otherwise
   */
  void setDefaultFontIsVariablePitch(boolean flag);
  
  /**
   * Returns if default font is variable pitch.
   * 
   * @return true if variable pitch, false, otherwise
   */
  boolean defaultFontIsVariablePitch();

  // ********************************************************************
  // ****** Flags 2 bits
  // **********************************
  
  /**
   * Set to true if transcripting is on.
   * 
   * @param flag true if transcripting on
   */
  void setTranscripting(boolean flag);
  
  /**
   * Returns true if transcripting is on.
   * 
   * @return true if transcripting is on, false, otherwise
   */
  boolean isTranscriptingOn();

  /**
   * Returns true if fixed font is forced by the game.
   * 
   * @return true if fixed font forced, false, otherwise
   */
  boolean forceFixedFont();
}
