/*
 * $Id$
 * 
 * Created on 2005/12/06
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package test.zmpp.base;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryAccess;
import org.zmpp.base.MemorySection;

public class MemorySectionTest extends MockObjectTestCase {

  private Mock mockMemaccess;
  private MemoryAccess memaccess;
  private MemorySection section;
  
  protected void setUp() throws Exception {

    mockMemaccess = mock(MemoryAccess.class);
    memaccess = (MemoryAccess) mockMemaccess.proxy();
    section = new MemorySection(memaccess, 36, 256);
  }
  
  public void testGetLength() {
    
    assertEquals(256, section.getLength());
  }
  
  public void testReadUnsigned48() {
    
    mockMemaccess.expects(once()).method("readUnsigned48").with(eq(12 + 36)).will(returnValue((long) 1234));
    section.readUnsigned48(12);
  }

  public void testWriteUnsigned48() {
    
    mockMemaccess.expects(once()).method("writeUnsigned48").with(eq(12 + 36), eq((long) 512));
    section.writeUnsigned48(12, 512);
  }
  
  public void testWriteUnsignedShort() {
    
    mockMemaccess.expects(once()).method("writeUnsignedShort").with(eq(12 + 36), eq(512));
    section.writeUnsignedShort(12, 512);
  }

  public void testWriteShort() {
    
    mockMemaccess.expects(once()).method("writeShort").with(eq(12 + 36), eq((short) 512));
    section.writeShort(12, (short) 512);
  }

  public void testWriteUnsignedByte() {
    
    mockMemaccess.expects(once()).method("writeUnsignedByte").with(eq(12 + 36), eq((short) 120));
    section.writeUnsignedByte(12, (short) 120);
  }
  
  public void testWriteByte() {
    
    mockMemaccess.expects(once()).method("writeByte").with(eq(12 + 36), eq((byte) -120));
    section.writeByte(12, (byte) -120);
  }
  
  public void testWriteUnsigned32() {
    
    mockMemaccess.expects(once()).method("writeUnsigned32").with(eq(16 + 36), eq((long) 1120));
    section.writeUnsigned32(16, (long) 1120);
  }
}