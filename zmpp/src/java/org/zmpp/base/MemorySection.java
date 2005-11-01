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
package org.zmpp.base;

/**
 * A MemorySection object wraps a reference to a MemoryAccess object, a length
 * and a start to support subsections within memory.
 * All access functions will be relative to the initialized start offset
 * within the global memory.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MemorySection implements MemoryAccess {

  private MemoryAccess memaccess;
  private int start;
  private int length;
  
  public MemorySection(MemoryAccess memaccess, int start, int length) {
    
    this.memaccess = memaccess;
    this.start = start;
    this.length = length;
  }
  
  /**
   * Returns the length of this object in bytes.
   * 
   * @return the length in bytes
   */
  public int getLength() {
    
    return length;
  }
  
  /**
   * {@inheritDoc}
   */
  public void writeUnsignedShort(int address, int value) {
    
    memaccess.writeUnsignedShort(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeShort(int address, short value) {
    
    memaccess.writeShort(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeUnsignedByte(int address, short value) {

    memaccess.writeUnsignedByte(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeByte(int address, byte value) {

    memaccess.writeByte(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public void writeUnsigned32(int address, long value) {

    memaccess.writeUnsigned32(address + start, value);
  }

  /**
   * {@inheritDoc}
   */
  public long readUnsigned32(int address) {
    
    return memaccess.readUnsigned32(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public int readUnsignedShort(int address) {
    
    return memaccess.readUnsignedShort(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public short readShort(int address) {
    
    return memaccess.readShort(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public short readUnsignedByte(int address) {
    
    return memaccess.readUnsignedByte(address + start);
  }

  /**
   * {@inheritDoc}
   */
  public byte readByte(int address) {
    
    return memaccess.readByte(address + start);
  }
}