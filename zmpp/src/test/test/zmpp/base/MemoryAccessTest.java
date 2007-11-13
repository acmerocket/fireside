/*
 * $Id$
 * 
 * Created on 2005/09/23
 * Copyright 2005-2007 by Wei-ju Wu
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

import org.zmpp.base.DefaultMemoryAccess;
import org.zmpp.base.MemoryAccess;

/**
 * This class is a test for the MemoryMap class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class MemoryAccessTest extends TestCase {

  private MemoryAccess memaccess;
  private byte[] data = { 0x03, 0x00, 0x37, 0x09, (byte) 0xff, (byte) 0xff };
  
  protected void setUp() throws Exception {
    
    memaccess = new DefaultMemoryAccess(data);
  }
    
  public void testReadUnsignedByte() {
    
    assertEquals(3, memaccess.readUnsignedByte(0x00));
  }
  
  public void testReadUnsignedWord() {
    
    assertEquals(0x3709, memaccess.readUnsignedShort(0x02));
  }
  
  public void testGetUnsignedShortGeneral() {
    
    assertEquals(0xffff, memaccess.readUnsignedShort(0x04));
    assertNotSame(-1, memaccess.readUnsignedShort(0x04));
  }
  
  public void testGetShortGeneral() {
    
    assertEquals(-1, memaccess.readShort(0x04));
  }
  
  public void testReadUnsigned32(){
    byte[] data32 = { (byte) 0xd7, (byte) 0x4b, (byte) 0xd7, (byte) 0x53 };
    MemoryAccess memaccess = new DefaultMemoryAccess(data32);
    assertEquals(0xd74bd753, memaccess.readUnsigned32(0x00));
  }
  
  public void testWriteUnsignedByte() {
    
    memaccess.writeUnsignedByte(0x02, (short) 0xff);
    assertEquals(0xff, memaccess.readUnsignedByte(0x02));
    
    memaccess.writeUnsignedByte(0x03, (short) 0x32);
    assertEquals(0x32, memaccess.readUnsignedByte(0x03));
  }
  
  public void testWriteUnsignedShort() {
    
    memaccess.writeUnsignedShort(0x02, 0xffff);
    assertEquals(0xffff, memaccess.readUnsignedShort(0x02));
    
    memaccess.writeUnsignedShort(0x04, 0x00ff);
    assertEquals(0x00ff, memaccess.readUnsignedShort(0x04));
  }
  
  public void testWriteUnsigned32() {
    
    memaccess.writeUnsigned32(0x00, 0xffffffff);
    assertEquals(0x00000000ffffffff, memaccess.readUnsigned32(0x00));
    
    memaccess.writeUnsigned32(0x00, 0xf0f00f0f);
    assertEquals(0x00000000f0f00f0f, memaccess.readUnsigned32(0x00));
  }
  
  public void testWriteUnsigned48() {
    
    memaccess.writeUnsigned48(0, 12345678l);
    assertEquals(12345678l, memaccess.readUnsigned48(0));
  }
}
