/*
 * $Id$
 * 
 * Created on 24.09.2005
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
import org.zmpp.vmutil.ZsciiConverter;
import org.zmpp.vmutil.ZsciiConverter.Alphabet;

/**
 * This class implements a view on the dictionary within a memory map.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Dictionary {

  /**
   * The dictionary version.
   */
  private int version;
  
  /**
   * The memory map.
   */
  private MemoryReadAccess map;
  
  /**
   * The dictionary start address.
   */
  private int address;
  
  /**
   * The converter object.
   */
  private ZsciiConverter converter;

  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param version the story file version
   * @param address the start address of the dictionary
   */
  public Dictionary(MemoryReadAccess map, int version, int address) {
    
    this.map = map;
    this.address = address;
    this.version = version;
    this.converter = new ZsciiConverter(this.version);
  }  
  
  public int getNumberOfSeparators() {
    
    return map.readUnsignedByte(address);
  }
  
  public int getSeparator(int i) {
    
    byte zchar = (byte) map.readUnsignedByte(address + i);
    return ZsciiConverter.decode(Alphabet.A0, zchar);
  }
  
  public int getEntryLength() {
    
    return map.readUnsignedByte(address + getNumberOfSeparators() + 1);
  }
  
  public int getNumberOfEntries() {
    
    return map.readUnsignedShort(address + getNumberOfSeparators() + 2);
  }
  
  /**
   * Returns the entry text at the specified position.
   * 
   * @param entryNum entry number between (0 - getNumberOfEntries() - 1)
   * @return
   */
  public String getEntryString(int entryNum) {
    
    int entryAddress = address + getNumberOfSeparators() + 4 +
                       entryNum * getEntryLength();
    return converter.convert(map, entryAddress);
  }
}
