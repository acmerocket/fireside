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

import org.zmpp.vm.StoryFileHeader;

/**
 * This class is a test for the StoryFileHeader class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class StoryFileHeaderTest extends MemoryMapSetup {

  private StoryFileHeader header;
  
  public void setUp() throws Exception {
    
    super.setUp();
    header = new StoryFileHeader(minizorkmap);
  }
  
  public void tearDown() throws Exception {
    
  }
    
  public void testHeader() throws Exception {
    
    assertEquals(3, header.getVersion());
    assertEquals(0, header.getFlags1());
    assertEquals(0x3709, header.getBaseAddress());
    assertEquals(0x37d9, header.getProgramStart());
    assertEquals(0x285a, header.getDictionaryAddress());
    assertEquals(0x03c6, header.getObjectTableAddress());
    assertEquals(0x02b4, header.getGlobalsAddress());
    assertEquals(0x2187, header.getStaticsAddress());
    assertEquals(0, header.getFlags2());
    assertEquals(0x01f4, header.getAbbreviationsAddress());
    assertEquals("871124", header.getSerialNumber());
    assertEquals(0xd870, header.getChecksum());
    assertEquals(0, header.getRevision());
    assertEquals(0, header.getInterpreter());
    assertEquals(0, header.getInterpreterVersion());
    assertEquals(0xcbf8, header.getFileLength());
    int abbraddr = header.getAbbreviationsAddress();
    int globaddr = header.getGlobalsAddress();
    int numAbbrev = (globaddr - abbraddr) / 2;
    assertEquals(96, numAbbrev);
  }  
}
