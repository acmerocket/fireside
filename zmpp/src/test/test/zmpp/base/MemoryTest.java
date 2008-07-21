/*
 * $Id: MemoryAccessTest.java 520 2007-11-13 19:14:51Z weiju $
 * 
 * Created on 2005/09/23
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

import org.junit.Before;
import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import static org.junit.Assert.*;

/**
 * This class is a test for the Memory class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemoryTest {

  private Memory memory;
  private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };
  
  @Before
  public void setUp() throws Exception {
    memory = new DefaultMemory(data);
  }
  
  @Test
  public void testReadUnsignedByte() {
    assertEquals(3, memory.readUnsigned8(0x00));
  }
  
  @Test
  public void testReadUnsignedWord() {
    assertEquals(0x3709, memory.readUnsigned16(0x02));
  }
  
  @Test
  public void testGetUnsignedShortGeneral() {
    assertEquals(0xffff, memory.readUnsigned16(0x04));
    assertNotSame(-1, memory.readUnsigned16(0x04));
  }
  
  @Test
  public void testGetShortGeneral() {
    assertEquals(-1, memory.readSigned16(0x04));
  }
  
  @Test
  public void testWriteUnsignedByte() {
    memory.writeUnsigned8(0x02, (short) 0xff);
    assertEquals(0xff, memory.readUnsigned8(0x02));
    
    memory.writeUnsigned8(0x03, (short) 0x32);
    assertEquals(0x32, memory.readUnsigned8(0x03));
  }
  
  @Test
  public void testWriteUnsignedShort() {
    memory.writeUnsigned16(0x02, (char) 0xffff);
    assertEquals(0xffff, memory.readUnsigned16(0x02));    
    memory.writeUnsigned16(0x04, (char) 0x00ff);
    assertEquals(0x00ff, memory.readUnsigned16(0x04));
  }
  
  @Test
  public void testCopyBytesToArray() {
    byte[] dstData = new byte[4];
    int dstOffset = 1;
    int srcOffset = 2;
    int numBytes = 3;
    memory.copyBytesToArray(dstData, dstOffset, srcOffset, numBytes);
    assertEquals(0x37, dstData[1]);
    assertEquals(0x09, dstData[2]);
    assertEquals((byte) 0xff, dstData[3]);
  }

  @Test
  public void testCopyBytesFromArray() {
    byte[] srcData = { (byte) 0x00, (byte) 0xef, (byte )0x10, (byte) 0xfe };
    int srcOffset = 1;
    int dstOffset = 0;
    int numBytes = 3;
    memory.copyBytesFromArray(srcData, srcOffset, dstOffset, numBytes);
    assertEquals(0xef, memory.readUnsigned8(0));
    assertEquals(0x10, memory.readUnsigned8(1));
    assertEquals(0xfe, memory.readUnsigned8(2));
  }

  @Test
  public void testCopyBytesFromMemory() {
    byte[] dstData = { (byte) 0x00, (byte) 0x00, (byte )0x00, (byte) 0x00 };
    byte[] srcData = { (byte) 0x00, (byte) 0xef, (byte )0x10, (byte) 0xfe };
    Memory srcMem = new DefaultMemory(srcData);
    Memory dstMem = new DefaultMemory(dstData);
    int srcOffset = 1;
    int dstOffset = 0;
    int numBytes = 3;
    dstMem.copyBytesFromMemory(srcMem, srcOffset, dstOffset, numBytes);
    assertEquals(0xef, dstMem.readUnsigned8(0));
    assertEquals(0x10, dstMem.readUnsigned8(1));
    assertEquals(0xfe, dstMem.readUnsigned8(2));
  }
  
  @Test
  public void testCopyArea() {
    memory.copyArea(0, 2, 3);
    assertEquals(0x03, memory.readUnsigned8(0));
    assertEquals(0x00, memory.readUnsigned8(1));
    assertEquals(0x03, memory.readUnsigned8(2));
    assertEquals(0x00, memory.readUnsigned8(3));
    assertEquals(0x37, memory.readUnsigned8(4));
  }
}
