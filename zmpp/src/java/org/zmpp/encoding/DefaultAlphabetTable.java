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
  public final short getA0Char(int index) {
    
    return (short) A0CHARS.charAt(index);
  }

  /**
   * {@inheritDoc}
   */
  public final short getA1Char(int index) {
  
    return (short) A1CHARS.charAt(index);
  }

  /**
   * {@inheritDoc}
   */
  public short getA2Char(int index) {
    
    return (short) A2CHARS.charAt(index);
  }
  
  /**
   * {@inheritDoc}
   */
  public final int getA0IndexOf(short zsciiChar) {

    return A0CHARS.indexOf(zsciiChar);
  }
  
  /**
   * {@inheritDoc}
   */
  public final int getA1IndexOf(short zsciiChar) {

    return A1CHARS.indexOf(zsciiChar);
  }

  /**
   * {@inheritDoc}
   */
  public int getA2IndexOf(short zsciiChar) {

    return A2CHARS.indexOf(zsciiChar);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShiftCharacter(short zchar) {
    
    return SHIFT_4 <= zchar && zchar <= SHIFT_5;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(short zchar) {
    
    return 1 <= zchar && zchar <= 3;
  }  
}
