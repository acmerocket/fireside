/*
 * $Id$
 * 
 * Copyright 2005-2007 by Wei-ju Wu
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
package test.zmpp.encoding;

import junit.framework.TestCase;

import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV1;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.DefaultAlphabetTable;

public class AlphabetTableTest extends TestCase {

  private AlphabetTable v1Table = new AlphabetTableV1();
  private AlphabetTable v2Table = new AlphabetTableV2();
  private AlphabetTable defaultTable = new DefaultAlphabetTable();
  
  public void testChar0IsSpace() {
    
    assertEquals(' ', v1Table.getA0Char((byte) 0));
    assertEquals(' ', v1Table.getA1Char((byte) 0));
    assertEquals(' ', v1Table.getA2Char((byte) 0));

    assertEquals(' ', v2Table.getA0Char((byte) 0));
    assertEquals(' ', v2Table.getA1Char((byte) 0));
    assertEquals(' ', v2Table.getA2Char((byte) 0));

    assertEquals(' ', defaultTable.getA0Char((byte) 0));
    assertEquals(' ', defaultTable.getA1Char((byte) 0));
    assertEquals(' ', defaultTable.getA2Char((byte) 0));
  }

  public void testChar1IsNewLineInV1() {
    
    assertEquals('\n', v1Table.getA0Char((byte) 1));
    assertEquals('\n', v1Table.getA1Char((byte) 1));
    assertEquals('\n', v1Table.getA2Char((byte) 1));
  }
  
  public void testIsAbbreviation() {
    
    assertFalse(v1Table.isAbbreviation((short)1));
    assertFalse(v1Table.isAbbreviation((short)2));
    assertFalse(v1Table.isAbbreviation((short)3));

    assertTrue(v2Table.isAbbreviation((short)1));
    assertFalse(v2Table.isAbbreviation((short)2));
    assertFalse(v2Table.isAbbreviation((short)3));
  }

  public void testShiftChars() {
    
    assertTrue(v1Table.isShift(AlphabetTable.SHIFT_2));
    assertTrue(v1Table.isShift(AlphabetTable.SHIFT_3));
    assertTrue(v2Table.isShift(AlphabetTable.SHIFT_2));
    assertTrue(v2Table.isShift(AlphabetTable.SHIFT_3));
    assertFalse(v1Table.isShiftLock(AlphabetTable.SHIFT_2));
    assertFalse(v1Table.isShiftLock(AlphabetTable.SHIFT_3));
    assertFalse(v2Table.isShiftLock(AlphabetTable.SHIFT_2));
    assertFalse(v2Table.isShiftLock(AlphabetTable.SHIFT_3));

    assertFalse(v1Table.isShiftLock(AlphabetTable.SHIFT_2));
    assertFalse(v1Table.isShiftLock(AlphabetTable.SHIFT_3));
    assertFalse(v2Table.isShiftLock(AlphabetTable.SHIFT_2));
    assertFalse(v2Table.isShiftLock(AlphabetTable.SHIFT_3));
    assertTrue(v1Table.isShiftLock(AlphabetTable.SHIFT_4));
    assertTrue(v1Table.isShiftLock(AlphabetTable.SHIFT_5));
    assertTrue(v2Table.isShiftLock(AlphabetTable.SHIFT_4));
    assertTrue(v2Table.isShiftLock(AlphabetTable.SHIFT_5));
    
    assertFalse(defaultTable.isShift(AlphabetTable.SHIFT_2));
    assertFalse(defaultTable.isShift(AlphabetTable.SHIFT_3));
    assertTrue(defaultTable.isShift(AlphabetTable.SHIFT_4));
    assertTrue(defaultTable.isShift(AlphabetTable.SHIFT_5));
    assertFalse(defaultTable.isShiftLock(AlphabetTable.SHIFT_2));
    assertFalse(defaultTable.isShiftLock(AlphabetTable.SHIFT_3));
    assertFalse(defaultTable.isShiftLock(AlphabetTable.SHIFT_4));
    assertFalse(defaultTable.isShiftLock(AlphabetTable.SHIFT_5));
  }
}
