/*
 * Created on 2008/07/20
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