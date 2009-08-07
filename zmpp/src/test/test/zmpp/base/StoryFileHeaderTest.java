/*
 * Created on 09/23/2005
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
package test.zmpp.base;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.base.Memory;
import org.zmpp.base.DefaultStoryFileHeader;
import org.zmpp.base.StoryFileHeader;
import org.zmpp.base.StoryFileHeader.Attribute;

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
      atLeast(1).of (memory).readUnsigned8(0x00); will(returnValue((char) 3));
    }});
    assertEquals(3, fileHeader.getVersion());
  }

  @Test
  public void testGetSerialNumber() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x012); will(returnValue('0'));
      one (memory).readUnsigned8(0x013); will(returnValue('5'));
      one (memory).readUnsigned8(0x014); will(returnValue('1'));
      one (memory).readUnsigned8(0x015); will(returnValue('2'));
      one (memory).readUnsigned8(0x016); will(returnValue('0'));
      one (memory).readUnsigned8(0x017); will(returnValue('9'));
    }});
    assertEquals("051209", fileHeader.getSerialNumber());
  }

  @Test
  public void testGetFileLengthV3() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x00); will(returnValue((char) 3));
      one (memory).readUnsigned16(0x1a); will(returnValue((char) 4718));
    }});
    assertEquals(4718 * 2, fileHeader.getFileLength());
  }

  @Test
  public void testGetFileLengthV4() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x00); will(returnValue((char) 4));
      one (memory).readUnsigned16(0x1a); will(returnValue((char) 4718));
    }});
    assertEquals(4718 * 4, fileHeader.getFileLength());
  }

  @Test
  public void testGetFileLengthV8() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x00); will(returnValue((char) 8));
      one (memory).readUnsigned16(0x1a); will(returnValue((char) 4718));
    }});
    assertEquals(4718 * 8, fileHeader.getFileLength());
  }
  
  @Test
  public void testSetInterpreterVersionV5() {
    // Story file version 4 or 5: version number as string
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x00); will(returnValue((char) 5));
      one (memory).writeUnsigned8(0x1f, '4');
    }});
    fileHeader.setInterpreterVersion(4);
  }
  
  @Test
  public void testSetInterpreterVersionV8() {
    // Story file version > 5: version number as value
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x00); will(returnValue((char) 8));
      one (memory).writeUnsigned8(0x1f, (char) 4);
    }});
    fileHeader.setInterpreterVersion(4);
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
      atLeast(1).of (memory).readUnsigned16(0x10); will(returnValue((char) 0));
      one (memory).writeUnsigned16(0x10, (char) 1);
      one (memory).writeUnsigned16(0x10, (char) 0);
    }});
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, true);
    fileHeader.setEnabled(Attribute.TRANSCRIPTING, false);
  }
  
  @Test
  public void testIsTranscriptingEnabled() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned16(0x10);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 0)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
    assertFalse(fileHeader.isEnabled(Attribute.TRANSCRIPTING));
  }
  
  @Test
  public void testSetForceFixedFont() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned16(0x10); will(returnValue((char) 1));
      one (memory).writeUnsigned16(0x10, (char) 3);
      one (memory).writeUnsigned16(0x10, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.FORCE_FIXED_FONT, false);
  }
  
  @Test
  public void testIsForceFixedFont() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned16(0x10);
        will(onConsecutiveCalls(returnValue((char) 6), returnValue((char) 5)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
    assertFalse(fileHeader.isEnabled(Attribute.FORCE_FIXED_FONT));
  }

  @Test
  public void testSetSupportsTimedInput() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 3), returnValue((char) 131)));
      one (memory).writeUnsigned8(0x01, (char) 131);
      one (memory).writeUnsigned8(0x01, (char) 3);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_TIMED_INPUT, false);
  }

  @Test
  public void testIsScoreGame() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 5), returnValue((char) 7)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.SCORE_GAME));
    assertFalse(fileHeader.isEnabled(Attribute.SCORE_GAME));
  }

  @Test
  public void testSetSupportsFixed() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 17)));
      one (memory).writeUnsigned8(0x01, (char) 17);
      one (memory).writeUnsigned8(0x01, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_FIXED_FONT, false);
  }

  @Test
  public void testSetSupportsBold() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 5)));
      one (memory).writeUnsigned8(0x01, (char) 5);
      one (memory).writeUnsigned8(0x01, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_BOLD, false);
  }
  
  @Test
  public void testSetSupportsItalic() {    
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 9)));
      one (memory).writeUnsigned8(0x01, (char) 9);
      one (memory).writeUnsigned8(0x01, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_ITALIC, false);
  }

  @Test
  public void testSetSupportsScreenSplitting() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 33)));
      one (memory).writeUnsigned8(0x01, (char) 33);
      one (memory).writeUnsigned8(0x01, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_SCREEN_SPLITTING, false);
  }

  @Test
  public void testSetSupportsStatusLine() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 17), returnValue((char) 1)));
      one (memory).writeUnsigned8(0x01, (char) 1);
      one (memory).writeUnsigned8(0x01, (char) 17);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_STATUSLINE, false);
  }

  @Test
  public void testSetDefaultFontIsVariable() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 1), returnValue((char) 65)));
      one (memory).writeUnsigned8(0x01, (char) 65);
      one (memory).writeUnsigned8(0x01, (char) 1);
    }});
    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, true);
    fileHeader.setEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE, false);
  }

  @Test
  public void testIsDefaultFontVariable() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 69), returnValue((char) 7)));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
    assertFalse(fileHeader.isEnabled(Attribute.DEFAULT_FONT_IS_VARIABLE));
  }
  
  @Test
  public void testSetSupportsColors() {
    context.checking(new Expectations() {{
      atLeast(1).of (memory).readUnsigned8(0x01);
        will(onConsecutiveCalls(returnValue((char) 4), returnValue((char) 5)));
      one (memory).writeUnsigned8(0x01, (char) 5);
      one (memory).writeUnsigned8(0x01, (char) 4);
    }});
    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, true);
    fileHeader.setEnabled(Attribute.SUPPORTS_COLOURS, false);
  }
  
  @Test
  public void testSetFontWidthV5() { 
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x00); will(returnValue((char) 5));
      one (memory).writeUnsigned8(0x26, (char) 1);
    }});
    fileHeader.setFontWidth(1);
  }
  
  @Test
  public void testSetFontWidthV6() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x00); will(returnValue((char) 6));
      one (memory).writeUnsigned8(0x27, (char) 1);
    }});
    fileHeader.setFontWidth(1);
  }
  

  @Test
  public void testSetFontHeightV5() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x00); will(returnValue((char) 5));
      one (memory).writeUnsigned8(0x27, (char) 2);
    }});
    fileHeader.setFontHeight(2);
  }

  @Test
  public void testSetFontHeightV6() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x00); will(returnValue((char) 6));
      one (memory).writeUnsigned8(0x26, (char) 2);
    }});
    fileHeader.setFontHeight(2);
  }

  @Test
  public void testUseMouseFalse() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x10); will(returnValue((char) 2));
    }});
    assertFalse(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }

  @Test
  public void testUseMouseTrue() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned8(0x10); will(returnValue((char) 63));
    }});
    assertTrue(fileHeader.isEnabled(Attribute.USE_MOUSE));    
  }

  // Simulate a situation to set mouse coordinates
  @Test
  public void testSetMouseCoordinatesNoExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(0x36); will(returnValue((char) 0));
    }});
    fileHeader.setMouseCoordinates(1, 2);
  }
  
  @Test
  public void testSetMouseCoordinatesHasExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(0x36); will(returnValue((char) 100));
      one (memory).readUnsigned16(100); will(returnValue((char) 2));
      one (memory).writeUnsigned16(102, (char) 1);
      one (memory).writeUnsigned16(104, (char) 2);
    }});
    fileHeader.setMouseCoordinates(1, 2);
  }

  @Test
  public void testGetUnicodeTranslationTableNoExtensionTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(0x36); will(returnValue((char) 0));
    }});
    assertEquals(0, fileHeader.getCustomAccentTable());
  }

  @Test
  public void testGetCustomUnicodeTranslationTableNoTableInExtTable() {
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(0x36); will(returnValue((char) 100));
      one (memory).readUnsigned16(100); will(returnValue((char) 2));
    }});
    assertEquals(0, fileHeader.getCustomAccentTable());
  }

  @Test
  public void testGetCustomUnicodeTranslationTableHasExtAddress() { 
    context.checking(new Expectations() {{
      one (memory).readUnsigned16(0x36); will(returnValue((char) 100));
      one (memory).readUnsigned16(100); will(returnValue((char) 3));
      one (memory).readUnsigned16(106); will(returnValue((char) 1234));
    }});
    assertEquals(1234, fileHeader.getCustomAccentTable());
  }
}
