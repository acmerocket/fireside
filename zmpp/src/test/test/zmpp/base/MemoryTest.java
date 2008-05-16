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

import junit.framework.TestCase;

import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;

/**
 * This class is a test for the Memory class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MemoryTest extends TestCase {

  private Memory memory;
  private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };
  
  @Override
  protected void setUp() throws Exception {
    memory = new DefaultMemory(data);
  }
    
  public void testReadUnsignedByte() {
    assertEquals(3, memory.readUnsignedByte(0x00));
  }
  
  public void testReadUnsignedWord() {
    assertEquals(0x3709, memory.readUnsignedShort(0x02));
  }
  
  public void testGetUnsignedShortGeneral() {
    assertEquals(0xffff, memory.readUnsignedShort(0x04));
    assertNotSame(-1, memory.readUnsignedShort(0x04));
  }
  
  public void testGetShortGeneral() {
    assertEquals(-1, memory.readShort(0x04));
  }
  
  public void testReadUnsigned32(){
    byte[] data32 = { (byte) 0xd7, (byte) 0x4b, (byte) 0xd7, (byte) 0x53 };
    Memory memaccess = new DefaultMemory(data32);
    assertEquals(0xd74bd753L, memaccess.readUnsigned32(0x00));
  }
  
  public void testWriteUnsignedByte() {
    memory.writeUnsignedByte(0x02, (short) 0xff);
    assertEquals(0xff, memory.readUnsignedByte(0x02));
    
    memory.writeUnsignedByte(0x03, (short) 0x32);
    assertEquals(0x32, memory.readUnsignedByte(0x03));
  }
  
  public void testWriteUnsignedShort() {
    memory.writeUnsignedShort(0x02, 0xffff);
    assertEquals(0xffff, memory.readUnsignedShort(0x02));
    
    memory.writeUnsignedShort(0x04, 0x00ff);
    assertEquals(0x00ff, memory.readUnsignedShort(0x04));
  }
  
  public void testWriteUnsigned32() {
    memory.writeUnsigned32(0x00, 0xffffffffL);
    assertEquals(0x00000000ffffffffL, memory.readUnsigned32(0x00));
    
    memory.writeUnsigned32(0x00, 0xf0f00f0fL);
    assertEquals(0x00000000f0f00f0fL, memory.readUnsigned32(0x00));
  }  
}
