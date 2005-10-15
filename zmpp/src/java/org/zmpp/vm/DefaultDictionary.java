/*
 * $Id$
 * 
 * Created on 14.10.2005
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

import java.util.HashMap;
import java.util.Map;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vmutil.ZsciiString;

/**
 * This class implements a view on the dictionary within a memory map.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DefaultDictionary implements Dictionary {

  /**
   * The memory map.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * The dictionary start address.
   */
  private int address;
  
  /**
   * The lookup map.
   */
  private Map<String, Integer> lookupMap;
  
  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param address the start address of the dictionary
   */
  public DefaultDictionary(MemoryReadAccess map, int address) {
    
    this.memaccess = map;
    this.address = address;
    createLookupMap();
  }  

  /**
   * {@inheritDoc}
   */
  public int getNumberOfSeparators() {
    
    return memaccess.readUnsignedByte(address);
  }
  
  /**
   * {@inheritDoc}
   */
  public byte getSeparator(int i) {
    
    return (byte) memaccess.readUnsignedByte(address + i + 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getEntryLength() {
    
    return memaccess.readUnsignedByte(address + getNumberOfSeparators() + 1);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getNumberOfEntries() {
    
    return memaccess.readUnsignedShort(address + getNumberOfSeparators() + 2);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getEntryAddress(int entryNum) {
   
    int headerSize = getNumberOfSeparators() + 4;    
    return address + headerSize + entryNum * getEntryLength();
  }
  
  /**
   * {@inheritDoc}
   */
  public int lookup(String token) {
    
    String entry = token;
    
    // The lookup token can only be 6 characters long in version 3
    if (token.length() > 6) {
      
      entry = token.substring(0, 6);
    }
    
    if (lookupMap.containsKey(entry)) {
      return lookupMap.get(entry);
    }
    return 0;
  }
  
  /**
   * Create the dictionary lookup map. The standards document suggests to
   * convert the tokens into ZSCII strings and look them up in the dictionary
   * by a binary search algorithm, which results in a O(log n) search algorithm,
   * instead I convert the dictionary strings into Java strings and put them
   * into a (entry - address) map, which is easier to handle and is O(1).
   * Generating it once at initialization is safe because the dictionary is in
   * static memory and does not change at runtime.
   */
  private void createLookupMap() {
    
    lookupMap = new HashMap<String, Integer>();
    ZsciiString zstr;
    int entryAddress;
    
    for (int i = 0, n = getNumberOfEntries(); i < n; i++) {
      
      entryAddress = getEntryAddress(i);
      zstr = new ZsciiString(memaccess, entryAddress);
      lookupMap.put(zstr.toString(), entryAddress);
    }
  }  
}