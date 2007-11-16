/*
 * $Id$
 *
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
package test.zmpp.iff;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;

public class FormChunkTest extends MockObjectTestCase {

  private Memory formChunkData;
  private FormChunk formChunk; 
  
  protected void setUp() throws Exception {
    
    File testSaveFile = new File("testfiles/leathersave.ifzs");
    RandomAccessFile saveFile = new RandomAccessFile(testSaveFile, "r");
    byte[] data = new byte[(int) saveFile.length()];
    saveFile.readFully(data);
    formChunkData = new DefaultMemory(data);
    formChunk = new DefaultFormChunk(formChunkData);
    saveFile.close();
  }
  
  public void testCreation() {
    
    assertTrue(formChunk.isValid());
    assertEquals("FORM", new String(formChunk.getId()));
    assertEquals(512, formChunk.getSize());
    assertEquals("IFZS", new String(formChunk.getSubId()));
  }
  
  public void testSubchunks() {
    
    Iterator<Chunk> iter = formChunk.getSubChunks();
    List<Chunk> result = new ArrayList<Chunk>();
    
    while (iter.hasNext()) {
      
      Chunk chunk = iter.next();
      assertTrue(chunk.isValid());
      result.add(chunk);
    }
    
    assertEquals("IFhd", new String(result.get(0).getId()));
    assertEquals(13, result.get(0).getSize());
    
    assertEquals("CMem", new String(result.get(1).getId()));
    assertEquals(351, result.get(1).getSize());
    
    assertEquals("Stks", new String(result.get(2).getId()));
    assertEquals(118, result.get(2).getSize());
    
    assertEquals(3, result.size());
  }
  
  public void testGetSubChunk() {
    
    assertNotNull(formChunk.getSubChunk("IFhd".getBytes()));
    assertNotNull(formChunk.getSubChunk("CMem".getBytes()));
    assertNotNull(formChunk.getSubChunk("Stks".getBytes()));
    assertNull(formChunk.getSubChunk("Test".getBytes()));
  }
}
