/*
 * $Id$
 * 
 * Created on 2005/12/06
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
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.GameData;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MemoryOutputStream;
import org.zmpp.vm.StoryFileHeader;

public class MemoryOutputStreamTest extends MockObjectTestCase {

  private Mock mockMemory, mockMachine, mockGameData, mockCpu, mockFileheader;
  private Machine machine;
  private Memory memory;
  private MemoryOutputStream output;
  private GameData gamedata;
  private Cpu cpu;
  private StoryFileHeader fileheader;
  
  protected void setUp() throws Exception {
    mockMachine = mock(Machine.class);
    mockMemory = mock(Memory.class);
    mockGameData = mock(GameData.class);
    mockCpu = mock(Cpu.class);
    mockFileheader = mock(StoryFileHeader.class);
    
    machine = (Machine) mockMachine.proxy();
    memory = (Memory) mockMemory.proxy();
    gamedata = (GameData) mockGameData.proxy();
    cpu = (Cpu) mockCpu.proxy();
    fileheader = (StoryFileHeader) mockFileheader.proxy();

    output = new MemoryOutputStream(machine);
  }
  
  public void testPrintVersion5() {
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(fileheader));
    mockFileheader.expects(once()).method("getVersion").will(returnValue(5));
    
    mockMemory.expects(once()).method("writeUnsignedByte").with(eq(4713), eq((short)65));
    mockMemory.expects(once()).method("writeUnsignedShort").with(eq(4711), eq(1));
    
    // Selection has to be performed prior to printing - ALWAYS !!!
    output.select(4711, 0);    
    output.print((short) 65, false);
    output.select(false);
  }
  
  public void testIsSelected() {
    output.select(4711, 0);
    assertTrue(output.isSelected());
  }
  
  public void testUnusedMethods() {
    output.flush();
    output.close();
  }
  
  public void testSelectMaxNesting() {
    mockMachine.expects(once()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq("maximum nesting depth (16) for stream 3 exceeded"));
    for (int i = 0; i < 17; i++) {
      
      output.select(4710 + 10 * i, 0);
    }
  }
  
}
