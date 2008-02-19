/*
 * $Id$
 *
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
package test.zmpp.blorb;

import java.io.File;
import java.io.RandomAccessFile;

import junit.framework.TestCase;

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.blorb.BlorbResources;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultFormChunk;
import org.zmpp.iff.FormChunk;
import org.zmpp.media.SoundEffect;

/**
 * This testing class explores the structure of a BLORB file.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class BlorbFileTest extends TestCase {

  public void testLurkingHorrorBlorb() throws Exception {

    File file = new File("testfiles/Lurking.blb");
    //File file = new File("testfiles/sherlock.blb");
    RandomAccessFile rndfile = null;
    
    try {
      
      rndfile = new RandomAccessFile(file, "r");
      byte[] data = new byte[(int) rndfile.length()];
      rndfile.readFully(data);
      Memory memory = new DefaultMemory(data);
      FormChunk formchunk = new DefaultFormChunk(memory);
      
      // Chunk type
      assertEquals("IFRS", new String(formchunk.getSubId()));
      
      // Ridx chunk
      Chunk ridxChunk = formchunk.getSubChunk("RIdx".getBytes());
      assertNotNull(ridxChunk);
      /*
      MemoryReadAccess chunkmem = ridxChunk.getMemoryAccess();
      int offset = 8;
      int numresources = (int) chunkmem.readUnsigned32(offset);
      assertEquals(14, numresources);
      
      // Usage
      byte[] usage = new byte[4];
      for (int i = 0; i < 4; i++) usage[i] = chunkmem.readByte(offset + 4 + i);
      assertEquals("Snd ", new String(usage));

      // Number of resource
      int resnum = (int) chunkmem.readUnsigned32(offset + 8);
      assertEquals(3, resnum);

      // Start of resource
      int resstart = (int) chunkmem.readUnsigned32(offset + 12);
      assertEquals(308, resstart);
      Chunk subchunk = formchunk.getSubChunk(308);
      assertNotNull(subchunk);
      FormChunk soundchunk1 = new DefaultFormChunk(subchunk.getMemoryAccess());
      assertEquals("AIFF", new String(soundchunk1.getSubId()));
      
      Chunk commChunk = soundchunk1.getSubChunk("COMM".getBytes());
      assertNotNull(commChunk);
      MemoryReadAccess sound1mem = commChunk.getMemoryAccess();
      
      // sample rate
      int ratebits = (int) sound1mem.readUnsigned32(16);
      float rate = Float.intBitsToFloat(ratebits);
      assertEquals(2.1967278f, rate);
      */
      BlorbResources resources = new BlorbResources(formchunk);
      SoundEffect sound = resources.getSounds().getResource(17);
      sound.play(1, 255);
      Thread.sleep(6000);
      
    } finally {
      
      if (rndfile != null) rndfile.close();
    }
  }

  public void testJourneyBlorb() throws Exception {

    File file = new File("testfiles/Journey.blb");
    RandomAccessFile rndfile = null;
    
    try {
      
      rndfile = new RandomAccessFile(file, "r");
      byte[] data = new byte[(int) rndfile.length()];
      rndfile.readFully(data);
      Memory memory = new DefaultMemory(data);
      FormChunk formchunk = new DefaultFormChunk(memory);
      assertEquals("IFRS", new String(formchunk.getSubId()));

      // Ridx chunk
      Chunk ridxChunk = formchunk.getSubChunk("RIdx".getBytes());
      assertNotNull(ridxChunk);
      Memory chunkmem = ridxChunk.getMemory();
      int offset = 8;
      int numresources = (int) chunkmem.readUnsigned32(offset);
      assertEquals(134, numresources);
      
      // Usage            
      byte[] usage = new byte[4];
      for (int i = 0; i < 4; i++) usage[i] = chunkmem.readByte(offset + 4 + i);
      assertEquals("Pict", new String(usage));

      // Number of resource, pictures usually start at 1
      int resnum = (int) chunkmem.readUnsigned32(offset + 8);
      assertEquals(1, resnum);
      
      // Start of resource
      int resstart = (int) chunkmem.readUnsigned32(offset + 12);
      assertEquals(1806, resstart);      
      
    } finally {
      
      if (rndfile != null) rndfile.close();
    }
  }
}
