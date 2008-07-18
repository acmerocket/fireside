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

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.encoding.ZsciiStringTokenizer;

/**
 * Test clas for ZsciiStringTokenizer.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZsciiStringTokenizerTest {

  @Before
  public void setUp() {
    ZsciiString.initialize(new ZsciiEncoding(new DefaultAccentTable()));
  }
  
  @Test
  public void testTokenizeWithSpace() {
    ZsciiString input = new ZsciiString(new char[] { 'H', 'i', ' ', 'y', 'o', 'u' });
    ZsciiString delim = new ZsciiString(new char[] { ' ' });
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  

  @Test
  public void testTokenizeWithCommaAndSpace() {
    ZsciiString input = new ZsciiString(new char[] { 'H', 'i', ',', ' ', 'y', 'o', 'u' });
    ZsciiString delim = new ZsciiString(new char[] { ' ', ','});
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(",", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  
}
