/*
 * $Id$
 * 
 * Created on 24.09.2005
 * Copyright 2005 by Wei-ju Wu
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
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryReadAccess;
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

  private Mock mockMemAccess;
  private MemoryReadAccess memaccess;
  private Dictionary dictionary;
  private Mock mockDecoder;
  private ZCharDecoder decoder;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    
    super.setUp();

    mockMemAccess = mock(MemoryReadAccess.class);
    memaccess = (MemoryReadAccess) mockMemAccess.proxy();
    mockDecoder = mock(ZCharDecoder.class);
    decoder = (ZCharDecoder) mockDecoder.proxy();
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
    ZsciiString get = new ZsciiString(new short[] { 'g', 'e', 't'});
    ZsciiString look = new ZsciiString(new short[] { 'l', 'o', 'o', 'k' });

    // num separators
    mockMemAccess.expects(exactly(5)).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    
    // num entries
    mockMemAccess.expects(once()).method("readShort").with(eq(1005)).will(returnValue((short) 2));
    
    // entry size
    mockMemAccess.expects(exactly(2)).method("readUnsignedByte").with(eq(1004)).will(returnValue((short) 4));
    
    mockDecoder.expects(once()).method("decode2Zscii").with(eq(memaccess), eq(1007), eq(4)).will(returnValue(get));
    mockDecoder.expects(once()).method("decode2Zscii").with(eq(memaccess), eq(1011), eq(4)).will(returnValue(look));
    
    dictionary = new DefaultDictionary(memaccess, 1000, decoder, new DictionarySizesV1ToV3());
  }
  
  public void testGetNumSeparators() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    assertEquals(3, dictionary.getNumberOfSeparators());
  }
  
  public void testGetNumEntries() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    mockMemAccess.expects(once()).method("readShort").with(eq(1005)).will(returnValue((short) 536));
    assertEquals(536, dictionary.getNumberOfEntries());
  }
  
  public void testGetEntryLength() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1004)).will(returnValue((short) 7));
    assertEquals(7, dictionary.getEntryLength());
  }
  
  public void testGetEntryAddress() {

    mockMemAccess.expects(exactly(2)).method("readUnsignedByte").with(eq(1000)).will(returnValue((short) 3));
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1004)).will(returnValue((short) 7));
    
    assertEquals(1014, dictionary.getEntryAddress(1));
  }
  
  public void testGetSeparator() {
    
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(1001)).will(returnValue((short) '.'));
    assertEquals('.', dictionary.getSeparator(0));
  }

  public void testLookup() {
    
    short[] get = { 'g', 'e', 't' };
    short[] nonsense = { 'n', 'o', 'n', 's', 'e', 'n', 's', 'e' };
    assertEquals(1007, dictionary.lookup(new ZsciiString(get)));
    assertEquals(0, dictionary.lookup(new ZsciiString(nonsense)));
  }  
}
