/*
 * Created on 2005/09/23
 * Copyright (c) 2005-2009, Wei-ju Wu.
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
    assertEquals(69, new DefaultAccentTable().getLength());
  }
  
  @Test
  public void testToLowerCase() { 
    assertEquals('a', encoding.toLower('A'));
    assertEquals(155, encoding.toLower((char) 158));
  }
}
