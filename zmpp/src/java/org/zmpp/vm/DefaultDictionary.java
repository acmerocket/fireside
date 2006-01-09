/*
 * $Id$
 * 
 * Created on 10/14/2005
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

import java.util.HashMap;
import java.util.Map;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vmutil.ZCharDecoder;

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
   * A Z char decoder.
   */
  private ZCharDecoder decoder;
  
  /**
   * The lookup map.
   */
  private Map<String, Integer> lookupMap;
  
  /**
   * The maximum entry size.
   */
  private int maxEntrySize;
  
  /**
   * Constructor.
   * 
   * @param map the memory map
   * @param address the start address of the dictionary
   * @param converter a Z char decoder object
   */
  public DefaultDictionary(MemoryReadAccess map, int address,
                           ZCharDecoder decoder) {
    
    this.memaccess = map;
    this.address = address;
    this.decoder = decoder;
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
    
    // Unfortunately it seems that the maximum size of an entry is not equal 
    // to the size declared in the dictionary header, therefore we take
    // the maximum length of any token in the dictionary
    int entryLength = getEntryLength();
    entryLength = (maxEntrySize < entryLength) ? maxEntrySize : entryLength;
    //System.out.println("lookup(), token: '" + token + "' entrylen: "
    //                  + entryLength);
    
    // The lookup token can only be 6 characters long in version 3
    // and 9 in versions >= 4
    if (token.length() > entryLength) {
      
      entry = token.substring(0, entryLength);
    }
    //System.out.println("Truncated to entry: '" + entry + "'");
    
    if (lookupMap.containsKey(entry)) {
      
      //System.out.println("Found, entry: " + lookupMap.get(entry));
      return lookupMap.get(entry);
    }
    //System.out.println("Not found, token: '" + token + "'");
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
    int entryAddress;
    
    for (int i = 0, n = getNumberOfEntries(); i < n; i++) {
      
      entryAddress = getEntryAddress(i);      
      String str = decoder.decode2Unicode(memaccess, entryAddress);
      maxEntrySize = Math.max(str.length(), maxEntrySize);
      lookupMap.put(str, entryAddress);
    }
  }

  /**
   * Creates a string presentation of this dictionary.
   * 
   * @return the string presentation
   */
  public String toString() {

    StringBuilder buffer = new StringBuilder();
    int entryAddress;
    int i = 0;
    int n = getNumberOfEntries();
    
    while (true) {
      
      entryAddress = getEntryAddress(i);
      String str = decoder.decode2Unicode(memaccess, entryAddress);
      buffer.append(String.format("[%4d] '%-9s' ", (i + 1), str));
      i++;
      if ((i % 4) == 0) buffer.append("\n");
      if (i == n) break;
    }
    return buffer.toString();
  }
}
