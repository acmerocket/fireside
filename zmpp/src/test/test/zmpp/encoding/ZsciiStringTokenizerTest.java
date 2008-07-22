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

import org.zmpp.encoding.ZsciiStringTokenizer;

/**
 * Test clas for ZsciiStringTokenizer.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZsciiStringTokenizerTest {

  @Test
  public void testTokenizeWithSpace() {
    String input = new String(new char[] { 'H', 'i', ' ', 'y', 'o', 'u' });
    String delim = new String(new char[] { ' ' });
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  

  @Test
  public void testTokenizeWithCommaAndSpace() {
    String input = new String(new char[] { 'H', 'i', ',', ' ', 'y', 'o', 'u' });
    String delim = new String(new char[] { ' ', ','});
      
    ZsciiStringTokenizer tok = new ZsciiStringTokenizer(input, delim);
    assertEquals("Hi", tok.nextToken().toString());
    assertEquals(",", tok.nextToken().toString());
    assertEquals(" ", tok.nextToken().toString());
    assertEquals("you", tok.nextToken().toString());
  }  
}
