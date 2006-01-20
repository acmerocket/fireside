/*
 * $Id$
 * 
 * Created on 2005/09/23
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

import java.io.File;
import java.io.RandomAccessFile;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.base.MemoryAccess;
import org.zmpp.base.MemoryReadAccess;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZCharDecoder.AbbreviationsTable;
import org.zmpp.vm.Abbreviations;
import org.zmpp.vm.DefaultStoryFileHeader;
import org.zmpp.vm.StoryFileHeader;

/**
 * This class tests the DefaultZCharDecoder class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ZCharDecoderTest extends MockObjectTestCase {

  private Mock mockMemAccess;
  private MemoryAccess memaccess;
  private Mock mockAbbrev;
  private AbbreviationsTable abbrev;
  private ZCharDecoder decoder;
  byte shift2 = 2;
  byte shift3 = 3;
  byte shift4 = 4;
  byte shift5 = 5;

  public void setUp() {
   
    mockAbbrev = mock(AbbreviationsTable.class);
    abbrev = (AbbreviationsTable) mockAbbrev.proxy();
    mockMemAccess = mock(MemoryAccess.class);
    memaccess = (MemoryAccess) mockMemAccess.proxy();
    
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    AlphabetTable alphabetTable = new DefaultAlphabetTable();
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    decoder = new DefaultZCharDecoder(encoding, translator, abbrev);
  }
  
  public void testDecodeByte() {
  
    assertEquals('a', decoder.decodeZChar((byte) 6));
  }
  
  public void testDecode2Unicode2Params() {
  
    byte[] hello = { 0x35, 0x51, (byte) 0xc6, (byte) 0x85 };
    byte[] Hello = { 0x11, (byte) 0xaa, (byte) 0xc6, (byte) 0x34 };
    MemoryReadAccess memaccess1 = new DefaultMemoryAccess(hello);     
    MemoryReadAccess memaccess2 = new DefaultMemoryAccess(Hello);     
    assertEquals("hello", decoder.decode2Unicode(memaccess1, 0));    
    assertEquals("Hello", decoder.decode2Unicode(memaccess2, 0));    
  }
  
  // *********************************************************************
  // **** Real-world test
  // ****************************************
  
  public void testMinizork() throws Exception {
    
    File zork1 = new File("testfiles/minizork.z3");
    RandomAccessFile file = new RandomAccessFile(zork1, "r");
    int fileSize = (int) file.length();
    byte[] zork1data = new byte[fileSize];    
    file.read(zork1data);
    file.close();
    
    MemoryAccess memaccess = new DefaultMemoryAccess(zork1data);
    StoryFileHeader fileheader = new DefaultStoryFileHeader(memaccess);
    AbbreviationsTable abbr = new Abbreviations(memaccess, fileheader.getAbbreviationsAddress());

    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    AlphabetTable alphabetTable = new DefaultAlphabetTable(); 
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    
    ZCharDecoder converter = new DefaultZCharDecoder(encoding, translator, abbr);
    assertEquals("The Great Underground Empire", converter.decode2Unicode(memaccess, 0xc120));
    assertEquals("[I don't understand that sentence.]", converter.decode2Unicode(memaccess, 0x3e6d));
  }

  // *********************************************************************
  // **** Tests based on mock objects
  // ****************************************
  
  public void testConvertWithAbbreviation() {

    byte[] helloAbbrev = {
        0x35, 0x51, (byte) 0x46, (byte) 0x81, (byte) 0x88, (byte) 0xa5, // hello{abbrev_2}
        0x35, 0x51, (byte) 0xc6, (byte) 0x85, // hello
        0x11, (byte) 0xaa, (byte) 0xc6, (byte) 0x34 // Hello
    };
    mockAbbrev.expects(once()).method("getWordAddress").with(eq(2)).will(returnValue(10));
    MemoryReadAccess memaccess = new DefaultMemoryAccess(helloAbbrev);
    assertEquals("helloHello", decoder.decode2Unicode(memaccess, 0));    
  }
  
  
  public void testEndCharacter() {
    
    short notEndWord = 0x7123;
    assertFalse(DefaultZCharDecoder.isEndWord(notEndWord));
    short endWord = (short) 0x8123;
    assertTrue(DefaultZCharDecoder.isEndWord(endWord));
  }

  public void testExtractZBytesOneWordOnly() {
    
    mockMemAccess.expects(once()).method("readShort").will(returnValue((short) 0x9865));
    short[] data = DefaultZCharDecoder.extractZbytes(memaccess, 0, 0);
    assertEquals(3, data.length);
    assertEquals(6, data[0]);
    assertEquals(3, data[1]);
    assertEquals(5, data[2]);
  }

  public void testExtractZBytesThreeWords() {
    
    mockMemAccess.expects(atLeastOnce()).method("readShort").will(
        onConsecutiveCalls(returnValue((short) 0x5432),
                           returnValue((short) 0x1234),
                           returnValue((short) 0x9865)));
    short[] data = DefaultZCharDecoder.extractZbytes(memaccess, 0, 0);
    assertEquals(9, data.length);
  }  

  // *********************************************************************
  // **** Tests for string truncation
  // **** We test the truncation algorithm for V3 length only which is
  // **** 4 bytes, 6 characters. In fact, this should be general enough
  // **** so we do not need to test 6 bytes, 9 characters as in >= V4
  // **** files. Since this method is only used within dictionaries, we
  // **** do not need to test abbreviations
  // ****************************************
  
  public void testTruncateAllSmall() {
    
    byte[] data = { (byte) 0x35, (byte) 0x51, (byte) 0x46, (byte) 0x86,
                    (byte) 0xc6, (byte) 0x85 };
    MemoryReadAccess memaccess = new DefaultMemoryAccess(data);
    int length = 4;
    
    // With length = 0
    assertEquals("helloalo", decoder.decode2Unicode(memaccess, 0, 0));
    
    // With length = 4
    assertEquals("helloa", decoder.decode2Unicode(memaccess, 0, length));    
  }

  public void testTruncateShiftAtEnd() {
    
    byte[] data = { (byte) 0x34, (byte) 0x8a, (byte) 0x45, (byte) 0xc4 };
    MemoryReadAccess memaccess = new DefaultMemoryAccess(data);
    int length = 4;
    
    decoder.decode2Unicode(memaccess, 0, length);
    assertEquals("hEli", decoder.decode2Unicode(memaccess, 0, length));    
  }
  
  /**
   * Escape A6 starts at position 0 of the last word.
   */
  public void testTruncateEscapeA2AtEndStartsAtWord2_0() {
    
    byte[] data = { (byte) 0x34, (byte) 0xd1, (byte) 0x14, (byte) 0xc1,
                    (byte) 0x80, (byte) 0xa5 };
    MemoryReadAccess memaccess = new DefaultMemoryAccess(data);
    int length = 4;
    
    decoder.decode2Unicode(memaccess, 0, length);
    assertEquals("hal", decoder.decode2Unicode(memaccess, 0, length));    
  }

  /**
   * Escape A6 starts at position 1 of the last word.
   */
  public void testTruncateEscapeA2AtEndStartsAtWord2_1() {
    
    byte[] data = { (byte) 0x34, (byte) 0xd1, (byte) 0x44, (byte) 0xa6,
                    (byte) 0x84, (byte) 0x05 };
    MemoryReadAccess memaccess = new DefaultMemoryAccess(data);
    int length = 4;
    
    decoder.decode2Unicode(memaccess, 0, length);
    assertEquals("hall", decoder.decode2Unicode(memaccess, 0, length));    
  }
}
