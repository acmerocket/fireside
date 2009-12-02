/*
 * Created on 2008/07/20
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

import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.base.MemoryUtil;
import static org.junit.Assert.*;
import org.zmpp.iff.Chunk;
import org.zmpp.iff.DefaultChunk;

/**
 * Test class for DefaultChunk.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class DefaultChunkTest {

  @Test
  public void testCreateChunkForWriting() {
    byte[] id = {(byte) 'F', (byte) 'O', (byte) 'R', (byte) 'M' };
    byte[] chunkdata = { (byte) 0x01, (byte) 0x02, (byte) 0x03 };
    Chunk chunk = new DefaultChunk(id, chunkdata);
    assertEquals(3, chunk.getSize());
    assertEquals("FORM", chunk.getId());
    assertEquals(0, chunk.getAddress());
    Memory mem = chunk.getMemory();
    assertEquals('F', mem.readUnsigned8(0));
    assertEquals('O', mem.readUnsigned8(1));
    assertEquals('R', mem.readUnsigned8(2));
    assertEquals('M', mem.readUnsigned8(3));
    assertEquals(3, MemoryUtil.readUnsigned32(mem, 4));
    assertEquals(0x01, mem.readUnsigned8(8));
    assertEquals(0x02, mem.readUnsigned8(9));
    assertEquals(0x03, mem.readUnsigned8(10));
  }
  
  @Test
  public void testCreateChunkForReading() {
    byte[] data = {
      (byte) 'F', (byte) 'O', (byte) 'R', (byte) 'M',
      (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03,
      (byte) 0x01, (byte) 0x02, (byte) 0x03
    };
    Memory mem = new DefaultMemory(data);
    Chunk chunk = new DefaultChunk(mem, 1234);
    assertEquals(1234, chunk.getAddress());
    assertEquals("FORM", chunk.getId());
    assertSame(mem, chunk.getMemory());
    assertEquals(3, chunk.getSize());
  }
}