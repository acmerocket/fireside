/*
 * $Id$
 * 
 * Created on 2006/01/12
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
package org.zmpp.encoding;

/**
 * The default alphabet table implementation.
 * 
 * @author Wei-ju Wu
 */
public class DefaultAlphabetTable implements AlphabetTable {

  private static final String A0CHARS = "abcdefghijklmnopqrstuvwxyz";
  private static final String A1CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String A2CHARS = " \n0123456789.,!?_#'\"/\\-:()";

  /**
   * {@inheritDoc}
   */
  public short getA0Char(byte zchar) {
    
    return (short) A0CHARS.charAt(zchar - ALPHABET_START);
  }

  /**
   * {@inheritDoc}
   */
  public short getA1Char(byte zchar) {
  
    return (short) A1CHARS.charAt(zchar - ALPHABET_START);
  }

  /**
   * {@inheritDoc}
   */
  public short getA2Char(byte zchar) {
    
    return (short) A2CHARS.charAt(zchar - ALPHABET_START);
  }
  
  /**
   * {@inheritDoc}
   */
  public final byte getA0CharCode(short zsciiChar) {

    return getCharCodeFor(A0CHARS, zsciiChar);
  }
  
  /**
   * {@inheritDoc}
   */
  public final byte getA1CharCode(short zsciiChar) {

    return getCharCodeFor(A1CHARS, zsciiChar);
  }

  /**
   * {@inheritDoc}
   */
  public byte getA2CharCode(short zsciiChar) {

    return getCharCodeFor(A2CHARS, zsciiChar);
  }

  protected static byte getCharCodeFor(String chars, short zsciiChar) {
    
    int index = chars.indexOf(zsciiChar);
    if (index >= 0) {
      
      index += ALPHABET_START;
    }
    return (byte) index;
  }
  
  
  /**
   * {@inheritDoc}
   */
  public boolean isShift1(short zchar) {
    
    return zchar == SHIFT_4;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShift2(short zchar) {
    
    return zchar == SHIFT_5;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isShift(short zchar) {
    
    return isShift1(zchar) || isShift2(zchar);
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isShiftLock(short zchar) {
    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(short zchar) {
    
    return 1 <= zchar && zchar <= 3;
  }  
}