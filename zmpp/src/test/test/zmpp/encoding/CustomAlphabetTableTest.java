/*
 * $Id$
 * 
 * Created on 2006/01/17
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
package test.zmpp.encoding;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryReadAccess;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.CustomAlphabetTable;

public class CustomAlphabetTableTest extends MockObjectTestCase {

  private Mock mockMemAccess;
  private MemoryReadAccess memaccess;
  
  private AlphabetTable alphabetTable;
  
  protected void setUp() throws Exception {
    
    mockMemAccess = mock(MemoryReadAccess.class);
    memaccess = (MemoryReadAccess) mockMemAccess.proxy();
    alphabetTable = new CustomAlphabetTable(memaccess, 1000);
  }
  
  public void testGetA0Char() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA0Char(0));

    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1006)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA0Char(6));
  }

  public void testGetA1Char() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1026)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA1Char(0));

    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1032)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA1Char(6));
  }

  public void testGetA2Char() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1052)).will(returnValue((short) 3));
    assertEquals(3, alphabetTable.getA2Char(0));

    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1058)).will(returnValue((short) 2));
    assertEquals(2, alphabetTable.getA2Char(6));
  }
  
  public void testA0IndexOfNotFound() {

    for (int i = 0; i < 26; i++) {
      
      mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1000 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA0IndexOf((short) '@'));
  }

  public void testA1IndexOfNotFound() {

    for (int i = 0; i < 26; i++) {
      
      mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1026 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA1IndexOf((short) '@'));
  }

  public void testA2IndexOfNotFound() {

    for (int i = 0; i < 26; i++) {
      
      mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1052 + i)).will(returnValue((short) 'a'));
    }
    assertEquals(-1, alphabetTable.getA2IndexOf((short) '@'));
  }
}
