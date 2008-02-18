/*
 * $Id$
 * 
 * Created on 2005/09/23
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

import java.io.File;
import java.io.RandomAccessFile;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.encoding.AlphabetTable;
import org.zmpp.encoding.AlphabetTableV1;
import org.zmpp.encoding.DefaultAccentTable;
import org.zmpp.encoding.DefaultAlphabetTable;
import org.zmpp.encoding.DefaultZCharDecoder;
import org.zmpp.encoding.DefaultZCharTranslator;
import org.zmpp.encoding.ZCharDecoder;
import org.zmpp.encoding.ZCharTranslator;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.encoding.ZsciiString;
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

  private Mock mockMemory;
  private Memory memory;
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
    mockMemory = mock(Memory.class);
    memory = (Memory) mockMemory.proxy();
    
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    ZsciiString.initialize(encoding);
    AlphabetTable alphabetTable = new DefaultAlphabetTable();
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    decoder = new DefaultZCharDecoder(encoding, translator, abbrev);
  }
  
  public void testDecodeByte() {
  
    assertEquals('a', decoder.decodeZChar((char) 6));
  }
  
  public void testDecode2Unicode2Params() {
  
    byte[] hello = { 0x35, 0x51, (byte) 0xc6, (byte) 0x85 };
    byte[] Hello = { 0x11, (byte) 0xaa, (byte) 0xc6, (byte) 0x34 };
    Memory memory1 = new DefaultMemory(hello);     
    Memory memory2 = new DefaultMemory(Hello);     
    assertEquals("hello", decoder.decode2Zscii(memory1, 0, 0).toString());    
    assertEquals("Hello", decoder.decode2Zscii(memory2, 0, 0).toString());    
  }
  
  // *********************************************************************
  // **** Real-world tests
  // ****************************************
  
  public void testMinizork() throws Exception {
    
    File zork1 = new File("testfiles/minizork.z3");
    RandomAccessFile file = new RandomAccessFile(zork1, "r");
    int fileSize = (int) file.length();
    byte[] zork1data = new byte[fileSize];    
    file.read(zork1data);
    file.close();
    
    Memory memory = new DefaultMemory(zork1data);
    StoryFileHeader fileheader = new DefaultStoryFileHeader(memory);
    AbbreviationsTable abbr = new Abbreviations(memory, fileheader.getAbbreviationsAddress());

    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    AlphabetTable alphabetTable = new DefaultAlphabetTable(); 
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    
    ZCharDecoder decoder = new DefaultZCharDecoder(encoding, translator, abbr);
    assertEquals("The Great Underground Empire", decoder.decode2Zscii(memory, 0xc120, 0).toString());
    assertEquals("[I don't understand that sentence.]", decoder.decode2Zscii(memory, 0x3e6d, 0).toString());
  }

  /**
   * A pretty complex example: the Zork I introduction message. This one
   * clarified that the current shift lock alphabet needs to be restored
   * after a regular shift occured.
   */
  public void testZork1V1() {
    
    String originalString = "ZORK: The Great Underground Empire - Part I\n"
      + "Copyright (c) 1980 by Infocom, Inc. All rights reserved.\n"
      + "ZORK is a trademark of Infocom, Inc.\n"
      + "Release ";
    
    // This String was extracted from release 5 of Zork I and contains
    // the same message as in originalString.
    byte[] data = {
        
        (byte) 0x13, (byte) 0xf4, (byte) 0x5e, (byte) 0x02, 
        (byte) 0x74, (byte) 0x19, (byte) 0x15, (byte) 0xaa, 
        (byte) 0x00, (byte) 0x4c, (byte) 0x5d, (byte) 0x46, 
        (byte) 0x64, (byte) 0x02, (byte) 0x6a, (byte) 0x69, 
        (byte) 0x2a, (byte) 0xec, (byte) 0x5e, (byte) 0x9a, 
        (byte) 0x4d, (byte) 0x20, (byte) 0x09, (byte) 0x52, 
        (byte) 0x55, (byte) 0xd7, (byte) 0x28, (byte) 0x03, 
        (byte) 0x70, (byte) 0x02, (byte) 0x54, (byte) 0xd7, 
        (byte) 0x64, (byte) 0x02, (byte) 0x38, (byte) 0x22, 
        (byte) 0x22, (byte) 0x95, (byte) 0x7a, (byte) 0xee, 
        (byte) 0x31, (byte) 0xb9, (byte) 0x00, (byte) 0x7e, 
        (byte) 0x20, (byte) 0x7f, (byte) 0x00, (byte) 0xa8, 
        (byte) 0x41, (byte) 0xe7, (byte) 0x00, (byte) 0x87, 
        (byte) 0x78, (byte) 0x02, (byte) 0x3a, (byte) 0x6b, 
        (byte) 0x51, (byte) 0x14, (byte) 0x48, (byte) 0x72, 
        (byte) 0x00, (byte) 0x4e, (byte) 0x4d, (byte) 0x03, 
        (byte) 0x44, (byte) 0x02, (byte) 0x1a, (byte) 0x31, 
        (byte) 0x02, (byte) 0xee, (byte) 0x31, (byte) 0xb9, 
        (byte) 0x60, (byte) 0x17, (byte) 0x2b, (byte) 0x0a, 
        (byte) 0x5f, (byte) 0x6a, (byte) 0x24, (byte) 0x71, 
        (byte) 0x04, (byte) 0x9f, (byte) 0x52, (byte) 0xf0, 
        (byte) 0x00, (byte) 0xae, (byte) 0x60, (byte) 0x06, 
        (byte) 0x03, (byte) 0x37, (byte) 0x19, (byte) 0x2a, 
        (byte) 0x48, (byte) 0xd7, (byte) 0x40, (byte) 0x14, 
        (byte) 0x2c, (byte) 0x02, (byte) 0x3a, (byte) 0x6b, 
        (byte) 0x51, (byte) 0x14, (byte) 0x48, (byte) 0x72, 
        (byte) 0x00, (byte) 0x4e, (byte) 0x4d, (byte) 0x03, 
        (byte) 0x44, (byte) 0x22, (byte) 0x5d, (byte) 0x51, 
        (byte) 0x28, (byte) 0xd8, (byte) 0xa8, (byte) 0x05, 
    };
    
    Memory memory = new DefaultMemory(data);
        
    ZsciiEncoding encoding = new ZsciiEncoding(new DefaultAccentTable());
    AlphabetTable alphabetTable = new AlphabetTableV1(); 
    ZCharTranslator translator = new DefaultZCharTranslator(alphabetTable);
    
    ZCharDecoder decoder = new DefaultZCharDecoder(encoding, translator, null);
    String decoded = decoder.decode2Zscii(memory, 0, 0).toString();
    assertEquals(originalString, decoded);
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
    Memory memory = new DefaultMemory(helloAbbrev);
    assertEquals("helloHello", decoder.decode2Zscii(memory, 0, 0).toString());    
  }
  
  
  public void testEndCharacter() {
    
    short notEndWord = 0x7123;
    assertFalse(DefaultZCharDecoder.isEndWord(notEndWord));
    short endWord = (short) 0x8123;
    assertTrue(DefaultZCharDecoder.isEndWord(endWord));
  }

  public void testExtractZBytesOneWordOnly() {
    
    mockMemory.expects(once()).method("readShort").will(returnValue((short) 0x9865));
    char[] data = DefaultZCharDecoder.extractZbytes(memory, 0, 0);
    assertEquals(3, data.length);
    assertEquals(6, data[0]);
    assertEquals(3, data[1]);
    assertEquals(5, data[2]);
  }

  public void testExtractZBytesThreeWords() {
    
    mockMemory.expects(atLeastOnce()).method("readShort").will(
        onConsecutiveCalls(returnValue((short) 0x5432),
                           returnValue((short) 0x1234),
                           returnValue((short) 0x9865)));
    char[] data = DefaultZCharDecoder.extractZbytes(memory, 0, 0);
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
    Memory memory = new DefaultMemory(data);
    int length = 4;
    
    // With length = 0
    assertEquals("helloalo", decoder.decode2Zscii(memory, 0, 0).toString());
    
    // With length = 4
    assertEquals("helloa", decoder.decode2Zscii(memory, 0, length).toString());    
  }

  public void testTruncateShiftAtEnd() {
    
    byte[] data = { (byte) 0x34, (byte) 0x8a, (byte) 0x45, (byte) 0xc4 };
    Memory memory = new DefaultMemory(data);
    int length = 4;
    
    assertEquals("hEli", decoder.decode2Zscii(memory, 0, length).toString());    
  }
  
  /**
   * Escape A6 starts at position 0 of the last word.
   */
  public void testTruncateEscapeA2AtEndStartsAtWord2_0() {
    
    byte[] data = { (byte) 0x34, (byte) 0xd1, (byte) 0x14, (byte) 0xc1,
                    (byte) 0x80, (byte) 0xa5 };
    Memory memory = new DefaultMemory(data);
    int length = 4;
    
    assertEquals("hal", decoder.decode2Zscii(memory, 0, length).toString());    
  }

  /**
   * Escape A6 starts at position 1 of the last word.
   */
  public void testTruncateEscapeA2AtEndStartsAtWord2_1() {
    
    byte[] data = { (byte) 0x34, (byte) 0xd1, (byte) 0x44, (byte) 0xa6,
                    (byte) 0x84, (byte) 0x05 };
    Memory memory = new DefaultMemory(data);
    int length = 4;
    
    assertEquals("hall", decoder.decode2Zscii(memory, 0, length).toString());    
  }
}
