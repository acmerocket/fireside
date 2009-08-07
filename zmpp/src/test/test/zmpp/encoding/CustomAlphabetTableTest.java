/*
 * Created on 2006/01/17
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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.zmpp.base.Memory;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.CustomAlphabetTable;

/**
 * A test class for the CustomAlphabet class.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class CustomAlphabetTableTest {

  private Mockery context = new JUnit4Mockery();
  private Memory memory;
  private AlphabetTable alphabetTable;
  
  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    alphabetTable = new CustomAlphabetTable(memory, 1000);
  }
  
  @Test
  public void testGetA0Char() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1000); will(returnValue((char) 3)); 
      one (memory).readUnsigned8(1006); will(returnValue((char) 2)); 
    }});
    assertEquals(3, alphabetTable.getA0Char((byte) 6));
    assertEquals(2, alphabetTable.getA0Char((byte) 12));
    assertEquals(' ', alphabetTable.getA0Char((byte) 0));
  }

  @Test
  public void testGetA1Char() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1026); will(returnValue((char) 3)); 
      one (memory).readUnsigned8(1032); will(returnValue((char) 2)); 
    }});
    assertEquals(3, alphabetTable.getA1Char((byte) 6));
    assertEquals(2, alphabetTable.getA1Char((byte) 12));
    assertEquals(' ', alphabetTable.getA1Char((byte) 0));
  }

  @Test
  public void testGetA2Char() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1052); will(returnValue((char) 3)); 
      one (memory).readUnsigned8(1058); will(returnValue((char) 2)); 
    }});
    assertEquals(3, alphabetTable.getA2Char((byte) 6));
    assertEquals(2, alphabetTable.getA2Char((byte) 12));
    assertEquals(' ', alphabetTable.getA2Char((byte) 0));
    assertEquals('\n', alphabetTable.getA2Char((byte) 7));
  }
  
  @Test
  public void testA0IndexOfNotFound() {
    context.checking(new Expectations() {{
      for (int i = 0; i < 26; i++) {
        one (memory).readUnsigned8(1000 + i);
        will(returnValue('a' ));
      }
    }});
    assertEquals(-1, alphabetTable.getA0CharCode('@'));
  }

  @Test
  public void testA1IndexOfNotFound() {
    context.checking(new Expectations() {{
      for (int i = 0; i < 26; i++) {
        one (memory).readUnsigned8(1026 + i);
        will(returnValue('a' ));
      }
    }});
    assertEquals(-1, alphabetTable.getA1CharCode('@'));
  }

  @Test
  public void testA2IndexOfNotFound() {
    context.checking(new Expectations() {{
      // char 7 is directly returned !!
      one (memory).readUnsigned8(1052); will(returnValue('a' ));
      for (int i = 2; i < 26; i++) {
        one (memory).readUnsigned8(1052 + i);
        will(returnValue('a' ));
      }
    }});
    assertEquals(-1, alphabetTable.getA2CharCode('@'));
  }
}
