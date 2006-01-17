/*
 * $Id$
 * 
 * Created on 2006/01/16
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

import org.zmpp.base.MemoryReadAccess;

/**
 * If the story file header defines a custom alphabet table, instances
 * of this class are used to retrieve the alphabet characters.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class CustomAlphabetTable implements AlphabetTable {

  private static final int ALPHABET_SIZE = 26;
  
  private MemoryReadAccess memaccess;
  private int tableAddress;
 
  /**
   * Constructor.
   * 
   * @param memaccess the memory access object
   * @param address the table address
   */
  public CustomAlphabetTable(MemoryReadAccess memaccess, int address) {
    
    this.memaccess = memaccess;
    tableAddress = address;
  }
  
  /**
   * {@inheritDoc}
   */
  public short getA0Char(int index) {
    
    return memaccess.readUnsignedByte(tableAddress + index);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getA1Char(int index) {
    
    return memaccess.readUnsignedByte(tableAddress + ALPHABET_SIZE + index);
  }
  
  /**
   * {@inheritDoc}
   */
  public short getA2Char(int index) {
    
    return memaccess.readUnsignedByte(tableAddress + 2 * ALPHABET_SIZE
                                      + index);
  }
  
  /**
   * {@inheritDoc}
   */
  public final int getA0IndexOf(short zsciiChar) {

    for (int i = 0; i < ALPHABET_SIZE; i++) {
      
      if (getA0Char(i) == zsciiChar) return i;
    }
    return -1;
  }
  
  /**
   * {@inheritDoc}
   */
  public final int getA1IndexOf(short zsciiChar) {

    for (int i = 0; i < ALPHABET_SIZE; i++) {
      
      if (getA1Char(i) == zsciiChar) return i;
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public int getA2IndexOf(short zsciiChar) {

    for (int i = 0; i < ALPHABET_SIZE; i++) {
      
      if (getA2Char(i) == zsciiChar) return i;
    }
    return -1;
  }
  
  
  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(short zchar) {
   
    return 1 <= zchar && zchar <= 3;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShiftCharacter(short zchar) {

    return zchar == AlphabetTable.SHIFT_4 || zchar == AlphabetTable.SHIFT_5;
  }
}
