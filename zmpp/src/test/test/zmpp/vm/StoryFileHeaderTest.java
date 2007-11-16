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
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.vm.DefaultStoryFileHeader;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.StoryFileHeader.Attribute;


/**
 * This class is a test for the StoryFileHeader class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StoryFileHeaderTest extends MockObjectTestCase {

  private Mock mockMemory;
  private Memory memory;
  private StoryFileHeader fileHeader;
  
  protected void setUp() throws Exception {
    
    mockMemory = mock(Memory.class);
    memory = (Memory) mockMemory.proxy();
    fileHeader = new DefaultStoryFileHeader(memory); 
  }
  
  public void testGetVersion() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 3));
    assertEquals(3, fileHeader.getVersion());
  }
  
  public void testGetRelease() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x02)).will(returnValue(35));    
    assertEquals(35, fileHeader.getRelease());
  }
  
  public void testGetHighMemAddress() {
  
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x04)).will(returnValue(4711));    
    assertEquals(4711, fileHeader.getHighMemAddress());
  }
  
  public void testGetInitialPC() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x06)).will(returnValue(4712));    
    assertEquals(4712, fileHeader.getProgramStart());
  }

  public void testGetDictionaryAddress() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x08)).will(returnValue(4713));    
    assertEquals(4713, fileHeader.getDictionaryAddress());
  }
  
  public void testGetObjectTableAddress() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x0a)).will(returnValue(4714));    
    assertEquals(4714, fileHeader.getObjectTableAddress());
  }

  public void testGetGlobalsAddress() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x0c)).will(returnValue(4715));    
    assertEquals(4715, fileHeader.getGlobalsAddress());
  }

  public void testGetStaticMemAddress() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x0e)).will(returnValue(4716));    
    assertEquals(4716, fileHeader.getStaticsAddress());
  }
  
  public void testGetSerialNumber() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x12)).will(returnValue((short) '0'));    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x13)).will(returnValue((short) '5'));    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x14)).will(returnValue((short) '1'));    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x15)).will(returnValue((short) '2'));    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x16)).will(returnValue((short) '0'));    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x17)).will(returnValue((short) '9'));    
    assertEquals("051209", fileHeader.getSerialNumber());
  }

  public void testGetAbbreviationsAddress() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x18)).will(returnValue(4717));    
    assertEquals(4717, fileHeader.getAbbreviationsAddress());
  }
  
  public void testGetFileLengthV3() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 3));
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x1a)).will(returnValue(4718));
    
    assertEquals(4718 * 2, fileHeader.getFileLength());
  }

  public void testGetFileLengthV4() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 4));
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x1a)).will(returnValue(4718));
    
    assertEquals(4718 * 4, fileHeader.getFileLength());
  }

  public void testGetFileLengthV8() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 8));
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x1a)).will(returnValue(4718));
    
    assertEquals(4718 * 8, fileHeader.getFileLength());
  }
  
  public void testGetChecksum() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedShort").with(eq(0x1c)).will(returnValue(4719));    
    assertEquals(4719, fileHeader.getChecksum());
  }
  
  public void testSetScreenHeight() {
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x20), eq((short) 255));
    fileHeader.setScreenHeight(255);
  }
  
  public void testSetScreenHeightUnits() {
    
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(0x24), eq(40));
    fileHeader.setScreenHeightUnits(40);
  }

  public void testGetScreenWidth() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x21)).will(returnValue((short) 82));
    assertEquals(82, fileHeader.getScreenWidth());
  }
  
  public void testSetScreenWidth() {
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x21), eq((short) 82));
    fileHeader.setScreenWidth(82);
  }  

  public void testSetScreenWidthUnits() {
    
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(0x22), eq(82));    
    fileHeader.setScreenWidthUnits(82);
  }  
  
  public void testSetInterpreterVersionV5() {
    
    // Story file version 4 or 5: version number as string
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 5));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x1f), eq((short) '4'));
    fileHeader.setInterpreterVersion(4);
  }
  
  public void testSetInterpreterVersionV8() {
    
    // Story file version > 5: version number as value
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 8));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x1f), eq((short) 4));
    fileHeader.setInterpreterVersion(4);
  }
  
  public void testSetInterpreterNumber() {
 
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x1e), eq((short) 3));
    fileHeader.setInterpreterNumber(3);
  }
  
  // *************************************************************************
  // ****** ATTRIBUTES
  // **************************
  
  public void testIsEnabledNull() {
  
    // This is not matched in the code
    assertFalse(fileHeader.isEnabled(Attribute.SUPPORTS_STATUSLINE));    
  }
  
  public void testSetTranscripting() {
   
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x10)).will(returnValue((short) 0));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x10), eq((short) 1));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x10), eq((short) 0));
    
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, true);
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, false);
  }
  
  public void testIsTranscriptingEnabled() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x10)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 0) ));
    
    assertTrue(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
    assertFalse(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
  }
  
  public void testSetForceFixedFont() {
   
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x10)).will(returnValue((short) 1));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x10), eq((short) 3));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x10), eq((short) 1));

    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, false);
  }
  
  public void testIsForceFixedFont() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x10)).will(
        onConsecutiveCalls( returnValue((short) 6), returnValue((short) 5) ));
    
    assertTrue(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
    assertFalse(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
  }

  public void testSetSupportsTimedInput() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 3), returnValue((short) 131) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 131));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 3));
    
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, false);
  }

  public void testIsScoreGame() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 5), returnValue((short) 7) ));    
    assertTrue(fileHeader.isEnabled(Attribute.SCORE_GAME));
    assertFalse(fileHeader.isEnabled(Attribute.SCORE_GAME));
  }

  public void testSetSupportsFixed() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 17) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 17));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));

    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, false);
  }

  public void testSetSupportsBold() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 5) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 5));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));

    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, false);
  }
  
  public void testSetSupportsItalic() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 9) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 9));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));

    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, false);
  }

  public void testSetSupportsScreenSplitting() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 33) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 33));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));

    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, false);
  }

  public void testSetSupportsStatusLine() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 17), returnValue((short) 1) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 17));

    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, false);
  }

  public void testSetDefaultFontIsVariable() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 1), returnValue((short) 65) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 65));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 1));

    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, true);
    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, false);
  }

  public void testIsDefaultFontVariable() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 69), returnValue((short) 7) ));    
    assertTrue(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
    assertFalse(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
  }
  
  public void testSetSupportsColors() {
    
    mockMemory.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0x01)).will(
        onConsecutiveCalls( returnValue((short) 4), returnValue((short) 5) ));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 5));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x01), eq((short) 4));

    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, false);
  }
  
  public void testGetTerminatorsAddress() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x2e)).will(returnValue(1234));
    assertEquals(1234, fileHeader.getTerminatorsAddress());
  }
  
  public void testSetStandardRevision() {
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x32), eq((short) 1));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x33), eq((short) 2));
    fileHeader.setStandardRevision(1, 2);
  }
  
  public void testSetFontWidthV5() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 5));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x26), eq((short) 1));
    fileHeader.setFontWidth(1);
  }
  
  public void testSetFontWidthV6() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 6));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x27), eq((short) 1));
    fileHeader.setFontWidth(1);
  }
  
  public void testSetFontHeightV5() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 5));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x27), eq((short) 2));
    fileHeader.setFontHeight(2);
  }

  public void testSetFontHeightV6() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x00)).will(returnValue((short) 6));
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(0x26), eq((short) 2));
    fileHeader.setFontHeight(2);
  }
  
  public void testUseMouseFalse() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x10)).will(returnValue((short) 2));
    assertFalse(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }

  public void testUseMouseTrue() {
    
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0x10)).will(returnValue((short) 63));
    assertTrue(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }
  
  public void testGetCustomAlphabetTable() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x34)).will(returnValue(63));
    fileHeader.getCustomAlphabetTable();
  }
  
  // Simulate a situation to set mouse coordinates
  
  public void testSetMouseCoordinatesNoExtensionTable() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x36)).will(returnValue(0));
    fileHeader.setMouseCoordinates(1, 2);

  }
  
  public void testSetMouseCoordinatesHasExtensionTable() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x36)).will(returnValue(100));
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(100)).will(returnValue(2));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(102), eq(1));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(104), eq(2));
    fileHeader.setMouseCoordinates(1, 2);
  }
  
  public void testGetUnicodeTranslationTableNoExtensionTable() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x36)).will(returnValue(0));
    assertEquals(0, fileHeader.getCustomAccentTable());
  }
  
  public void testGetCustomUnicodeTranslationTableNoTableInExtTable() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x36)).will(returnValue(100));
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(100)).will(returnValue(2));
    assertEquals(0, fileHeader.getCustomAccentTable());
  }

  public void testGetCustomUnicodeTranslationTableHasExtAddress() {
    
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(0x36)).will(returnValue(100));
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(100)).will(returnValue(3));
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(106)).will(returnValue(1234));
    assertEquals(1234, fileHeader.getCustomAccentTable());
  }
}
