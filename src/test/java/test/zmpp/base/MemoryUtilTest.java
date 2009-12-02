/*
 * Created on 2008/07/19
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

import org.junit.Before;
import org.junit.Test;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.base.MemoryUtil;
import static org.junit.Assert.*;

/**
 * Test class for MemoryUtil.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MemoryUtilTest {

  private Memory memory;
  private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };
  
  @Before
  public void setUp() throws Exception {
    memory = new DefaultMemory(data);
  }  
  
  @Test
  public void testToUnsigned16() {
    assertEquals(1234, MemoryUtil.toUnsigned16(1234));
  }

  @Test
  public void testReadUnsigned32(){
    byte[] data32 = { (byte) 0xd7, (byte) 0x4b, (byte) 0xd7, (byte) 0x53 };
    Memory memaccess = new DefaultMemory(data32);
    assertEquals(0xd74bd753L, MemoryUtil.readUnsigned32(memaccess, 0x00));
  }
  
  @Test
  public void testWriteUnsigned32() {
    MemoryUtil.writeUnsigned32(memory, 0x00, 0xffffffffL);
    assertEquals(0x00000000ffffffffL, MemoryUtil.readUnsigned32(memory, 0x00));
    
    MemoryUtil.writeUnsigned32(memory, 0x00, 0xf0f00f0fL);
    assertEquals(0x00000000f0f00f0fL, MemoryUtil.readUnsigned32(memory, 0x00));
  }
 
  @Test
  public void testSignedToUnsigned16() {
    assertEquals(0, MemoryUtil.signedToUnsigned16((short) 0));
    assertEquals((char) 0xffff, MemoryUtil.signedToUnsigned16((short) -1));
    assertEquals((char) 0xfffe, MemoryUtil.signedToUnsigned16((short) -2));
    assertEquals((char) 32767, MemoryUtil.signedToUnsigned16((short) 32767));
    assertEquals((char) 32768, MemoryUtil.signedToUnsigned16((short) -32768));
  }

  @Test
  public void testUnsignedToSigned16() {
    assertEquals(0, MemoryUtil.unsignedToSigned16((char) 0));
    assertEquals(1, MemoryUtil.unsignedToSigned16((char) 1));
    assertEquals(-32768, MemoryUtil.unsignedToSigned16((char) 32768));
    assertEquals(32767, MemoryUtil.unsignedToSigned16((char) 32767));
    assertEquals(-1, MemoryUtil.unsignedToSigned16((char) 65535));
  }

  @Test
  public void testUnsignedToSigned8() {
    assertEquals(0, MemoryUtil.unsignedToSigned8((char) 0));
    assertEquals(1, MemoryUtil.unsignedToSigned8((char) 1));
    assertEquals(-128, MemoryUtil.unsignedToSigned8((char) 128));
    assertEquals(127, MemoryUtil.unsignedToSigned8((char) 127));
    assertEquals(-1, MemoryUtil.unsignedToSigned8((char) 0xff));
    assertEquals(-1, MemoryUtil.unsignedToSigned8((char) 0x10ff));
  }
}