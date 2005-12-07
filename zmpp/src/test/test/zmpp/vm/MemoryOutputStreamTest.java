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
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MemoryOutputStream;

public class MemoryOutputStreamTest extends MockObjectTestCase {

  private Mock mockMachine;
  private Machine machine;
  private Mock mockMemAccess;
  private MemoryAccess memaccess;
  private MemoryOutputStream output;
  
  protected void setUp() throws Exception {

    mockMachine = mock(Machine.class);
    machine = (Machine) mockMachine.proxy();
    mockMemAccess = mock(MemoryAccess.class);
    memaccess = (MemoryAccess) mockMemAccess.proxy();

    mockMachine.expects(once()).method("getMemoryAccess").will(returnValue(memaccess));
    
    output = new MemoryOutputStream(machine);
  }
  
  public void testPrint() {
    
    mockMemAccess.expects(once()).method("writeUnsignedByte").with(eq(4713), eq((short)65));
    mockMemAccess.expects(once()).method("writeUnsignedShort").with(eq(4711), eq(1));
    
    // Selection has to be performed prior to printing - ALWAYS !!!
    output.select(4711);    
    output.print((short) 65);
    output.select(false);
  }
  
  public void testIsSelected() {
    
    output.select(4711);
    assertTrue(output.isSelected());
  }
  
  public void testUnusedMethods() {
    
    output.flush();
    output.close();
  }
  
  public void testSelectMaxNesting() {
    
    mockMachine.expects(once()).method("halt").with(eq("maximum nesting depth (16) for stream 3 exceeded"));
    for (int i = 0; i < 17; i++) {
      
      output.select(4710 + 10 * i);
    }
  }
  
}
