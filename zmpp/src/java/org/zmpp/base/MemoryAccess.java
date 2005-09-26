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
 * This class manages read and write access to the byte array which contains
 * the story file data. It is the only means to read and manipulate the
 * memory map.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MemoryAccess implements MemoryReadAccess {

  /**
   * The data array containing the story file.
   */
  private byte[] data;  
  
  /**
   * Constructor.
   * 
   * @param data the story file data
   */
  public MemoryAccess(byte[] data) {
    
    this.data = data;    
  }
  
  /**
   * {@inheritDoc}
   */
  public long readUnsigned32(int address) {
  
    return (data[address] & 0xff) << 24 | (data[address + 1] & 0xff) << 16
           | (data[address + 2] & 0xff) << 8 | (data[address + 3] & 0xff);
  }
  
  /**
   * {@inheritDoc}
   */
  public int readUnsignedShort(int address) {
    
    return (data[address] & 0xff) << 8 | (data[address + 1] & 0xff);
  }
  
  /**
   * {@inheritDoc}
   */
  public short readShort(int address) {
    
    return (short) (data[address] << 8 | (data[address + 1] & 0xff));
  }
  
  /**
   * {@inheritDoc}
   */
  public short readUnsignedByte(int address) {
    
    return (short) (data[address] & 0xff);
  }    
}
