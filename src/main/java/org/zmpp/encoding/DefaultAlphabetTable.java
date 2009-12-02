/*
 * Created on 2006/01/12
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

/**
 * The default alphabet table implementation.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultAlphabetTable implements AlphabetTable {

  private static final String A0CHARS = "abcdefghijklmnopqrstuvwxyz";
  private static final String A1CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String A2CHARS = " \n0123456789.,!?_#'\"/\\-:()";

  /**
   * {@inheritDoc}
   */
  public char getA0Char(final byte zchar) {
    if (zchar == 0) return ' ';
    return A0CHARS.charAt(zchar - ALPHABET_START);
  }

  /**
   * {@inheritDoc}
   */
  public char getA1Char(final byte zchar) {
    if (zchar == 0) return ' ';
    return A1CHARS.charAt(zchar - ALPHABET_START);
  }

  /**
   * {@inheritDoc}
   */
  public char getA2Char(final byte zchar) {
    if (zchar == 0) return ' ';
    return A2CHARS.charAt(zchar - ALPHABET_START);
  }

  /**
   * {@inheritDoc}
   */
  public final int getA0CharCode(final char zsciiChar) {
    return getCharCodeFor(A0CHARS, zsciiChar);
  }

  /**
   * {@inheritDoc}
   */
  public final int getA1CharCode(final char zsciiChar) {
    return getCharCodeFor(A1CHARS, zsciiChar);
  }

  /**
   * {@inheritDoc}
   */
  public int getA2CharCode(final char zsciiChar) {
    return getCharCodeFor(A2CHARS, zsciiChar);
  }

  /**
   * Returns the character code for the specified ZSCII character by searching
   * the index in the specified chars string.
   * @param chars the search string
   * @param zsciiChar the ZSCII character
   * @return the character code, which is the index of the character in chars
   *         or -1 if not found
   */
  protected static int getCharCodeFor(final String chars,
      final char zsciiChar) {
    int index = chars.indexOf(zsciiChar);
    if (index >= 0) {
      index += ALPHABET_START;
    }
    return index;
  }


  /**
   * {@inheritDoc}
   */
  public boolean isShift1(final char zchar) { return zchar == SHIFT_4; }

  /**
   * {@inheritDoc}
   */
  public boolean isShift2(final char zchar) { return zchar == SHIFT_5; }

  /**
   * {@inheritDoc}
   */
  public boolean isShift(final char zchar) {
    return isShift1(zchar) || isShift2(zchar);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isShiftLock(final char zchar) { return false; }

  /**
   * {@inheritDoc}
   */
  public boolean isAbbreviation(final char zchar) {
    return 1 <= zchar && zchar <= 3;
  }
}
