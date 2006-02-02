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
import org.zmpp.encoding.ZsciiStringTokenizer;

public class ZsciiStringTokenizerTest extends TestCase {

  protected void setUp() {
  
    ZsciiString.initialize(new ZsciiEncoding(new DefaultAccentTable()));
  }
  
  public void testTokenizeWithSpace() {
    
    ZsciiString input = new ZsciiString(new short[] { 'H', 'i', ' ', 'y', 'o', 'u' });
    ZsciiString delim = new ZsciiString(new short[] { ' ' });
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  

  public void testTokenizeWithCommaAndSpace() {
    
    ZsciiString input = new ZsciiString(new short[] { 'H', 'i', ',', ' ', 'y', 'o', 'u' });
    ZsciiString delim = new ZsciiString(new short[] { ' ', ','});
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(",", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  
}
