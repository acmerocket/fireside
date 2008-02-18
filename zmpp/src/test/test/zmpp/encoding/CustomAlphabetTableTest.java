/*
 * $Id$
 * 
 * Created on 2006/01/17
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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.CustomAlphabetTable;

public class CustomAlphabetTableTest extends MockObjectTestCase {

  private Mock mockMemory;
  private Memory memory;
  
  private AlphabetTable alphabetTable;
  
  protected void setUp() throws Exception {
    mockMemory = mock(Memory.class);
    memory = (Memory) mockMemory.proxy();
    alphabetTable = new CustomAlphabetTable(memory, 1000);
  }
  
  public void testGetA0Char() {
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA0Char((byte) 6));

    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1006)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA0Char((byte) 12));
    
    assertEquals(' ', alphabetTable.getA0Char((byte) 0));
  }

  public void testGetA1Char() {
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1026)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA1Char((byte) 6));

    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1032)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA1Char((byte) 12));

    assertEquals(' ', alphabetTable.getA1Char((byte) 0));
  }

  public void testGetA2Char() {
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1052)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA2Char((byte) 6));

    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1058)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA2Char((byte) 12));

    assertEquals(' ', alphabetTable.getA2Char((byte) 0));
    assertEquals('\n', alphabetTable.getA2Char((byte) 7));
  }
  
  public void testA0IndexOfNotFound() {
    for (int i = 0; i < 26; i++) {
      mockMemory.expects(once()).method("readUnsignedByte").with(eq(1000 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA0CharCode('@'));
  }

  public void testA1IndexOfNotFound() {
    for (int i = 0; i < 26; i++) {
      mockMemory.expects(once()).method("readUnsignedByte").with(eq(1026 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA1CharCode('@'));
  }

  public void testA2IndexOfNotFound() {
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(1052)).will(returnValue((short) 'a'));
    // char 7 is directly returned !!
    for (int i = 2; i < 26; i++) {
      mockMemory.expects(once()).method("readUnsignedByte").with(eq(1052 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA2CharCode('@'));
  }
}
