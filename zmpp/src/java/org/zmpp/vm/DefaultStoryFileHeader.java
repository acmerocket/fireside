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
package org.zmpp.vm;

import org.zmpp.base.MemoryReadAccess;

/**
 * This is the default implementation of the StoryFileHeader interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DefaultStoryFileHeader implements StoryFileHeader {

  /**
   * The memory map.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * Constructor.
   * 
   * @param memaccess a MemoryReadAccess object
   */
  public DefaultStoryFileHeader(MemoryReadAccess memaccess) {
    
    this.memaccess = memaccess;
  }

  /**
   * {@inheritDoc}
   */
  public int getVersion() { return memaccess.readUnsignedByte(0x00); }
  
  /**
   * {@inheritDoc}
   */
  public int getFlags1() { return memaccess.readUnsignedByte(0x01); }
  
  /**
   * {@inheritDoc}
   */
  public int getRelease() { return memaccess.readUnsignedShort(0x02); }
  
  /**
   * {@inheritDoc}
   */
  public int getHighMemAddress() { return memaccess.readUnsignedShort(0x04); }
  
  /**
   * {@inheritDoc}
   */
  public int getProgramStart() { return memaccess.readUnsignedShort(0x06); }
  
  /**
   * {@inheritDoc}
   */
  public int getDictionaryAddress() {
    
    return memaccess.readUnsignedShort(0x08);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getObjectTableAddress() {
    
    return memaccess.readUnsignedShort(0x0a);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getGlobalsAddress() { return memaccess.readUnsignedShort(0x0c); }
  
  /**
   * {@inheritDoc}
   */
  public int getStaticsAddress() { return memaccess.readUnsignedShort(0x0e); }
  
  /**
   * {@inheritDoc}
   */
  public int getFlags2() { return memaccess.readUnsignedByte(0x10); }
  
  /**
   * {@inheritDoc}
   */
  public String getSerialNumber() { return extractAscii(0x12, 6); }
  
  /**
   * {@inheritDoc}
   */
  public int getAbbreviationsAddress() {
    
    return memaccess.readUnsignedShort(0x18);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getFileLength() {
    int fileLength = memaccess.readUnsignedShort(0x1a);
    if (getVersion() <= 3) fileLength *= 2;      
    return fileLength;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getChecksum() { return memaccess.readUnsignedShort(0x1c); }
  
  /**
   * {@inheritDoc}
   */
  public int getInterpreter() { return memaccess.readUnsignedByte(0x1e); }
  
  /**
   * {@inheritDoc}
   */
  public int getInterpreterVersion() {
    
    return memaccess.readUnsignedByte(0x1f);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getRevision() { return memaccess.readUnsignedShort(0x32); }  

  /**
   * Extract an ASCII string of the specified length starting at the specified
   * address.
   * 
   * @param address the start address
   * @param length the length of the ASCII string
   * @return the ASCII string at the specified position
   */
  private String extractAscii(int address, int length) {
    
    StringBuilder builder = new StringBuilder();
    for (int i = address; i < address + length; i++) {
      
      builder.append((char) memaccess.readUnsignedByte(i));
    }
    return builder.toString();
  }
}
