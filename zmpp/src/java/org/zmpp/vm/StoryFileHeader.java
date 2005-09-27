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
  
  /**
   * Returns the story file version.
   * 
   * @return the story file version
   */
  public int getVersion() { return memaccess.readUnsignedByte(0x00); }
  
  /**
   * Returns the flags1 field.
   * 
   * @return the flags1 field
   */
  public int getFlags1() { return memaccess.readUnsignedByte(0x01); }
  
  /**
   * Returns the release number.
   * 
   * @return the release number
   */
  public int getRelease() { return memaccess.readUnsignedShort(0x02); }
  
  /**
   * Returns the high memory start address.
   * 
   * @return the start of the high memory
   */
  public int getHighMemAddress() { return memaccess.readUnsignedShort(0x04); }
  
  /**
   * Returns the program counter start address.
   * 
   * @return the PC start address
   */
  public int getProgramStart() { return memaccess.readUnsignedShort(0x06); }
  
  /**
   * Returns the dictionary's start address.
   * 
   * @return the dictionary start address
   */  
  public int getDictionaryAddress() {
    
    return memaccess.readUnsignedShort(0x08);
  }
  
  /**
   * Returns the object table's start address.
   * 
   * @return the object table's start address
   */
  public int getObjectTableAddress() {
    
    return memaccess.readUnsignedShort(0x0a);
  }
  
  /**
   * Returns the address of the global variables.
   * 
   * @return the global variables section
   */
  public int getGlobalsAddress() { return memaccess.readUnsignedShort(0x0c); }
  
  /**
   * Returns the static memory start address.
   * 
   * @return the start address of the static memory
   */
  public int getStaticsAddress() { return memaccess.readUnsignedShort(0x0e); }
  
  /**
   * Returns the flags2 field.
   * 
   * @return the flags2 field
   */
  public int getFlags2() { return memaccess.readUnsignedByte(0x10); }
  
  /**
   * Returns this game's serial number.
   * 
   * @return the serial number
   */
  public String getSerialNumber() { return extractAscii(0x12, 6); }
  
  /**
   * Returns the start address of the abbreviations section.
   * 
   * @return the abbreviations start address
   */
  public int getAbbreviationsAddress() {
    
    return memaccess.readUnsignedShort(0x18);
  }
  
  /**
   * Returns this story file's length.
   * 
   * @return the file length
   */
  public int getFileLength() {
    int fileLength = memaccess.readUnsignedShort(0x1a);
    if (getVersion() <= 3) fileLength *= 2;      
    return fileLength;
  }
  
  /**
   * Returns the checksum for the story file.
   * 
   * @return the checksum
   */
  public int getChecksum() { return memaccess.readUnsignedShort(0x1c); }
  
  /**
   * Returns the interpreter number.
   * 
   * @return the interpreter number
   */
  public int getInterpreter() { return memaccess.readUnsignedByte(0x1e); }
  
  /**
   * Returns the interpreter version.
   * 
   * @return the interpreter version
   */
  public int getInterpreterVersion() {
    
    return memaccess.readUnsignedByte(0x1f);
  }
  
  /**
   * Returns the revision number.
   * 
   * @return the revision number
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
