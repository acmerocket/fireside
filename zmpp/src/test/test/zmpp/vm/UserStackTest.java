/*
 * $Id$
 * 
 * Created on 05/11/2006
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
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.CpuImpl;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;
import org.zmpp.vm.StoryFileHeader;

/**
 * This class tests the access method to the user stack.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class UserStackTest extends MockObjectTestCase {

  private Mock mockMachine, mockDecoder, mockFileHeader;
  private Machine machine;
  private InstructionDecoder decoder;
  private Cpu cpu;
  private StoryFileHeader fileheader;
  private Memory memory;
  
  // A stack with three words, but only two slots
  private byte[] stackdata = {  0x00, 0x02, 0x00, 0x00, 0x00, 0x00 };
  
  @Override
  public void setUp() throws Exception {
    mockMachine = mock(Machine.class);
    mockDecoder = mock(InstructionDecoder.class);
    mockFileHeader = mock(StoryFileHeader.class);
    
    machine = (Machine) mockMachine.proxy();
    decoder = (InstructionDecoder) mockDecoder.proxy();
    fileheader = (StoryFileHeader) mockFileHeader.proxy();
    memory = new DefaultMemory(stackdata);
    
    mockMachine.expects(atLeastOnce()).method("getFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getProgramStart").will(returnValue(1000));
    mockFileHeader.expects(once()).method("getGlobalsAddress").will(returnValue(5000));
    mockMachine.expects(once()).method("getVersion").will(returnValue(5));
    mockMachine.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));
    mockDecoder.expects(once()).method("initialize").with(eq(machine));
    cpu = new CpuImpl(machine, decoder);
    cpu.reset();
  }
  
  public void testPushStack() {
    assertTrue(cpu.pushUserStack(0, (short) 12));
    assertEquals("stack should contain only one slot now", 1, memory.readUnsignedShort(0));
    assertEquals("value 12 should be in the second slot", 12, memory.readShort(4));
  }

  public void testPushStackOverflowError() {
    assertTrue(cpu.pushUserStack(0, (short) 12));
    assertTrue(cpu.pushUserStack(0, (short) 12));
    assertFalse(cpu.pushUserStack(0, (short) 12));
  }
  
  public void testPopStack() {
    cpu.pushUserStack(0, (short) 12);
    cpu.pushUserStack(0, (short) 13);
    assertEquals("first word on stack should be 13", 13, cpu.popUserStack(0));
    assertEquals("second word on stack should be 12", 12, cpu.popUserStack(0));    
  }
}
