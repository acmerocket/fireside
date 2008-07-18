/*
 * $Id$
 * 
 * Created on 2005/12/06
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
  Mockery context = new JUnit4Mockery();  
  private Memory memory;
  private MemorySection section;

  @Before
  public void setUp() throws Exception {
    memory = context.mock(Memory.class);
    section = new MemorySection(memory, 36, 256);
  }

  @Test
  public void testGetLength() {
    assertEquals(256, section.getLength());
  }

  @Test
  public void testWriteUnsignedShort() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedShort(12 + 36, 512);
    }});
    section.writeUnsignedShort(12, 512);
  }

  @Test
  public void testWriteShort() {
    context.checking(new Expectations() {{
      one (memory).writeShort(12 + 36, (short) 512);
    }});
    section.writeShort(12, (short) 512);
  }

  @Test
  public void testWriteUnsignedByte() {
    context.checking(new Expectations() {{
      one (memory).writeUnsignedByte(12 + 36, (short) 120);
    }});
    section.writeUnsignedByte(12, (short) 120);
  }
  
  @Test
  public void testWriteByte() {
    context.checking(new Expectations() {{
      one (memory).writeByte(12 + 36, (byte) -120);
    }});
    section.writeByte(12, (byte) -120);
  }

  @Test
  public void testWriteUnsigned32() {
   context.checking(new Expectations() {{
      one (memory).writeUnsigned32(16 + 36, (long) 1120);
    }});
    section.writeUnsigned32(16, (long) 1120);
  }
}
