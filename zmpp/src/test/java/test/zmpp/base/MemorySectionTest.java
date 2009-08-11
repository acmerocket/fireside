/*
 * Created on 2005/12/06
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
