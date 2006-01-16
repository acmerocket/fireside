/*
 * $Id$
 * 
 * Created on 2005/01/15
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

import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.StoryFileHeader;

/**
 * This accent table is used in case that there is an extension header
 * that specifies that accent table.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class CustomAccentTable implements AccentTable {

  private StoryFileHeader fileheader;
  
  private MemoryAccess memaccess;

  /**
   * Constructor.
   * 
   * @param machine the machine object
   */
  public CustomAccentTable(StoryFileHeader fileheader,
      MemoryAccess memaccess) {
  
    this.fileheader = fileheader;
    this.memaccess = memaccess;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getLength() {
    
    int address = fileheader.getCustomAccentTable();
    int result = 0;
    if (address > 0) {
      
      return memaccess.readUnsignedByte(address);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public short getAccent(int index) {
    
    int address = fileheader.getCustomAccentTable();
    short result = '?';
    
    if (address > 0) {
      
      return memaccess.readShort(address + (index * 2) + 1);
    }
    return result;
  }
}
