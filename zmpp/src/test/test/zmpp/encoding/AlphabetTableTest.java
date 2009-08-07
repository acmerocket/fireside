/*
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
package test.zmpp.encoding;

import org.junit.Test;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV1;
import org.zmpp.encoding.AlphabetTableV2;
import org.zmpp.encoding.DefaultAlphabetTable;

import static org.junit.Assert.*;

/**
 * Test for alphabet table behaviour
 * @author Wei-ju Wu
 * @version 1.5
 */
public class AlphabetTableTest {

  private AlphabetTable v1Table = new AlphabetTableV1();
  private AlphabetTable v2Table = new AlphabetTableV2();
  private AlphabetTable defaultTable = new DefaultAlphabetTable();
  
  @Test
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

  @Test
  public void testChar1IsNewLineInV1() {
    
    assertEquals('\n', v1Table.getA0Char((byte) 1));
    assertEquals('\n', v1Table.getA1Char((byte) 1));
    assertEquals('\n', v1Table.getA2Char((byte) 1));
  }
  
  @Test
  public void testIsAbbreviation() {
    assertFalse(v1Table.isAbbreviation((char) 1));
    assertFalse(v1Table.isAbbreviation((char) 2));
    assertFalse(v1Table.isAbbreviation((char) 3));

    assertTrue(v2Table.isAbbreviation((char)  1));
    assertFalse(v2Table.isAbbreviation((char) 2));
    assertFalse(v2Table.isAbbreviation((char) 3));
  }

  @Test
  public void testShiftChars() {
    
    assertTrue(v1Table.isShift((char) AlphabetTable.SHIFT_2));
    assertTrue(v1Table.isShift((char) AlphabetTable.SHIFT_3));
    assertTrue(v2Table.isShift((char) AlphabetTable.SHIFT_2));
    assertTrue(v2Table.isShift((char) AlphabetTable.SHIFT_3));
    assertFalse(v1Table.isShiftLock((char) AlphabetTable.SHIFT_2));
    assertFalse(v1Table.isShiftLock((char) AlphabetTable.SHIFT_3));
    assertFalse(v2Table.isShiftLock((char) AlphabetTable.SHIFT_2));
    assertFalse(v2Table.isShiftLock((char) AlphabetTable.SHIFT_3));

    assertFalse(v1Table.isShiftLock((char) AlphabetTable.SHIFT_2));
    assertFalse(v1Table.isShiftLock((char) AlphabetTable.SHIFT_3));
    assertFalse(v2Table.isShiftLock((char) AlphabetTable.SHIFT_2));
    assertFalse(v2Table.isShiftLock((char) AlphabetTable.SHIFT_3));
    assertTrue(v1Table.isShiftLock((char) AlphabetTable.SHIFT_4));
    assertTrue(v1Table.isShiftLock((char) AlphabetTable.SHIFT_5));
    assertTrue(v2Table.isShiftLock((char) AlphabetTable.SHIFT_4));
    assertTrue(v2Table.isShiftLock((char) AlphabetTable.SHIFT_5));
    
    assertFalse(defaultTable.isShift((char) AlphabetTable.SHIFT_2));
    assertFalse(defaultTable.isShift((char) AlphabetTable.SHIFT_3));
    assertTrue(defaultTable.isShift((char) AlphabetTable.SHIFT_4));
    assertTrue(defaultTable.isShift((char) AlphabetTable.SHIFT_5));
    assertFalse(defaultTable.isShiftLock((char) AlphabetTable.SHIFT_2));
    assertFalse(defaultTable.isShiftLock((char) AlphabetTable.SHIFT_3));
    assertFalse(defaultTable.isShiftLock((char) AlphabetTable.SHIFT_4));
    assertFalse(defaultTable.isShiftLock((char) AlphabetTable.SHIFT_5));
  }
}
