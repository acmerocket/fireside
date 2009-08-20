/*
 * Created on 2006/01/09
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
 * This class implements a user dictionary. The specification suggests that
 * lookup is implemented using linear search in case the user dictionary
 * is specified as unordered (negative number of entries) and in case of
 * ordered a binary search will be performed.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class UserDictionary extends AbstractDictionary {

  /**
   * Constructor.
   * @param memory the Memory object
   * @param address the start address of the dictionary
   * @param decoder a ZCharDecoder object
   * @param encoder a ZCharEncoder object
   */
  public UserDictionary(Memory memory, int address,
                        ZCharDecoder decoder, ZCharEncoder encoder) {
    super(memory, address, decoder, encoder, new DictionarySizesV4ToV8());
  }

  /** {@inheritDoc} */
  public int lookup(final String token) {
    // We only implement linear search for user dictionaries
    final int n = Math.abs(getNumberOfEntries());
    final byte tokenBytes[] = truncateTokenToBytes(token);
    for (int i = 0; i < n; i++) {
      final int entryAddress = getEntryAddress(i);
      if (tokenMatch(tokenBytes, entryAddress) == 0) { return entryAddress; }
    }
    return 0;
  }
}