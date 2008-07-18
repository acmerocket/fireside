/*
 * $Id$
 * 
 * Created on 2006/02/01
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
import org.zmpp.encoding.ZsciiString;

/**
 * This class tests the ZsciiString class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZsciiStringTest {

  @Before
  public void setUp() throws Exception {
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
  }

  @Test
  public void testCreateString() { 
    char[] chars = { (short) 'H', (short) 'i' };
    char[] charsAccented = { (short) 'H', (short) 155 };
    
    ZsciiString strHi = new ZsciiString(chars);
    ZsciiString strHae = new ZsciiString(charsAccented);
    assertEquals("Hi", strHi.toString());
    assertEquals("H\u00e4", strHae.toString());
  }
  
  @Test
  public void testStringLength() {
    char[] chars = { 'H', 'i' };
    ZsciiString strHi = new ZsciiString(chars);
    assertEquals(2, strHi.length());
  }
  
  @Test
  public void testNotEquals() {
    char[] chars = { 'H', 'i' };
    char[] charsAccented = { 'H', 155 };
    
    ZsciiString strHi = new ZsciiString(chars);
    ZsciiString strHae = new ZsciiString(charsAccented);
    assertFalse(strHi.equals(strHae));
    assertFalse(strHi.equals(null));
    assertFalse(strHi.equals(""));
  }
  
  @Test
  public void testEquals() {
    char[] chars1 = { 'H', 'i' };
    char[] chars2 = { 'H', 'i' };
    ZsciiString strHi1 = new ZsciiString(chars1);
    ZsciiString strHi2 = new ZsciiString(chars1);
    ZsciiString strHi3 = new ZsciiString(chars2);
    assertEquals(strHi1, strHi1);
    assertNotSame(strHi1, strHi2);
    assertEquals(strHi1, strHi2);
    assertEquals(strHi1, strHi3);
  }
  
  @Test
  public void testHashCode() {
    
    char[] chars1 = { 'H', 'i' };
    char[] chars2 = { 'H', 'i' };
    char[] charsAccented = { 'H', 155 };
    ZsciiString strHi1 = new ZsciiString(chars1);
    ZsciiString strHi2 = new ZsciiString(chars1);
    ZsciiString strHi3 = new ZsciiString(chars2);
    ZsciiString strAccented = new ZsciiString(charsAccented);
    
    assertEquals(strHi1.hashCode(), strHi1.hashCode());
    assertEquals(strHi1.hashCode(), strHi2.hashCode());
    assertEquals(strHi1.hashCode(), strHi3.hashCode());
    assertNotSame(strHi1.hashCode(), strAccented.hashCode());
  }
  
  @Test
  public void testCharAt() {
    char[] chars1 = { 'H', 'i' };
    ZsciiString strHi1 = new ZsciiString(chars1);
    assertEquals((short) 'H', strHi1.charAt(0));
    assertEquals((short) 'i', strHi1.charAt(1));
  }
  
  @Test
  public void testIndexOf() {
    char[] chars = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
    char[] sub1 = { 'H' };
    char[] sub2 = { 'l' };
    char[] sub3 = { 'l', 'o', ' ' };
    char[] no = { 'n', 'o' };
    ZsciiString hello = new ZsciiString(chars);
    ZsciiString substr1 = new ZsciiString(sub1);
    ZsciiString substr2 = new ZsciiString(sub2);
    ZsciiString substr3 = new ZsciiString(sub3);
    ZsciiString nonexistent = new ZsciiString(no);
    
    assertEquals(0, hello.indexOf(substr1, 0));
    assertEquals(2, hello.indexOf(substr2, 0));
    assertEquals(3, hello.indexOf(substr2, 3));
    assertEquals(3, hello.indexOf(substr3, 0));
    assertEquals(-1, hello.indexOf(nonexistent, 0));
    assertEquals(-1, hello.indexOf(substr1, 5));
  }
  
  @Test
  public void testSubstring() {
    char[] chars = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
    ZsciiString hello = new ZsciiString(chars);
    ZsciiString sub0 = hello.substring(0, 1);
    ZsciiString sub1 = hello.substring(0, 2);
    ZsciiString sub2 = hello.substring(6, hello.length());
    assertEquals("H", sub0.toString());    
    assertEquals("He", sub1.toString());    
    assertEquals("World", sub2.toString());    
  }
}
