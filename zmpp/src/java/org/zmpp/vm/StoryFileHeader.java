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
 * This class defines the structure of a story file header in the Z-machine.
 * It is designed as a read only view to the byte array containing the
 * story file data.
 * By this means, changes in the memory map will be implicitly change
 * the header structure.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StoryFileHeader {

  /**
   * The memory map.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * Constructor.
   * 
   * @param memaccess a MemoryReadAccess object
   */
  public StoryFileHeader(MemoryReadAccess memaccess) {
    this.memaccess = memaccess;
  }
  
  public int getVersion() { return memaccess.readUnsignedByte(0x00); }
  public int getFlags1() { return memaccess.readUnsignedByte(0x01); }
  public int getBaseAddress() { return memaccess.readUnsignedShort(0x04); }
  public int getProgramStart() { return memaccess.readUnsignedShort(0x06); }
  public int getDictionaryAddress() { return memaccess.readUnsignedShort(0x08); }
  public int getObjectTableAddress() { return memaccess.readUnsignedShort(0x0a); }
  public int getGlobalsAddress() { return memaccess.readUnsignedShort(0x0c); }
  public int getStaticsAddress() { return memaccess.readUnsignedShort(0x0e); }
  public int getFlags2() { return memaccess.readUnsignedByte(0x10); }
  public String getSerialNumber() { return extractAscii(0x12, 6); }
  public int getAbbreviationsAddress() { return memaccess.readUnsignedShort(0x18); }
  public int getFileLength() {
    int fileLength = memaccess.readUnsignedShort(0x1a);
    if (getVersion() <= 3) fileLength *= 2;      
    return fileLength;
  }
  public int getChecksum() { return memaccess.readUnsignedShort(0x1c); }
  public int getInterpreter() { return memaccess.readUnsignedByte(0x1e); }
  public int getInterpreterVersion() { return memaccess.readUnsignedByte(0x1f); }
  public int getRevision() { return memaccess.readUnsignedShort(0x32); }  

  private String extractAscii(int address, int length) {
    
    StringBuilder builder = new StringBuilder();
    for (int i = address; i < address + length; i++) {
      
      builder.append((char) memaccess.readUnsignedByte(i));
    }
    return builder.toString();
  }
}
