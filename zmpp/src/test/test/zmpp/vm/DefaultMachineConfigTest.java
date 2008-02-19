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
package test.zmpp.vm;

import java.io.File;
import java.io.FileInputStream;

import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.GameDataImpl;
import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vmutil.FileUtils;

public class DefaultMachineConfigTest extends MockObjectTestCase {

  private java.io.InputStream input;
  private byte[] data;
  
  protected void setUp() throws Exception {
    
    File zork1 = new File("testfiles/minizork.z3");
    input = new FileInputStream(zork1);
    data = FileUtils.readFileBytes(input);
  }
  
  protected void tearDown() throws Exception {
    
    input.close();
  }
  
  public void testCreate() throws Exception {
    
    GameDataImpl config = new GameDataImpl(data, null);
    StoryFileHeader fileheader = config.getStoryFileHeader();
    Dictionary dictionary = config.getDictionary();
    Memory memory = config.getMemory();
    ObjectTree objectTree = config.getObjectTree();
    
    assertNotNull(memory);
    assertNotNull(dictionary);
    assertNotNull(fileheader);
    assertNotNull(objectTree);
    
    config.reset();
    
    assertNotSame(fileheader, config.getStoryFileHeader());
    assertNotSame(dictionary, config.getDictionary());
    assertNotSame(memory, config.getMemory());
    assertNotSame(objectTree, config.getObjectTree());
  }  
}
