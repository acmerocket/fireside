/*
 * $Id$
 * 
 * Created on 2006/01/09
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
package org.zmpp.vm;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.encoding.ZCharDecoder;

/**
 * This class implements a user dictionary. The specification suggests that
 * lookup is implemented using linear search in case the user dictionary
 * is specified as unordered (negative number of entries) and in case of
 * ordered a binary search will be performed.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class UserDictionary extends AbstractDictionary {

  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param address the start address of the dictionary
   * @param converter a Z char decoder object
   */
  public UserDictionary(MemoryReadAccess memaccess, int address,
                        ZCharDecoder decoder) {
    
    super(memaccess, address, decoder);
  }

  /**
   * {@inheritDoc}
   */
  public int lookup(String token) {

    // We only implement linear search for the moment
    System.out.println("user dict, # entries: " + getNumberOfEntries());
    int n = Math.abs(getNumberOfEntries());
    String lookupToken = truncateToken(token);
    
    for (int i = 0; i < n; i++) {
      
      int entryAddress = getEntryAddress(i);
      String entry = getDecoder().decode2Unicode(getMemoryAccess(),
                                                 entryAddress);
      System.out.println("lookup(), compareto: " + entry);
      if (lookupToken.equals(entry)) {
        
        return entryAddress;
      }
    }
    return 0;
  }
  
  /**
   * {@inheritDoc}
   */
  protected int getMaxEntrySize() {
    
    return getEntryLength();
  }
}
