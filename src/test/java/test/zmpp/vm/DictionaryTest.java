/*
 * Created on 09/24/2005
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
