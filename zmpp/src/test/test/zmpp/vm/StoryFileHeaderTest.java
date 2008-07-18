/*
 * $Id$
 * 
 * Created on 09/23/2005
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
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.base.Memory;
import org.zmpp.vm.DefaultStoryFileHeader;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.StoryFileHeader.Attribute;

/**
 * This class is a test for the StoryFileHeader class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class StoryFileHeaderTest {
  private Mockery context = new JUnit4Mockery();
  private Memory memory;
  private StoryFileHeader fileHeader;
  
  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    fileHeader = new DefaultStoryFileHeader(memory); 
  }
  
  @Test
  public void testGetVersion() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x00); will(returnValue((short) 3));
    }});
    assertEquals(3, fileHeader.getVersion());
  }
  
  @Test
  public void testGetRelease() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x02); will(returnValue(35));
    }});
    assertEquals(35, fileHeader.getRelease());
  }
  
  @Test
  public void testGetHighMemAddress() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x04); will(returnValue(4711));
    }});
    assertEquals(4711, fileHeader.getHighMemAddress());
  }
  
  @Test
  public void testGetInitialPC() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x06); will(returnValue(4712));
    }});
    assertEquals(4712, fileHeader.getProgramStart());
  }

  @Test
  public void testGetDictionaryAddress() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x08); will(returnValue(4713));
    }});
    assertEquals(4713, fileHeader.getDictionaryAddress());
  }
  
  @Test
  public void testGetObjectTableAddress() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x0a); will(returnValue(4714));
    }});
    assertEquals(4714, fileHeader.getObjectTableAddress());
  }

  @Test
  public void testGetGlobalsAddress() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedShort(0x0c); will(returnValue(4715));
    }});
    assertEquals(4715, fileHeader.getGlobalsAddress());
  }

  @Test
  public void testGetStaticMemAddress() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x0e); will(returnValue(4716));
    }});
    assertEquals(4716, fileHeader.getStaticsAddress());
  }
  
  @Test
  public void testGetSerialNumber() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x012); will(returnValue((short) '0'));
      one (memory).readUnsignedByte(0x013); will(returnValue((short) '5'));
      one (memory).readUnsignedByte(0x014); will(returnValue((short) '1'));
      one (memory).readUnsignedByte(0x015); will(returnValue((short) '2'));
      one (memory).readUnsignedByte(0x016); will(returnValue((short) '0'));
      one (memory).readUnsignedByte(0x017); will(returnValue((short) '9'));
    }});
    assertEquals("051209", fileHeader.getSerialNumber());
  }

  @Test
  public void testGetAbbreviationsAddress() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x18); will(returnValue(4717));
    }});
    assertEquals(4717, fileHeader.getAbbreviationsAddress());
  }

  @Test
  public void testGetFileLengthV3() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x00); will(returnValue((short) 3));
      one (memory).readUnsignedShort(0x1a); will(returnValue(4718));
    }});
    assertEquals(4718 * 2, fileHeader.getFileLength());
  }

  @Test
  public void testGetFileLengthV4() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x00); will(returnValue((short) 4));
      one (memory).readUnsignedShort(0x1a); will(returnValue(4718));
    }});
    assertEquals(4718 * 4, fileHeader.getFileLength());
  }

  @Test
  public void testGetFileLengthV8() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x00); will(returnValue((short) 8));
      one (memory).readUnsignedShort(0x1a); will(returnValue(4718));
    }});
    assertEquals(4718 * 8, fileHeader.getFileLength());
  }
  
  @Test
  public void testGetChecksum() {    
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x1c); will(returnValue(4719));
    }});
    assertEquals(4719, fileHeader.getChecksum());
  }
  
  @Test
  public void testSetScreenHeight() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedByte(0x20, (short) 255);
    }});
    fileHeader.setScreenHeight(255);
  }
  
  @Test
  public void testSetScreenHeightUnits() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedShort(0x24, (short) 40);
    }});
    fileHeader.setScreenHeightUnits(40);
  }

  @Test
  public void testGetScreenWidth() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x21); will(returnValue((short) 82));
    }});
    assertEquals(82, fileHeader.getScreenWidth());
  }
  
  @Test
  public void testSetScreenWidth() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedByte(0x21, (short) 82);
    }});
    fileHeader.setScreenWidth(82);
  }  

  @Test
  public void testSetScreenWidthUnits() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedShort(0x22, 82);
    }});
    fileHeader.setScreenWidthUnits(82);
  }  
  
  @Test
  public void testSetInterpreterVersionV5() {
    // Story file version 4 or 5: version number as string
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x00); will(returnValue((short) 5));
      one (memory).writeUnsignedByte(0x1f, (short) '4');
    }});
    fileHeader.setInterpreterVersion(4);
  }
  
  @Test
  public void testSetInterpreterVersionV8() {
    // Story file version > 5: version number as value
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x00); will(returnValue((short) 8));
      one (memory).writeUnsignedByte(0x1f, (short) 4);
    }});
    fileHeader.setInterpreterVersion(4);
  }
  
  @Test
  public void testSetInterpreterNumber() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedByte(0x1e, (short) 3);
    }});
    fileHeader.setInterpreterNumber(3);
  }
  
  // *************************************************************************
  // ****** ATTRIBUTES
  // **************************
  
  @Test
  public void testIsEnabledNull() {
    // This is not matched in the code
    assertFalse(fileHeader.isEnabled(Attribute.SUPPORTS_STATUSLINE));    
  }
  
  @Test
  public void testSetTranscripting() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x10); will(returnValue((short) 0));
      one (memory).writeUnsignedByte(0x10, (short) 1);
      one (memory).writeUnsignedByte(0x10, (short) 0);
    }});
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, true);
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, false);
  }
  
  @Test
  public void testIsTranscriptingEnabled() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x10);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 0)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
    assertFalse(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
  }
  
  @Test
  public void testSetForceFixedFont() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x10); will(returnValue((short) 1));
      one (memory).writeUnsignedByte(0x10, (short) 3);
      one (memory).writeUnsignedByte(0x10, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, false);
  }
  
  @Test
  public void testIsForceFixedFont() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x10);
        will(onConsecutiveCalls(returnValue((short) 6), returnValue((short) 5)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
    assertFalse(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
  }

  @Test
  public void testSetSupportsTimedInput() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 3), returnValue((short) 131)));
      one (memory).writeUnsignedByte(0x01, (short) 131);
      one (memory).writeUnsignedByte(0x01, (short) 3);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, false);
  }

  @Test
  public void testIsScoreGame() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 5), returnValue((short) 7)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.SCORE_GAME));
    assertFalse(fileHeader.isEnabled(Attribute.SCORE_GAME));
  }

  @Test
  public void testSetSupportsFixed() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 17)));
      one (memory).writeUnsignedByte(0x01, (short) 17);
      one (memory).writeUnsignedByte(0x01, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, false);
  }

  @Test
  public void testSetSupportsBold() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 5)));
      one (memory).writeUnsignedByte(0x01, (short) 5);
      one (memory).writeUnsignedByte(0x01, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, false);
  }
  
  @Test
  public void testSetSupportsItalic() {    
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 9)));
      one (memory).writeUnsignedByte(0x01, (short) 9);
      one (memory).writeUnsignedByte(0x01, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, false);
  }

  @Test
  public void testSetSupportsScreenSplitting() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 33)));
      one (memory).writeUnsignedByte(0x01, (short) 33);
      one (memory).writeUnsignedByte(0x01, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, false);
  }

  @Test
  public void testSetSupportsStatusLine() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 17), returnValue((short) 1)));
      one (memory).writeUnsignedByte(0x01, (short) 1);
      one (memory).writeUnsignedByte(0x01, (short) 17);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, false);
  }

  @Test
  public void testSetDefaultFontIsVariable() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 1), returnValue((short) 65)));
      one (memory).writeUnsignedByte(0x01, (short) 65);
      one (memory).writeUnsignedByte(0x01, (short) 1);
    }});
    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, true);
    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, false);
  }

  @Test
  public void testIsDefaultFontVariable() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 69), returnValue((short) 7)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
    assertFalse(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
  }
  
  @Test
  public void testSetSupportsColors() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsignedByte(0x01);
        will(onConsecutiveCalls(returnValue((short) 4), returnValue((short) 5)));
      one (memory).writeUnsignedByte(0x01, (short) 5);
      one (memory).writeUnsignedByte(0x01, (short) 4);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, false);
  }
  
  @Test
  public void testGetTerminatorsAddress() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x2e); will(returnValue(1234));
    }});
    assertEquals(1234, fileHeader.getTerminatorsAddress());
  }
  
  @Test
  public void testSetStandardRevision() {  
    context.checking(new Expectations() {{
      one (memory).writeUnsignedByte(0x32, (short) 1);
      one (memory).writeUnsignedByte(0x33, (short) 2);
    }});
    fileHeader.setStandardRevision(1, 2);
  }
  
  @Test
  public void testSetFontWidthV5() { 
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x00); will(returnValue((short) 5));
      one (memory).writeUnsignedByte(0x26, (short) 1);
    }});
    fileHeader.setFontWidth(1);
  }
  
  @Test
  public void testSetFontWidthV6() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x00); will(returnValue((short) 6));
      one (memory).writeUnsignedByte(0x27, (short) 1);
    }});
    fileHeader.setFontWidth(1);
  }
  

  @Test
  public void testSetFontHeightV5() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x00); will(returnValue((short) 5));
      one (memory).writeUnsignedByte(0x27, (short) 2);
    }});
    fileHeader.setFontHeight(2);
  }

  @Test
  public void testSetFontHeightV6() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x00); will(returnValue((short) 6));
      one (memory).writeUnsignedByte(0x26, (short) 2);
    }});
    fileHeader.setFontHeight(2);
  }

  @Test
  public void testUseMouseFalse() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x10); will(returnValue((short) 2));
    }});
    assertFalse(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }

  @Test
  public void testUseMouseTrue() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedByte(0x10); will(returnValue((short) 63));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }

  @Test
  public void testGetCustomAlphabetTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x34); will(returnValue(63));
    }});    
    fileHeader.getCustomAlphabetTable();
  }
  
  // Simulate a situation to set mouse coordinates
  @Test
  public void testSetMouseCoordinatesNoExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x36); will(returnValue(0));
    }});
    fileHeader.setMouseCoordinates(1, 2);
  }
  
  @Test
  public void testSetMouseCoordinatesHasExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x36); will(returnValue(100));
      one (memory).readUnsignedShort(100); will(returnValue(2));
      one (memory).writeUnsignedShort(102, 1);
      one (memory).writeUnsignedShort(104, 2);
    }});
    fileHeader.setMouseCoordinates(1, 2);
  }

  @Test
  public void testGetUnicodeTranslationTableNoExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x36); will(returnValue(0));
    }});
    assertEquals(0, fileHeader.getCustomAccentTable());
  }

  @Test
  public void testGetCustomUnicodeTranslationTableNoTableInExtTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x36); will(returnValue(100));
      one (memory).readUnsignedShort(100); will(returnValue(2));
    }});
    assertEquals(0, fileHeader.getCustomAccentTable());
  }

  @Test
  public void testGetCustomUnicodeTranslationTableHasExtAddress() { 
    context.checking(new Expectations() {{
      one (memory).readUnsignedShort(0x36); will(returnValue(100));
      one (memory).readUnsignedShort(100); will(returnValue(3));
      one (memory).readUnsignedShort(106); will(returnValue(1234));
    }});
    assertEquals(1234, fileHeader.getCustomAccentTable());
  }
}
