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

import org.junit.Test;
import static org.junit.Assert.*;

import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.encoding.ZsciiStringBuilder;

/**
 * Test class for ZsciiStringBuilder.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class ZsciiStringBuilderTest {

  @Test
  public void testEmpty() {
    ZsciiString.initialize(new ZsciiEncoding(new DefaultAccentTable()));
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    assertEquals(0, builder.toZsciiString().length());
    assertEquals(0, builder.toString().length());
  }
  
  @Test
  public void testAppend() {
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    builder.append('a');
    assertEquals(1, builder.toZsciiString().length());
    assertEquals("a", builder.toZsciiString().toString());
    assertEquals("a", builder.toString());
  }

  @Test
  public void testAppendString() { 
    ZsciiStringBuilder builder = new ZsciiStringBuilder();
    char[] zchars = { 'H', 'i' };
    ZsciiString str = new ZsciiString(zchars);
    builder.append('a');
    builder.append(str);
    assertEquals(3, builder.toZsciiString().length());
    assertEquals("aHi", builder.toZsciiString().toString());
    assertEquals("aHi", builder.toString());
  }
}
