/*
 * $Id$
 * 
 * Created on 2006/02/01
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
import org.zmpp.encoding.ZsciiString;

/**
 * This class tests the ZsciiString class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZsciiStringTest extends TestCase {

  protected void setUp() throws Exception {
    
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
  }

  public void testCreateString() {
    
    short[] chars = { (short) 'H', (short) 'i' };
    short[] charsAccented = { (short) 'H', (short) 155 };
    
    ZsciiString strHi = new ZsciiString(chars);
    ZsciiString strHae = new ZsciiString(charsAccented);
    assertEquals("Hi", strHi.toString());
    assertEquals("H\u00e4", strHae.toString());
  }
  
  public void testStringLength() {
    
    short[] chars = { (short) 'H', (short) 'i' };
    ZsciiString strHi = new ZsciiString(chars);
    assertEquals(2, strHi.length());
  }
  
  public void testNotEquals() {
    
    short[] chars = { (short) 'H', (short) 'i' };
    short[] charsAccented = { (short) 'H', (short) 155 };
    
    ZsciiString strHi = new ZsciiString(chars);
    ZsciiString strHae = new ZsciiString(charsAccented);
    assertFalse(strHi.equals(strHae));
    assertFalse(strHi.equals(null));
    assertFalse(strHi.equals(""));
  }
  
  public void testEquals() {
    
    short[] chars1 = { (short) 'H', (short) 'i' };
    short[] chars2 = { (short) 'H', (short) 'i' };
    ZsciiString strHi1 = new ZsciiString(chars1);
    ZsciiString strHi2 = new ZsciiString(chars1);
    ZsciiString strHi3 = new ZsciiString(chars2);
    assertEquals(strHi1, strHi1);
    assertNotSame(strHi1, strHi2);
    assertEquals(strHi1, strHi2);
    assertEquals(strHi1, strHi3);
  }
  
  public void testHashCode() {
    
    short[] chars1 = { (short) 'H', (short) 'i' };
    short[] chars2 = { (short) 'H', (short) 'i' };
    short[] charsAccented = { (short) 'H', (short) 155 };
    ZsciiString strHi1 = new ZsciiString(chars1);
    ZsciiString strHi2 = new ZsciiString(chars1);
    ZsciiString strHi3 = new ZsciiString(chars2);
    ZsciiString strAccented = new ZsciiString(charsAccented);
    
    assertEquals(strHi1.hashCode(), strHi1.hashCode());
    assertEquals(strHi1.hashCode(), strHi2.hashCode());
    assertEquals(strHi1.hashCode(), strHi3.hashCode());
    assertNotSame(strHi1.hashCode(), strAccented.hashCode());
  }
}
