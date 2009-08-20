/*
 * Created on 2006/01/16
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
package org.zmpp.encoding;

import org.zmpp.base.Memory;

/**
 * If the story file header defines a custom alphabet table, instances
 * of this class are used to retrieve the alphabet characters.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class CustomAlphabetTable implements AlphabetTable {

  private static final int ALPHABET_SIZE = 26;
  private Memory memory;
  private int tableAddress;

  /**
   * Constructor.
   * @param memory the Memory object
   * @param address the table address
   */
  public CustomAlphabetTable(final Memory memory, final int address) {
    this.memory = memory;
    tableAddress = address;
  }

  /**
   * {@inheritDoc}
   */
  public char getA0Char(final byte zchar) {
    if (zchar == 0) {
      return ' ';
    }
    return (char) memory.readUnsigned8(tableAddress +
                                       (zchar - ALPHABET_START));
  }

  /**
   * {@inheritDoc}
   */
  public char getA1Char(final byte zchar) {
    if (zchar == 0) {
      return ' ';
    }
    return (char) memory.readUnsigned8(tableAddress +
                                       ALPHABET_SIZE +
                                       (zchar - ALPHABET_START));
  }

  /**
   * {@inheritDoc}
   */
  public char getA2Char(final byte zchar) {

    if (zchar == 0) {
      return ' ';
    }
    if (zchar == 7) {
      return (short) '\n';
    }
    return (char) memory.readUnsigned8(tableAddress + 2 * ALPHABET_SIZE +
                                       (zchar - ALPHABET_START));
  }

  /**
   * {@inheritDoc}
   */
  public final int getA0CharCode(final char zsciiChar) {
    for (int i = ALPHABET_START; i < ALPHABET_START + ALPHABET_SIZE; i++) {
      if (getA0Char((byte) i) == zsciiChar) {
        return i;
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public final int getA1CharCode(final char zsciiChar) {
    for (int i = ALPHABET_START; i < ALPHABET_START + ALPHABET_SIZE; i++) {

      if (getA1Char((byte) i) == zsciiChar) {
        return i;
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public final int getA2CharCode(final char zsciiChar) {
    for (int i = ALPHABET_START; i < ALPHABET_START + ALPHABET_SIZE; i++) {

      if (getA2Char((byte) i) == zsciiChar) {
        return i;
      }
    }
    return -1;
  }


  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(final char zchar) {
    return 1 <= zchar && zchar <= 3;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShift1(final char zchar) {
    return zchar == AlphabetTable.SHIFT_4;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShift2(final char zchar) {
    return zchar == AlphabetTable.SHIFT_5;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShiftLock(final char zchar) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShift(final char zchar) {
    return isShift1(zchar) || isShift2(zchar);
  }
}