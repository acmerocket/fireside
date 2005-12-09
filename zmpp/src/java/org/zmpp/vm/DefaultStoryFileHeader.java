/*
 * $Id$
 * 
 * Created on 2005/09/23
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
package org.zmpp.vm;

import org.zmpp.base.MemoryAccess;

/**
 * This is the default implementation of the StoryFileHeader interface.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class DefaultStoryFileHeader implements StoryFileHeader {

  /**
   * The memory map.
   */
  private MemoryAccess memaccess;
  
  /**
   * Constructor.
   * 
   * @param memaccess a MemoryReadAccess object
   */
  public DefaultStoryFileHeader(MemoryAccess memaccess) {
    
    this.memaccess = memaccess;
  }

  /**
   * {@inheritDoc}
   */
  public int getVersion() { return memaccess.readUnsignedByte(0x00); }
  
  /**
   * {@inheritDoc}
   */
  public int getRelease() { return memaccess.readUnsignedShort(0x02); }
  
  /**
   * {@inheritDoc}
   */
  public int getHighMemAddress() { return memaccess.readUnsignedShort(0x04); }
  
  /**
   * {@inheritDoc}
   */
  public int getProgramStart() { return memaccess.readUnsignedShort(0x06); }
  
  /**
   * {@inheritDoc}
   */
  public int getDictionaryAddress() {
    
    return memaccess.readUnsignedShort(0x08);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getObjectTableAddress() {
    
    return memaccess.readUnsignedShort(0x0a);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getGlobalsAddress() { return memaccess.readUnsignedShort(0x0c); }
  
  /**
   * {@inheritDoc}
   */
  public int getStaticsAddress() { return memaccess.readUnsignedShort(0x0e); }
  
  /**
   * {@inheritDoc}
   */
  public String getSerialNumber() { return extractAscii(0x12, 6); }
  
  /**
   * {@inheritDoc}
   */
  public int getAbbreviationsAddress() {
    
    return memaccess.readUnsignedShort(0x18);
  }
  
  /**
   * {@inheritDoc}
   */
  public int getFileLength() {
    
    int fileLength = memaccess.readUnsignedShort(0x1a);
    if (getVersion() <= 3) fileLength *= 2;      
    return fileLength;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getChecksum() { return memaccess.readUnsignedShort(0x1c); }
  
  /**
   * {@inheritDoc}
   */
  public void setInterpreterNumber(int number) {
    
    memaccess.writeUnsignedByte(0x1e, (short) number);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setInterpreterVersion(int version) {
    
    if (getVersion() == 4 || getVersion() == 5) {
      
      memaccess.writeUnsignedByte(0x1f,
                                  (short) String.valueOf(version).charAt(0));
    } else {
      
      memaccess.writeUnsignedByte(0x1f, (short) version);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreenWidth(int numChars) {

    memaccess.writeUnsignedByte(0x21, (short) numChars);
    
    if (getVersion() >= 5) {
      
      memaccess.writeUnsignedShort(0x22, numChars);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public int getScreenWidth() {
    
    return memaccess.readUnsignedByte(0x21);
  }
  
  /**
   * {@inheritDoc}
   */
  public void setScreenHeight(int numLines) {
    
    memaccess.writeUnsignedByte(0x20, (short) numLines);
    
    if (getVersion() >= 5) {
      
      memaccess.writeUnsignedShort(0x24, numLines);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void setEnabled(Attribute attribute, boolean flag) {
    
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
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isEnabled(Attribute attribute) {
    
    switch (attribute) {
    
    case TRANSCRIPTING:
      return isTranscriptingOn();
    case FORCE_FIXED_FONT:
      return forceFixedFont();
    case SCORE_GAME:
      return isScoreGame();
    case DEFAULT_FONT_IS_VARIABLE:
      return defaultFontIsVariablePitch();
    }
    return false;
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
  private String extractAscii(int address, int length) {
    
    StringBuilder builder = new StringBuilder();
    for (int i = address; i < address + length; i++) {
      
      builder.append((char) memaccess.readUnsignedByte(i));
    }
    return builder.toString();
  }
    
  private void setTranscripting(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x10);
    flags = flag ? (flags | 1) : (flags & 0xfe);
    memaccess.writeUnsignedByte(0x10, (short) flags);
  }
  
  private boolean isTranscriptingOn() {

    return (memaccess.readUnsignedByte(0x10) & 1) > 0;
  }
  
  private boolean forceFixedFont() {
    
    return (memaccess.readUnsignedByte(0x10) & 2) > 0;
  }
  
  private void setForceFixedFont(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x10);
    flags = flag ? (flags | 2) : (flags & 0xfd);
    memaccess.writeUnsignedByte(0x10, (short) flags);
  }
  
  private void setTimedInputAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 128) : (flags & 0x7f);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }
  
  private boolean isScoreGame() {
    
    return (memaccess.readUnsignedByte(0x01) & 2) == 0;
  }
  
  private void setFixedFontAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 16) : (flags & 0xef);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }
  
  private void setBoldFaceAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 4) : (flags & 0xfb);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }  

  private void setItalicAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 8) : (flags & 0xf7);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }
  
  private void setScreenSplittingAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 32) : (flags & 0xdf);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }
  
  private void setStatusLineAvailable(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 16) : (flags & 0xef);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }  

  private void setDefaultFontIsVariablePitch(boolean flag) {
    
    int flags = memaccess.readUnsignedByte(0x01);
    flags = flag ? (flags | 64) : (flags & 0xbf);
    memaccess.writeUnsignedByte(0x01, (short) flags);
  }

  private boolean defaultFontIsVariablePitch() {
    
    return (memaccess.readUnsignedByte(0x01) & 64) > 0;
  }
}
