/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package org.zmpp.base;

/**
 * This interface defines the structure of a story file header in the Z-machine.
 * It is designed as a read only view to the byte array containing the
 * story file data.
 * By this means, changes in the memory map will be implicitly change
 * the header structure.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface StoryFileHeader {

  static final int RELEASE              = 0x02;
  static final int PROGRAM_START        = 0x06;
  static final int DICTIONARY           = 0x08;
  static final int OBJECT_TABLE         = 0x0a;
  static final int GLOBALS              = 0x0c;
  static final int STATIC_MEM           = 0x0e;
  static final int ABBREVIATIONS        = 0x18;
  static final int CHECKSUM             = 0x1c;
  static final int INTERPRETER_NUMBER   = 0x1e;
  static final int SCREEN_HEIGHT        = 0x20;
  static final int SCREEN_WIDTH         = 0x21;
  static final int SCREEN_WIDTH_UNITS   = 0x22;
  static final int SCREEN_HEIGHT_UNITS  = 0x24;
  static final int ROUTINE_OFFSET       = 0x28;  
  static final int STATIC_STRING_OFFSET = 0x2a;
  static final int DEFAULT_BACKGROUND   = 0x2c;
  static final int DEFAULT_FOREGROUND   = 0x2d;
  static final int TERMINATORS          = 0x2e;
  static final int OUTPUT_STREAM3_WIDTH = 0x30; // 16 bit
  static final int STD_REVISION_MAJOR   = 0x32;
  static final int STD_REVISION_MINOR   = 0x33;
  static final int CUSTOM_ALPHABET      = 0x34;

  enum Attribute {
    DEFAULT_FONT_IS_VARIABLE,
    SCORE_GAME, SUPPORTS_STATUSLINE, SUPPORTS_SCREEN_SPLITTING, // V3 only
    TRANSCRIPTING, FORCE_FIXED_FONT, SUPPORTS_TIMED_INPUT,
    SUPPORTS_FIXED_FONT, SUPPORTS_ITALIC, SUPPORTS_BOLD,
    SUPPORTS_COLOURS, USE_MOUSE
  };
  /**
   * Returns the story file version.
   * @return the story file version
   */
  int getVersion();

  /**
   * Returns this game's serial number.
   * @return the serial number
   */
  String getSerialNumber();
  
  /**
   * Returns this story file's length.
   * @return the file length
   */
  int getFileLength();
  
  /**
   * Sets the interpreter version.
   * @param version the version
   */
  void setInterpreterVersion(int version);
  
  /**
   * Sets the font width in width of a '0'.
   * @param units the number of units in widths of a '0'
   */
  void setFontWidth(int units);
  
  /**
   * Sets the font height in width of a '0'.
   * @param units the number of units in heights of a '0'
   */
  void setFontHeight(int units);
  
  /**
   * Sets the mouse coordinates.
   * @param x the x coordinate
   * @param y the y coordinate
   */
  void setMouseCoordinates(int x, int y);
  
  /**
   * Returns the address of the cutom unicode translation table.
   * @return the address of the custom unicode translation table
   */
  char getCustomAccentTable();
  
  // ********************************************************************
  // ****** Attributes
  // **********************************
  
  /**
   * Enables the specified attribute.
   * @param attribute the attribute to set
   * @param flag the value
   */
  void setEnabled(Attribute attribute, boolean flag);
  
  /**
   * Checks the enabled status of the specified attribute
   * @param attribute the attribute name
   * @return true if enabled, false otherwise
   */
  boolean isEnabled(Attribute attribute);
}
