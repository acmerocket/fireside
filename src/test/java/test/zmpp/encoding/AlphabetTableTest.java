/*
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
