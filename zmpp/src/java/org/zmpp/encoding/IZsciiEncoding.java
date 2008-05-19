/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.encoding;

/**
 *
 * @author weiju
 */
public interface IZsciiEncoding {

  static final char NULL          = 0;
  static final char DELETE        = 8;
  static final char NEWLINE_10    = 10;
  static final char NEWLINE       = 13;
  static final char ESCAPE        = 27;
  static final char CURSOR_UP     = 129;
  static final char CURSOR_DOWN   = 130;
  static final char CURSOR_LEFT   = 131;
  static final char CURSOR_RIGHT  = 132;
  static final char ASCII_START   = 32;
  static final char ASCII_END     = 126;
  
  /**
   * The start of the accent range. 
   */
  static final char ACCENT_START = 155;
  
  /**
   * End of the accent range.
   */
  static final char ACCENT_END   = 251;
  
  
  static final char MOUSE_DOUBLE_CLICK = 253;
  static final char MOUSE_SINGLE_CLICK = 254;
  
  /**
   * Converts the specified string into its ZSCII representation.
   * 
   * @param str the input string
   * @return the ZSCII representation
   */
  char[] convertToZscii(String str);

  /**
   * Converts a ZSCII character to a unicode character. Will return
   * '?' if the given character is not known.
   * 
   * @param zsciiChar a ZSCII character.
   * @return the unicode representation
   */
  char getUnicodeChar(char zsciiChar);
}
