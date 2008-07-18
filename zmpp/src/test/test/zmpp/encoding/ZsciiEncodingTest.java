/*
 * $Id$
 * 
 * Created on 2005/09/23
 * Copyright 2005-2008 by Wei-ju Wu
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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;

/**
 * Test class for ZsciiEncoding.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZsciiEncodingTest {

  private ZsciiEncoding encoding;
  
  @Before
  public void setUp() throws Exception {
    encoding = new ZsciiEncoding(new DefaultAccentTable());
  }

  @Test
  public void testIsZsciiCharacterAscii() {
    assertTrue(encoding.isZsciiCharacter('A'));
    assertTrue(encoding.isZsciiCharacter('M'));
    assertTrue(encoding.isZsciiCharacter('Z'));
    assertTrue(encoding.isZsciiCharacter('a'));
    assertTrue(encoding.isZsciiCharacter('m'));
    assertTrue(encoding.isZsciiCharacter('z'));
  }
  
  @Test
  public void testIsZsciiCharacterExtra() {
    assertEquals(10, (int) '\n');
    assertTrue(encoding.isZsciiCharacter(ZsciiEncoding.NULL));
    assertTrue(encoding.isZsciiCharacter(ZsciiEncoding.NEWLINE));
    assertTrue(encoding.isZsciiCharacter(ZsciiEncoding.ESCAPE));
    assertTrue(encoding.isZsciiCharacter(ZsciiEncoding.DELETE));
  }
  
  @Test
  public void testIsConvertableToZscii() {
    assertTrue(encoding.isConvertableToZscii('A'));
    assertTrue(encoding.isConvertableToZscii('M'));
    assertTrue(encoding.isConvertableToZscii('Z'));
    assertTrue(encoding.isConvertableToZscii('a'));
    assertTrue(encoding.isConvertableToZscii('m'));
    assertTrue(encoding.isConvertableToZscii('z'));
    
    assertTrue(encoding.isConvertableToZscii('\n'));
    assertFalse(encoding.isConvertableToZscii('\07'));
  }
  
  @Test
  public void testGetUnicode() {
    assertEquals('A', encoding.getUnicodeChar('A'));
    assertEquals('M', encoding.getUnicodeChar('M'));
    assertEquals('Z', encoding.getUnicodeChar('Z'));
    assertEquals('a', encoding.getUnicodeChar('a'));
    assertEquals('m', encoding.getUnicodeChar('m'));
    assertEquals('z', encoding.getUnicodeChar('z'));
    assertEquals('\n', encoding.getUnicodeChar(ZsciiEncoding.NEWLINE));
    assertEquals('\0', encoding.getUnicodeChar(ZsciiEncoding.NULL));
    assertEquals('?', encoding.getUnicodeChar(ZsciiEncoding.DELETE));
  }
  
  @Test
  public void testGetZChar() {
    assertEquals('A', encoding.getZsciiChar('A'));
    assertEquals('M', encoding.getZsciiChar('M'));
    assertEquals('Z', encoding.getZsciiChar('Z'));
    
    assertEquals('a', encoding.getZsciiChar('a'));
    assertEquals('m', encoding.getZsciiChar('m'));
    assertEquals('z', encoding.getZsciiChar('z'));    
    assertEquals(0, encoding.getZsciiChar('\07'));    
  }
  
  @Test
  public void testIsCursorKey() {
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_UP));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_DOWN));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_LEFT));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_RIGHT));
    assertFalse(ZsciiEncoding.isCursorKey(ZsciiEncoding.NEWLINE));
  }
  
  @Test
  public void testStandardTable() {
    assertEquals(69, DefaultAccentTable.STANDARD_TRANSLATION_TABLE.length);
  }
  
  @Test
  public void testToLowerCase() { 
    assertEquals('a', encoding.toLower('A'));
    assertEquals(155, encoding.toLower((char) 158));
  }
}
