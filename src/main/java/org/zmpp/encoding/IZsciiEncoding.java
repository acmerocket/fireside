/*
 * Created on 2006/01/15
 * Copyright 2005-2009 by Wei-ju Wu
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
package org.zmpp.encoding;

/**
 * ZsciiEncoding interface.
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface IZsciiEncoding {

  char NULL          = 0;
  char DELETE        = 8;
  char NEWLINE_10    = 10;
  char NEWLINE       = 13;
  char ESCAPE        = 27;
  char CURSOR_UP     = 129;
  char CURSOR_DOWN   = 130;
  char CURSOR_LEFT   = 131;
  char CURSOR_RIGHT  = 132;
  char ASCII_START   = 32;
  char ASCII_END     = 126;

  /**
   * The start of the accent range.
   */
  char ACCENT_START = 155;

  /**
   * End of the accent range.
   */
  char ACCENT_END   = 251;


  char MOUSE_DOUBLE_CLICK = 253;
  char MOUSE_SINGLE_CLICK = 254;

  /**
   * Converts the specified string into its ZSCII representation.
   *
   * @param str the input string
   * @return the ZSCII representation
   */
  String convertToZscii(String str);

  /**
   * Converts a ZSCII character to a unicode character. Will return
   * '?' if the given character is not known.
   *
   * @param zsciiChar a ZSCII character.
   * @return the unicode representation
   */
  char getUnicodeChar(char zsciiChar);
}
