/*
 * Created on 2005/12/06
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
package test.zmpp.base;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zmpp.base.Memory;
import org.zmpp.base.MemorySection;
import static org.junit.Assert.*;

/**
 * Test of MemorySection class.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class MemorySectionTest {
  private static final int OFFSET = 36;
  Mockery context = new JUnit4Mockery();  
  private Memory memory;
  private MemorySection section;

  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    section = new MemorySection(memory, OFFSET, 256);
  }

  @Test
  public void testGetLength() {
    assertEquals(256, section.getLength());
  }

  @Test
  public void testWriteUnsignedShort() {
    context.checking(new Expectations() {{
      one (memory).writeUnsigned16(12 + 36, (char) 512);
    }});
    section.writeUnsigned16(12, (char) 512);
  }

  @Test
  public void testWriteUnsignedByte() {
    context.checking(new Expectations() {{
      one (memory).writeUnsigned8(12 + 36, (char) 120);
    }});
    section.writeUnsigned8(12, (char) 120);
  }
  
  @Test
  public void testCopyBytesToArray() {
    final byte[] dstData = new byte[5];
    final int dstOffset = 2;
    final int srcOffset = 3;
    final int numBytes = 23;
    context.checking(new Expectations() {{
      one (memory).copyBytesToArray(dstData, dstOffset, OFFSET + srcOffset, numBytes);
    }});
    section.copyBytesToArray(dstData, dstOffset, srcOffset, numBytes);
  }

  @Test
  public void testCopyBytesFromArray() {
    final byte[] srcData = new byte[5];
    final int srcOffset = 2;
    final int dstOffset = 3;
    final int numBytes = 23;
    context.checking(new Expectations() {{
      one (memory).copyBytesFromArray(srcData, srcOffset, OFFSET + dstOffset, numBytes);
    }});
    section.copyBytesFromArray(srcData, srcOffset, dstOffset, numBytes);
  }

  @Test
  public void testCopyBytesFromMemory() {
    final Memory srcMem = context.mock(Memory.class, "srcMem");
    final int srcOffset = 2;
    final int dstOffset = 3;
    final int numBytes = 5;
    context.checking(new Expectations() {{
      one (memory).copyBytesFromMemory(srcMem, srcOffset, OFFSET + dstOffset,
                                       numBytes);
    }});
    section.copyBytesFromMemory(srcMem, srcOffset, dstOffset, numBytes);
  }

  @Test
  public void testCopyArea() {
    final int src = 1, dst = 2, numBytes = 10;
    context.checking(new Expectations() {{
      one (memory).copyArea(OFFSET + src, OFFSET + dst, numBytes);
    }});
    section.copyArea(src, dst, numBytes);
  }
}
