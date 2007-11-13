/*
 * $Id$
 *
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
package test.zmpp.instructions;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryAccess;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.GameData;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.Output;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.ZObject;

public abstract class InstructionTestBase extends MockObjectTestCase {

  protected Mock mockMachine;
  protected Machine machine;
  protected Mock mockGameData;
  protected GameData gamedata;
  protected Mock mockOutputStream;
  protected OutputStream outputStream;
  protected Mock mockObjectTree;
  protected ObjectTree objectTree;
  protected Mock mockZObject;
  protected ZObject zobject;
  protected Mock mockMemAccess;
  protected MemoryAccess memoryAccess;
  protected Mock mockFileHeader;
  protected StoryFileHeader storyfileHeader;
  protected Mock mockDictionary;
  protected Dictionary dictionary;
  protected Mock mockCpu;
  protected Cpu cpu;
  protected Mock mockOutput;
  protected Output output;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    
    super.setUp();
  
    mockMachine = mock(Machine.class);
    machine = (Machine) mockMachine.proxy();
    mockGameData = mock(GameData.class);
    gamedata = (GameData) mockGameData.proxy();
    mockOutputStream = mock(OutputStream.class);
    outputStream = (OutputStream) mockOutputStream.proxy();    
    mockObjectTree = mock(ObjectTree.class);
    objectTree = (ObjectTree) mockObjectTree.proxy();
    mockZObject = mock(ZObject.class);
    zobject = (ZObject) mockZObject.proxy();
    mockMemAccess = mock(MemoryAccess.class);
    memoryAccess = (MemoryAccess) mockMemAccess.proxy();
    mockFileHeader = mock(StoryFileHeader.class);
    storyfileHeader = (StoryFileHeader) mockFileHeader.proxy();
    mockDictionary = mock(Dictionary.class);
    dictionary = (Dictionary) mockDictionary.proxy();    
    mockCpu = mock(Cpu.class);
    cpu = (Cpu) mockCpu.proxy();
    mockOutput = mock(Output.class);
    output = (Output) mockOutput.proxy();
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
  }
}
