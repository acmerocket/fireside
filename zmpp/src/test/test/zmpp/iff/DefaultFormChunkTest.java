/*
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
package test.zmpp.iff;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import static test.zmpp.testutil.ZmppTestUtil.*;

/**
 * Test class for DefaultFormChunk.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultFormChunkTest {

  private static Memory formChunkData;
  private static FormChunk formChunk;
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    File testSaveFile = createLocalFile("testfiles/leathersave.ifzs");
    RandomAccessFile saveFile = new RandomAccessFile(testSaveFile, "r");
    byte[] data = new byte[(int) saveFile.length()];
    saveFile.readFully(data);
    formChunkData = new DefaultMemory(data);
    formChunk = new DefaultFormChunk(formChunkData);
    saveFile.close();
  }
  
  @Test
  public void testInvalidIff() {
    byte[] illegalData = {
      (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
      (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
      (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
      (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05,
    };
    try {
      new DefaultFormChunk(new DefaultMemory(illegalData));
      fail("IOException should be thrown on an illegal IFF file");
    } catch (IOException expected) {
      assertTrue(expected.getMessage() != null);
    }
  }
  
  @Test
  public void testCreation() {
    assertTrue(formChunk.isValid());
    assertEquals("FORM", new String(formChunk.getId()));
    assertEquals(512, formChunk.getSize());
    assertEquals("IFZS", new String(formChunk.getSubId()));
  }
  
  @Test
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
    assertEquals(0x000c, result.get(0).getAddress());
    assertEquals("CMem", new String(result.get(1).getId()));
    assertEquals(351, result.get(1).getSize());
    assertEquals(0x0022, result.get(1).getAddress());
    assertEquals("Stks", new String(result.get(2).getId()));
    assertEquals(118, result.get(2).getSize());
    assertEquals(0x018a, result.get(2).getAddress());
    assertEquals(3, result.size());
  }
  
  @Test
  public void testGetSubChunk() {
    assertNotNull(formChunk.getSubChunk("IFhd"));
    assertNotNull(formChunk.getSubChunk("CMem"));
    assertNotNull(formChunk.getSubChunk("Stks"));
    assertNull(formChunk.getSubChunk("Test"));
  }
  
  @Test
  public void testGetSubChunkByAddress() {
    assertEquals("IFhd", new String(formChunk.getSubChunk(0x000c).getId()));
    assertEquals("CMem", new String(formChunk.getSubChunk(0x0022).getId()));
    assertEquals("Stks", new String(formChunk.getSubChunk(0x018a).getId()));
    assertNull(formChunk.getSubChunk(0x1234));
  }
}
