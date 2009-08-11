/*
 * Copyright 2005-2009 Wei-ju Wu
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
 * This class manages read and write access to the byte array which contains
 * the story file data. It is the only means to read and manipulate the
 * memory map.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface Memory {

  // ************************************************************************
  // ****
  // **** Read access
  // ****
  // *****************************
  /**
   * Reads the unsigned 16 bit word at the specified address.
   * 
   * @param address the address
   * @return the 16 bit unsigned value as int
   */
  char readUnsigned16(int address);
  
  /**
   * Returns the unsigned 8 bit value at the specified address.
   * 
   * @param address the address
   * @return the 8 bit unsigned value
   */
  char readUnsigned8(int address); 
  
  // ************************************************************************
  // ****
  // **** Write access
  // ****
  // *****************************
  /**
   * Writes an unsigned 16 bit value to the specified address.
   * 
   * @param address the address to write to
   * @param value the value to write
   */
  void writeUnsigned16(int address, char value);
  
  /**
   * Writes an unsigned byte value to the specified address.
   * 
   * @param address the address to write to
   * @param value the value to write
   */
  void writeUnsigned8(int address, char value);
  
  /**
   * A rather common operation: copy the specified number of bytes from
   * the offset to a taret array.
   * @param dstData the destination array
   * @param dstOffset the offset in the destinations array
   * @param srcOffset the offset in the source
   * @param numBytes the number of bytes to copy
   */
  void copyBytesToArray(byte[] dstData, int dstOffset,
                        int srcOffset, int numBytes);
  
  /**
   * Copy the specified number of bytes from the source array to this
   * Memory object
   * @param srcData the source array
   * @param srcOffset the source offset
   * @param dstOffset the destination offset
   * @param numBytes the number of bytes to copy
   */
  void copyBytesFromArray(byte[] srcData, int srcOffset,
                          int dstOffset, int numBytes);

  /**
   * Copy the specified number of bytes from the specified source Memory object.
   * @param srcMem the source Memory object
   * @param srcOffset the source offset
   * @param dstOffset the destination offset
   * @param numBytes the number of bytes to copy
   */
  void copyBytesFromMemory(Memory srcMem, int srcOffset, int dstOffset,
                           int numBytes);
  
  /**
   * Copy an area of bytes efficiently. Since the System.arraycopy() is used,
   * we do not have to worry about overlapping areas and can take advantage
   * of the performance gain.
   * @param src the source address
   * @param dst the destination address
   * @param numBytes the number of bytes
   */
  void copyArea(int src, int dst, int numBytes);
}
