/*
 * Created on 10/14/2005
 * Copyright 2005-2009 by Wei-ju Wu
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zmpp.vm;

import org.zmpp.base.Memory;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharEncoder;

/**
 * This class implements a view on the dictionary within a memory map.
 * Since it takes the implementations of getN
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultDictionary extends AbstractDictionary {

  /** The maximum entry size. */
  private int maxEntrySize;

  /**
   * Constructor.
   * @param memory the memory object
   * @param address the start address of the dictionary
   * @param converter a Z char decoder object
   * @param sizes a sizes object
   */
  public DefaultDictionary(Memory memory, int address,
                           ZCharDecoder decoder,
                           ZCharEncoder encoder,
                           DictionarySizes sizes) {
    super(memory, address, decoder, encoder, sizes);
    /*
    for (int i = 0; i < this.getNumberOfEntries(); i++) {
      int entryAddress = getEntryAddress(i);
      String str = decoder.decode2Zscii(memory, entryAddress, sizes.getNumEntryBytes());
      System.out.printf("%d: '%s' { 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x } " + 
          "{ 0x%02x, 0x%02x, 0x%02x, ... }\n",
          i, str, (int) memory.readUnsigned8(entryAddress),
          (int) memory.readUnsigned8(entryAddress + 1),
          (int) memory.readUnsigned8(entryAddress + 2),
          (int) memory.readUnsigned8(entryAddress + 3),
          (int) memory.readUnsigned8(entryAddress + 4),
          (int) memory.readUnsigned8(entryAddress + 5),
          (int) str.charAt(0),
          (int) (str.length() > 1 ? str.charAt(1) : 0),
          (int) (str.length() > 2 ? str.charAt(2) : 0)
          );
    }*/
  }

  /**
   * {@inheritDoc}
   */
  public int lookup(final String token) {
    // debug
    /*
    byte[] tokenBytes = truncateTokenToBytes(token);
    System.out.printf("token = %s tb = { 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x }\n",
        token,
        (int) tokenBytes[0], (int) tokenBytes[1], (int) tokenBytes[2], (int) tokenBytes[3],
        (int) tokenBytes[4], (int) tokenBytes[5]);
        */
    return lookupBinary(truncateTokenToBytes(token), 0,
                        getNumberOfEntries() - 1);
  }

  /**
   * Recursive binary search to find an input word in the dictionary.
   * @param tokenBytes the byte array containing the input word
   * @param left the left index
   * @param right the right index
   * @return the entry address
   */
  private int lookupBinary(byte[] tokenBytes, int left, int right) {
    if (left > right) return 0;
    int middle = left + (right - left) / 2;
    int entryAddress = getEntryAddress(middle);
    int res = tokenMatch(tokenBytes, entryAddress);
    if (res < 0) {
      return lookupBinary(tokenBytes, left, middle - 1);
    } else if (res > 0) {
      return lookupBinary(tokenBytes, middle + 1, right);
    } else {
      return entryAddress;
    }
  }

  /**
   * {@inheritDoc}
   */
  protected int getMaxEntrySize() { return maxEntrySize; }
}
