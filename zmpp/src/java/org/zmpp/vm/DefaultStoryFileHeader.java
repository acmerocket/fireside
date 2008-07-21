/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package org.zmpp.vm;

import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.toUnsigned16;

/**
 * This is the default implementation of the StoryFileHeader interface.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultStoryFileHeader implements StoryFileHeader {

  /**
   * The memory map.
   */
  private Memory memory;
  
  /**
   * Constructor.
   * @param memory a Memory object
   */
  public DefaultStoryFileHeader(final Memory memory) {
    this.memory = memory;
  }

  /**
   * {@inheritDoc}
   */
  public int getVersion() { return memory.readUnsigned8(0x00); }
  
  /**
   * {@inheritDoc}
   */
  public int getRelease() { return memory.readUnsigned16(0x02); }
  
  /**
   * {@inheritDoc}
   */
  public int getHighMemAddress() { return memory.readUnsigned16(0x04); }
  
  /**
   * {@inheritDoc}
   */
  public int getProgramStart() { return memory.readUnsigned16(0x06); }
  
  /**
   * {@inheritDoc}
   */
  public int getDictionaryAddress() {
    return memory.readUnsigned16(0x08);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getObjectTableAddress() {
    return memory.readUnsigned16(0x0a);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getGlobalsAddress() { return memory.readUnsigned16(0x0c); }
  
  /**
   * {@inheritDoc}
   */
  public int getStaticsAddress() { return memory.readUnsigned16(0x0e); }
  
  /**
   * {@inheritDoc}
   */
  public String getSerialNumber() { return extractAscii(0x12, 6); }
  
  /**
   * {@inheritDoc}
   */
  public int getAbbreviationsAddress() {
    return memory.readUnsigned16(0x18);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getFileLength() {
    // depending on the story file version we have to multiply the
    // file length in the header by a constant
    int fileLength = memory.readUnsigned16(0x1a);
    if (getVersion() <= 3) {
      
      fileLength *= 2;
      
    } else if (getVersion() <= 5) {
      
      fileLength *= 4;
      
    } else {
      
      fileLength *= 8;
    }
    return fileLength;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getChecksum() { return memory.readUnsigned16(0x1c); }
  
  /**
   * {@inheritDoc}
   */
  public void setInterpreterNumber(final int number) {
    memory.writeUnsigned8(0x1e, (char) number);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getInterpreterNumber() {
    return memory.readUnsigned8(0x1e);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setInterpreterVersion(final int version) {
    if (getVersion() == 4 || getVersion() == 5) {
      memory.writeUnsigned8(0x1f, String.valueOf(version).charAt(0));
    } else {
      memory.writeUnsigned8(0x1f, (char) version);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreenWidth(final int numChars) {
    memory.writeUnsigned8(0x21, (char) numChars);    
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreenWidthUnits(final int units) {
    memory.writeUnsigned16(0x22, toUnsigned16(units));
  }
  
  /**
   * {@inheritDoc}
   */  
  public int getScreenWidthUnits() {
    return memory.readUnsigned16(0x22);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getScreenWidth() {
    return memory.readUnsigned8(0x21);
  }

  /**
   * {@inheritDoc}
   */
  public int getScreenHeight() {
    return memory.readUnsigned8(0x20);
  }

  /**
   * {@inheritDoc}
   */  
  public int getScreenHeightUnits() {
    return memory.readUnsigned16(0x24);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreenHeight(final int numLines) {
    memory.writeUnsigned8(0x20, (char) numLines);
  }

  /**
   * {@inheritDoc}
   */
  public void setScreenHeightUnits(final int units) {
    memory.writeUnsigned16(0x24, toUnsigned16(units));
  }
  
  /**
   * {@inheritDoc}
   */
  public int getRoutineOffset() {
    return memory.readUnsigned16(0x28);
  }

  /**
   * {@inheritDoc}
   */
  public int getStaticStringOffset() {
    return memory.readUnsigned16(0x2a);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getDefaultBackground() {
    return memory.readUnsigned8(0x2c);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getDefaultForeground() {
    return memory.readUnsigned8(0x2d);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setDefaultBackground(final int color) {
    memory.writeUnsigned8(0x2c, (char) color);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setDefaultForeground(final int color) {
    memory.writeUnsigned8(0x2d, (char) color);
  }
 
  /**
   * {@inheritDoc}
   */
  public void setStandardRevision(final int major, final int minor) {
    memory.writeUnsigned8(0x32, (char) major);
    memory.writeUnsigned8(0x33, (char) minor);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getTerminatorsAddress() {
    return memory.readUnsigned16(0x2e);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setFontWidth(final int units) {
    if (getVersion() == 6) {
      memory.writeUnsigned8(0x27, (char) units);
    } else {
      memory.writeUnsigned8(0x26, (char) units);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public int getFontWidth() {
    return (getVersion() == 6) ? memory.readUnsigned8(0x27)
        : memory.readUnsigned8(0x26);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setFontHeight(final int units) {
    if (getVersion() == 6) {
      memory.writeUnsigned8(0x26, (char) units);
    } else {
      memory.writeUnsigned8(0x27, (char) units);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public int getFontHeight() {
    return (getVersion() == 6) ? memory.readUnsigned8(0x26)
        : memory.readUnsigned8(0x27);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getCustomAlphabetTable() {
    return memory.readUnsigned16(0x34);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setMouseCoordinates(final int x, final int y) {
    // check the extension table
    final int extTable = memory.readUnsigned16(0x36);
    if (extTable > 0) {
      
      final int numwords = memory.readUnsigned16(extTable);
      if (numwords >= 1) {
        
        memory.writeUnsigned16(extTable + 2, toUnsigned16(x));
      }
      if (numwords >= 2) {
        
        memory.writeUnsigned16(extTable + 4, toUnsigned16(y));
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public int getCustomAccentTable() {
    // check the extension table
    int result = 0;
    final int extTable = memory.readUnsigned16(0x36);
    
    if (extTable > 0) {
      
      final int numwords = memory.readUnsigned16(extTable);
      if (numwords >= 3) {
        
        result = memory.readUnsigned16(extTable + 6);
      }
    }
    return result;
  }
  
  // ***********************************************************************
  // ****** Attributes
  // **********************************
  
  /**
   * {@inheritDoc}
   */
  public void setEnabled(final Attribute attribute, final boolean flag) {
    switch (attribute) {

    case DEFAULT_FONT_IS_VARIABLE:
      setDefaultFontIsVariablePitch(flag);
      break;
    case TRANSCRIPTING:
      setTranscripting(flag);
      break;
    case FORCE_FIXED_FONT:
      setForceFixedFont(flag);
      break;
    case SUPPORTS_TIMED_INPUT:
      setTimedInputAvailable(flag);
      break;
    case SUPPORTS_FIXED_FONT:
      setFixedFontAvailable(flag);
      break;
    case SUPPORTS_BOLD:
      setBoldFaceAvailable(flag);
      break;
    case SUPPORTS_ITALIC:
      setItalicAvailable(flag);
      break;
    case SUPPORTS_SCREEN_SPLITTING:
      setScreenSplittingAvailable(flag);
      break;
    case SUPPORTS_STATUSLINE:
      setStatusLineAvailable(flag);
      break;
    case SUPPORTS_COLOURS:
      setSupportsColours(flag);
    default:
      break;
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled(final Attribute attribute) {
    switch (attribute) {
    
    case TRANSCRIPTING:
      return isTranscriptingOn();
    case FORCE_FIXED_FONT:
      return forceFixedFont();
    case SCORE_GAME:
      return isScoreGame();
    case DEFAULT_FONT_IS_VARIABLE:
      return defaultFontIsVariablePitch();
    case USE_MOUSE:
      return useMouse();
    default:
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setOutputStream3TextWidth(int units) {
    memory.writeUnsigned16(0x30, toUnsigned16(units));
  }
  
  /**
   * {@inheritDoc}
   */
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < 55; i++) {
      
      builder.append(String.format("Addr: %02x Byte: %02x\n", i, memory.readUnsigned8(i)));
    }
    return builder.toString();
  }
  
  // ************************************************************************
  // ****** Private section
  // *******************************

  /**
   * Extract an ASCII string of the specified length starting at the specified
   * address.
   * 
   * @param address the start address
   * @param length the length of the ASCII string
   * @return the ASCII string at the specified position
   */
  private String extractAscii(final int address, final int length) {
    final StringBuilder builder = new StringBuilder();
    for (int i = address; i < address + length; i++) {
      
      builder.append((char) memory.readUnsigned8(i));
    }
    return builder.toString();
  }
    
  private void setTranscripting(final boolean flag) {
    int flags = memory.readUnsigned8(0x10);
    flags = flag ? (flags | 1) : (flags & 0xfe);
    memory.writeUnsigned8(0x10, (char) flags);
  }
  
  private boolean isTranscriptingOn() {
    return (memory.readUnsigned8(0x10) & 1) > 0;
  }
  
  private boolean forceFixedFont() {
    return (memory.readUnsigned8(0x10) & 2) > 0;
  }
  
  private void setForceFixedFont(final boolean flag) {
    int flags = memory.readUnsigned8(0x10);
    flags = flag ? (flags | 2) : (flags & 0xfd);
    memory.writeUnsigned8(0x10, (char) flags);
  }
  
  private void setTimedInputAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 128) : (flags & 0x7f);
    memory.writeUnsigned8(0x01, (char) flags);
  }
  
  private boolean isScoreGame() {
    return (memory.readUnsigned8(0x01) & 2) == 0;
  }
  
  private void setFixedFontAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 16) : (flags & 0xef);
    memory.writeUnsigned8(0x01, (char) flags);
  }
  
  private void setBoldFaceAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 4) : (flags & 0xfb);
    memory.writeUnsigned8(0x01, (char) flags);
  }  

  private void setItalicAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 8) : (flags & 0xf7);
    memory.writeUnsigned8(0x01, (char) flags);
  }
  
  private void setScreenSplittingAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 32) : (flags & 0xdf);
    memory.writeUnsigned8(0x01, (char) flags);
  }
  
  private void setStatusLineAvailable(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 16) : (flags & 0xef);
    memory.writeUnsigned8(0x01, (char) flags);
  }  

  private void setDefaultFontIsVariablePitch(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 64) : (flags & 0xbf);
    memory.writeUnsigned8(0x01, (char) flags);
  }

  private boolean defaultFontIsVariablePitch() {
    return (memory.readUnsigned8(0x01) & 64) > 0;
  }
  
  private void setSupportsColours(final boolean flag) {
    int flags = memory.readUnsigned8(0x01);
    flags = flag ? (flags | 1) : (flags & 0xfe);
    memory.writeUnsigned8(0x01, (char) flags);
  }
  
  private boolean useMouse() {
    return (memory.readUnsigned8(0x10) & 32) > 0;
  }
}
