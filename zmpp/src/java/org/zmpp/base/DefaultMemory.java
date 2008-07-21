/*
 * $Id: DefaultMemoryAccess.java 520 2007-11-13 19:14:51Z weiju $
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
 * This class is the default implementation for MemoryAccess.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultMemory implements Memory {

  /**
   * The data array containing the story file.
   */
  private byte[] data;  
  
  /**
   * Constructor.
   * @param data the story file data
   */
  public DefaultMemory(final byte[] data) {    
    super();
    this.data = data;    
  }
  
  /** {@inheritDoc} */
  public char readUnsigned16(final int address) {    
    return (char)
      (((data[address] & 0xff) << 8 | (data[address + 1] & 0xff)) & 0xffff);
  }

  /** {@inheritDoc} */
  public char readUnsigned8(final int address) {
    return (char) (data[address] & 0xff);
  }
  
  /** {@inheritDoc} */
  public void writeUnsigned16(final int address, final char value) {
    data[address] = (byte) ((value & 0xff00) >> 8);
    data[address + 1] = (byte) (value & 0xff);
  }
  
  /** {@inheritDoc} */
  public void writeUnsigned8(final int address, final char value) { 
    data[address] = (byte) (value & 0xff);
  }

  /** {@inheritDoc} */
  public void copyBytesToArray(byte[] dstData, int dstOffset,
                               int srcOffset, int numBytes) {
    System.arraycopy(data, srcOffset, dstData, dstOffset, numBytes);
  }

  /** {@inheritDoc} */
  public void copyBytesFromArray(byte[] srcData, int srcOffset,
                                 int dstOffset, int numBytes) {
    System.arraycopy(srcData, srcOffset, data, dstOffset, numBytes);
  }

  /** {@inheritDoc} */
  public void copyBytesFromMemory(Memory srcMem, int srcOffset, int dstOffset,
                                  int numBytes) {
    // This copy method might not be as efficient, because the source
    // memory object could be based on something else than a byte array
    for (int i = 0; i < numBytes; i++) {
      data[dstOffset + i] = (byte) (srcMem.readUnsigned8(srcOffset + i) & 0xff);
    }
  }

  /** {@inheritDoc} */
  public void copyArea(int src, int dst, int numBytes) {
    System.arraycopy(data, src, data, dst, numBytes);
  }
}
