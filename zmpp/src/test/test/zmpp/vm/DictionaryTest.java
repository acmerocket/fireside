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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
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
 * @version 1.0
 */
public class DictionaryTest extends MockObjectTestCase {//extends MemoryMapSetup {

  private Mock mockMemory;
  private Memory memory;
  private Dictionary dictionary;
  private Mock mockDecoder;
  private ZCharDecoder decoder;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    
    super.setUp();

    mockMemory = mock(Memory.class);
    memory = (Memory) mockMemory.proxy();
    mockDecoder = mock(ZCharDecoder.class);
    decoder = (ZCharDecoder) mockDecoder.proxy();
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
    ZsciiString get = new ZsciiString(new char[] { 'g', 'e', 't'});
    ZsciiString look = new ZsciiString(new char[] { 'l', 'o', 'o', 'k' });

    // num separators
    mockMemory.expects(exactly(5)).method("readUnsignedByte")
      .with(eq(1000)).will(returnValue((short) 3));
    
    // num entries
    mockMemory.expects(once()).method("readShort")
      .with(eq(1005))
      .will(returnValue((short) 2));
    
    // entry size
    mockMemory.expects(exactly(2)).method("readUnsignedByte")
      .with(eq(1004))
      .will(returnValue((short) 4));
    
    mockDecoder.expects(once()).method("decode2Zscii")
      .with(eq(memory), eq(1007), eq(4))
      .will(returnValue(get));
    mockDecoder.expects(once()).method("decode2Zscii")
      .with(eq(memory), eq(1011), eq(4))
      .will(returnValue(look));
    
    dictionary = new DefaultDictionary(memory, 1000, decoder,
          new DictionarySizesV1ToV3());
  }
  
  public void testGetNumSeparators() {
    
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1000))
      .will(returnValue((short) 3));
    assertEquals(3, dictionary.getNumberOfSeparators());
  }
  
  public void testGetNumEntries() {
    
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1000))
      .will(returnValue((short) 3));
    mockMemory.expects(once()).method("readShort")
      .with(eq(1005))
      .will(returnValue((short) 536));
    assertEquals(536, dictionary.getNumberOfEntries());
  }
  
  public void testGetEntryLength() {
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1000))
      .will(returnValue((short) 3));
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1004))
      .will(returnValue((short) 7));
    assertEquals(7, dictionary.getEntryLength());
  }
  
  public void testGetEntryAddress() {

    mockMemory.expects(exactly(2)).method("readUnsignedByte")
      .with(eq(1000))
      .will(returnValue((short) 3));
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1004))
      .will(returnValue((short) 7));
    
    assertEquals(1014, dictionary.getEntryAddress(1));
  }
  
  public void testGetSeparator() {
    
    mockMemory.expects(once()).method("readUnsignedByte")
      .with(eq(1001))
      .will(returnValue((short) '.'));
    assertEquals('.', dictionary.getSeparator(0));
  }

  public void testLookup() {
    
    char[] get = { 'g', 'e', 't' };
    char[] nonsense = { 'n', 'o', 'n', 's', 'e', 'n', 's', 'e' };
    assertEquals(1007, dictionary.lookup(new ZsciiString(get)));
    assertEquals(0, dictionary.lookup(new ZsciiString(nonsense)));
  }  
}
