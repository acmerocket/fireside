/*
 * $Id$
 * 
 * Created on 2005/09/23
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

import junit.framework.TestCase;

import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;

public class ZsciiEncodingTest extends TestCase {

  private ZsciiEncoding zsciiDef;
  
  protected void setUp() throws Exception {
   
    zsciiDef = new ZsciiEncoding(new DefaultAccentTable());
  }

  public void testIsZsciiCharacterAscii() {
    
    assertTrue(zsciiDef.isZsciiCharacter((short) 'A'));
    assertTrue(zsciiDef.isZsciiCharacter((short) 'M'));
    assertTrue(zsciiDef.isZsciiCharacter((short) 'Z'));
    assertTrue(zsciiDef.isZsciiCharacter((short) 'a'));
    assertTrue(zsciiDef.isZsciiCharacter((short) 'm'));
    assertTrue(zsciiDef.isZsciiCharacter((short) 'z'));
  }
  
  public void testIsZsciiCharacterExtra() {
    
    assertEquals(10, (int) '\n');
    assertTrue(zsciiDef.isZsciiCharacter(ZsciiEncoding.NULL));
    assertTrue(zsciiDef.isZsciiCharacter(ZsciiEncoding.NEWLINE));
    assertTrue(zsciiDef.isZsciiCharacter(ZsciiEncoding.ESCAPE));
    assertTrue(zsciiDef.isZsciiCharacter(ZsciiEncoding.DELETE));
  }
  
  public void testIsConvertableToZscii() {
    
    assertTrue(zsciiDef.isConvertableToZscii('A'));
    assertTrue(zsciiDef.isConvertableToZscii('M'));
    assertTrue(zsciiDef.isConvertableToZscii('Z'));
    assertTrue(zsciiDef.isConvertableToZscii('a'));
    assertTrue(zsciiDef.isConvertableToZscii('m'));
    assertTrue(zsciiDef.isConvertableToZscii('z'));
    
    assertTrue(zsciiDef.isConvertableToZscii('\n'));
    assertFalse(zsciiDef.isConvertableToZscii('\07'));
  }
  
  public void testGetUnicode() {
    
    assertEquals('A', zsciiDef.getUnicodeChar((short) 'A'));
    assertEquals('M', zsciiDef.getUnicodeChar((short) 'M'));
    assertEquals('Z', zsciiDef.getUnicodeChar((short) 'Z'));
    assertEquals('a', zsciiDef.getUnicodeChar((short) 'a'));
    assertEquals('m', zsciiDef.getUnicodeChar((short) 'm'));
    assertEquals('z', zsciiDef.getUnicodeChar((short) 'z'));
    assertEquals('\n', zsciiDef.getUnicodeChar(ZsciiEncoding.NEWLINE));
    assertEquals('\0', zsciiDef.getUnicodeChar(ZsciiEncoding.NULL));
    assertEquals('?', zsciiDef.getUnicodeChar(ZsciiEncoding.DELETE));
  }
  
  public void testGetZChar() {
    
    assertEquals('A', zsciiDef.getZsciiChar('A'));
    assertEquals('M', zsciiDef.getZsciiChar('M'));
    assertEquals('Z', zsciiDef.getZsciiChar('Z'));
    
    assertEquals('a', zsciiDef.getZsciiChar('a'));
    assertEquals('m', zsciiDef.getZsciiChar('m'));
    assertEquals('z', zsciiDef.getZsciiChar('z'));    
    assertEquals(0, zsciiDef.getZsciiChar('\07'));    
  }
  
  public void testIsCursorKey() {
    
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_UP));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_DOWN));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_LEFT));
    assertTrue(ZsciiEncoding.isCursorKey(ZsciiEncoding.CURSOR_RIGHT));
    assertFalse(ZsciiEncoding.isCursorKey(ZsciiEncoding.NEWLINE));
  }
  
  public void testStandardTable() {
    
    assertEquals(69, DefaultAccentTable.STANDARD_TRANSLATION_TABLE.length);
  }
}
