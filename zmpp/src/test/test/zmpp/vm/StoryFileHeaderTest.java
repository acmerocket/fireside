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
package test.zmpp.vm;


/**
 * This class is a test for the StoryFileHeader class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StoryFileHeaderTest extends MemoryMapSetup {

  public void testHeader() throws Exception {
    
    assertEquals(3, fileheader.getVersion());
    assertEquals(112, fileheader.getFlags1());
    assertEquals(34, fileheader.getRelease());
    assertEquals(0x3709, fileheader.getHighMemAddress());
    assertEquals(0x37d9, fileheader.getProgramStart());
    assertEquals(0x285a, fileheader.getDictionaryAddress());
    assertEquals(0x03c6, fileheader.getObjectTableAddress());
    assertEquals(0x02b4, fileheader.getGlobalsAddress());
    assertEquals(0x2187, fileheader.getStaticsAddress());
    assertEquals(0, fileheader.getFlags2());
    assertEquals(0x01f4, fileheader.getAbbreviationsAddress());
    assertEquals("871124", fileheader.getSerialNumber());
    assertEquals(0xd870, fileheader.getChecksum());
    assertEquals(0, fileheader.getRevision());
    assertEquals(0xcbf8, fileheader.getFileLength());
    int abbraddr = fileheader.getAbbreviationsAddress();
    int globaddr = fileheader.getGlobalsAddress();
    int numAbbrev = (globaddr - abbraddr) / 2;
    assertEquals(96, numAbbrev);
  }
  
  public void testFlags1() {
    
    assertTrue(fileheader.isScoreGame());
    
    fileheader.setStatusLineAvailable(true);
    assertTrue((fileheader.getFlags1() & 16) > 0);
    fileheader.setStatusLineAvailable(false);
    assertTrue((fileheader.getFlags1() & 16) == 0);
    
    fileheader.setScreenSplittingAvailable(true);
    assertTrue((fileheader.getFlags1() & 32) > 0);
    fileheader.setScreenSplittingAvailable(false);
    assertTrue((fileheader.getFlags1() & 32) == 0);
    
    fileheader.setDefaultFontIsVariablePitch(true);
    assertTrue((fileheader.getFlags1() & 64) > 0);
    assertTrue(fileheader.defaultFontIsVariablePitch());
    
    fileheader.setDefaultFontIsVariablePitch(false);
    assertTrue((fileheader.getFlags1() & 64) == 0);
    assertFalse(fileheader.defaultFontIsVariablePitch());
  }
  
  public void testFlags2() {
   
    fileheader.setTranscripting(true);
    assertTrue((fileheader.getFlags2() & 1) > 0);
    assertTrue(fileheader.isTranscriptingOn());

    fileheader.setTranscripting(false);
    assertTrue((fileheader.getFlags2() & 1) == 0);
    assertFalse(fileheader.isTranscriptingOn());
    
    fileheader.setForceFixedFont(true);
    assertTrue(fileheader.forceFixedFont());
    
    fileheader.setForceFixedFont(false);
    assertFalse(fileheader.forceFixedFont());
  }
}
