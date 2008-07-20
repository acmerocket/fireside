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
   * 
   * @param data the story file data
   */
  public DefaultMemory(final byte[] data) {
    
    super();
    this.data = data;    
  }
  
  /**
   * {@inheritDoc}
   */
  public long readUnsigned32(final int address) {

      final long a24 = (data[address] & 0xffL) << 24;
      final long a16 = (data[address + 1] & 0xffL) << 16;
      final long a8  = (data[address + 2] & 0xffL) << 8;
      final long a0  = (data[address + 3] & 0xffL);

      return a24 | a16 | a8 | a0;
  }
  
  /**
   * {@inheritDoc}
   */
  public char readUnsigned16(final int address) {    
    return (char)
      (((data[address] & 0xff) << 8 | (data[address + 1] & 0xff)) & 0xffff);
  }
  
  /**
   * {@inheritDoc}
   */
  public short readSigned16(final int address) {
    return (short) (data[address] << 8 | (data[address + 1] & 0xff));
  }
  
  /**
   * {@inheritDoc}
   */
  public short readUnsigned8(final int address) {
    return (short) (data[address] & 0xff);
  }
  
  /**
   * {@inheritDoc}
   */
  public byte readSigned8(final int address) {
    return data[address];
  }
  
  /**
   * Writes an unsigned 16 bit value to the specified address.
   * 
   * @param address the address to write to
   * @param value the value to write
   */
  public void writeUnsigned16(final int address, final char value) {
    data[address] = (byte) ((value & 0xff00) >> 8);
    data[address + 1] = (byte) (value & 0xff);
  }
  
  /**
   * Writes a short value to the memory.
   * 
   * @param address the address
   * @param value the value
   */
  public void writeSigned16(final int address, final short value) {
    
    data[address] = (byte) ((value & 0xff00) >>> 8);
    data[address + 1] = (byte) (value & 0xff);
  }
  
  /**
   * Writes an unsigned byte value to the specified address.
   * 
   * @param address the address to write to
   * @param value the value to write
   */
  public void writeUnsigned8(final int address, final short value) {
    
    data[address] = (byte) (value & 0xff);
  }
  
  /**
   * Writes a byte value to the specified address.
   * 
   * @param address the address
   * @param value the value
   */
  public void writeSigned8(final int address, final byte value) {
    data[address] = value;
  }
  
  /**
   * Writes an unsigned 32 bit value to the specified address.
   * 
   * @param address the address to write to
   * @param value the value to write
   */
  public void writeUnsigned32(final int address, final long value) {
    data[address] = (byte) ((value & 0xff000000) >> 24);
    data[address + 1] = (byte) ((value & 0x00ff0000) >> 16);
    data[address + 2] = (byte) ((value & 0x0000ff00) >> 8);
    data[address + 3] = (byte) (value & 0x000000ff);
  }
}
