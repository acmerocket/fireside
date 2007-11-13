/*
 * $Id$
 * 
 * Created on 2006/02/01
 * Copyright 2005-2007 by Wei-ju Wu
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

import junit.framework.TestCase;

import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.encoding.ZsciiStringBuilder;

public class ZsciiStringBuilderTest extends TestCase {

  public void testEmpty() {

    ZsciiString.initialize(new ZsciiEncoding(new DefaultAccentTable()));
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    assertEquals(0, builder.toZsciiString().length());
    assertEquals(0, builder.toString().length());
  }
  
  public void testAppend() {
    
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    builder.append((short) 'a');
    assertEquals(1, builder.toZsciiString().length());
    assertEquals("a", builder.toZsciiString().toString());
    assertEquals("a", builder.toString());
  }

  public void testAppendString() {
    
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    short[] zchars = { 'H', 'i' };
    ZsciiString str = new ZsciiString(zchars);
    builder.append((short) 'a');
    builder.append(str);
    assertEquals(3, builder.toZsciiString().length());
    assertEquals("aHi", builder.toZsciiString().toString());
    assertEquals("aHi", builder.toString());
  }
}
