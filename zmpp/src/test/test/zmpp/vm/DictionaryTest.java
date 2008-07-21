/*
 * $Id$
 * 
 * Created on 09/24/2005
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
package test.zmpp.vm;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.zmpp.base.Memory;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
import org.zmpp.vm.DefaultDictionary;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.DictionarySizesV1ToV3;

/**
 * This class tests the dictionary view.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class DictionaryTest {
  Mockery context = new JUnit4Mockery();
  private Memory memory;
  private Dictionary dictionary;
  private ZCharDecoder decoder;

  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    decoder = context.mock(ZCharDecoder.class);
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
    final ZsciiString get = new ZsciiString(new char[] { 'g', 'e', 't'});
    final ZsciiString look = new ZsciiString(new char[] { 'l', 'o', 'o', 'k' });

    // num separators
    context.checking(new Expectations() {{
      // num separators
      exactly(5).of (memory).readUnsigned8(1000); will(returnValue((char) 3));
      // num entries
      one (memory).readUnsigned16(1005); will(returnValue((char) 2));
      // entry size
      exactly(2).of (memory).readUnsigned8(1004); will(returnValue((char) 4));

      // "get"
      one (decoder).decode2Zscii(memory, 1007, 4); will(returnValue(get));
      // "look"
      one (decoder).decode2Zscii(memory, 1011, 4); will(returnValue(look));
    }});    
    dictionary = new DefaultDictionary(memory, 1000, decoder,
          new DictionarySizesV1ToV3());
  }
  
  @Test
  public void testGetNumSeparators() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1000); will(returnValue((char) 3));
    }});
    assertEquals(3, dictionary.getNumberOfSeparators());
  }

  @Test
  public void testGetNumEntries() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1000); will(returnValue((char) 3));
      one (memory).readUnsigned16(1005); will(returnValue((char) 536));
    }});
    assertEquals(536, dictionary.getNumberOfEntries());
  }
  
  @Test
  public void testGetEntryLength() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1000); will(returnValue((char) 3));
      one (memory).readUnsigned8(1004); will(returnValue((char) 7));
    }});
    assertEquals(7, dictionary.getEntryLength());
  }

  @Test
  public void testGetEntryAddress() {
    context.checking(new Expectations() {{
      exactly(2).of (memory).readUnsigned8(1000); will(returnValue((char) 3));
      one (memory).readUnsigned8(1004); will(returnValue((char) 7));
    }});
    assertEquals(1014, dictionary.getEntryAddress(1));
  }
  
  @Test
  public void testGetSeparator() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(1001); will(returnValue('.'));
    }});
    assertEquals('.', dictionary.getSeparator(0));
  }

  @Test
  public void testLookup() {
    char[] get = { 'g', 'e', 't' };
    char[] nonsense = { 'n', 'o', 'n', 's', 'e', 'n', 's', 'e' };
    assertEquals(1007, dictionary.lookup(new ZsciiString(get)));
    assertEquals(0, dictionary.lookup(new ZsciiString(nonsense)));
  }
}
