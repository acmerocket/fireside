/*
 * Copyright 2005-2009 by Wei-ju Wu
 * Copyright (c) 2005-2009, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
