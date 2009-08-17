/*
 * Created on 09/24/2005
 * Copyright 2005-2009 by Wei-ju Wu
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

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zmpp.base.Memory;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharEncoder;
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
  private Mockery context = new JUnit4Mockery() {{
    setImposteriser(ClassImposteriser.INSTANCE);
  }};
  private Memory memory;
  private Dictionary dictionary;
  private ZCharDecoder decoder;
  private ZCharEncoder encoder;

  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    decoder = context.mock(ZCharDecoder.class);
    encoder = context.mock(ZCharEncoder.class);

    // num separators
    context.checking(new Expectations() {{
      // num separators
      atLeast(0) .of(memory).readUnsigned8(1000); will(returnValue((char) 3));
      // num entries
      atLeast(0) .of(memory).readUnsigned16(1005); will(returnValue((char) 2));
      // entry size
      atLeast(0) .of (memory).readUnsigned8(1004); will(returnValue((char) 4));
    }});
    dictionary = new DefaultDictionary(memory, 1000, decoder, encoder,
          new DictionarySizesV1ToV3());
  }
  
  @Test
  public void testGetNumSeparators() {
    assertEquals(3, dictionary.getNumberOfSeparators());
  }

  @Test
  public void testGetNumEntries() {
    assertEquals(2, dictionary.getNumberOfEntries());
  }
  
  @Test
  public void testGetEntryLength() {
    assertEquals(4, dictionary.getEntryLength());
  }

  @Test
  public void testGetEntryAddress() {
    assertEquals(1011, dictionary.getEntryAddress(1));
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
    final byte[] get = new byte[] {0x31, 0x59, (byte) 0x94, (byte) 0xa5};
    final byte[] look = new byte[] {
        0x46, (byte) 0x94, (byte) 0xc0, (byte) 0xa5
    };
    final byte[] oops = new byte[] {
        0x52, (byte) 0x95, (byte) 0xe0, (byte) 0xa5
    };
    dictionary = new DefaultDictionary(memory, 1000, decoder, encoder,
        new DictionarySizesV1ToV3()) {
      /** {@inheritDoc} */
      @Override
      protected byte[] truncateTokenToBytes(String token) {
        // just two words are used, 'get' and 'nonsense'
        if ("get".equals(token)) return get;
        return oops;
      }
    };

    context.checking(new Expectations() {{
      for (int i = 0; i < 4; i++) {
        // 'get'
        atLeast (0).of (memory).readUnsigned8(1007 + i);
        will(returnValue((char)  get[i]));
        // 'look'
        atLeast (0).of (memory).readUnsigned8(1011 + i);
        will(returnValue((char) look[i]));
      }
    }});
    assertEquals(1007, dictionary.lookup("get"));
    assertEquals(0, dictionary.lookup("oops"));
  }
}
