/*
 * Created on 10/04/2005
 * Copyright (c) 2005-2009, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package test.zmpp.vm;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.vm.ModernObjectTree;
import org.zmpp.vm.ObjectTree;
import org.zmpp.base.StoryFileHeader;

/**
 * Testing the tree for Curses.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class CursesObjectTreeTest extends CursesSetup {

	private static final int ADDR_7_20 	= 7734;
	private static final int ADDR_7_1 	= 7741;
	
  private ObjectTree objectTree;

  @Before
  @Override
  public void setUp() throws Exception {
	  super.setUp();
	  objectTree = new ModernObjectTree(curses,
      machine.readUnsigned16(StoryFileHeader.OBJECT_TABLE));
  }
  
  @Test
  public void testGetPropertiesDescriptionAddress() {
	  assertEquals(0x2d40, objectTree.getPropertiesDescriptionAddress(123));
  }
  
  @Test
  public void testGetPropertyAddress() {
  	assertEquals(ADDR_7_20, objectTree.getPropertyAddress(7, 20));
  	assertEquals(ADDR_7_1, objectTree.getPropertyAddress(7, 1));
  }

  @Test
  public void testGetProperty() {
	  assertEquals(0, objectTree.getProperty(3, 22));
	  assertEquals(0x0006, objectTree.getProperty(3, 8));
	  assertEquals(0xb685, objectTree.getProperty(2, 20));
  }
  
  @Test
  public void testSetGetProperty() {
	  objectTree.setProperty(122, 34, (char) 0xdefe);
	  assertEquals(0xdefe, objectTree.getProperty(122, 34));
  }
  
  @Test
  public void testGetNextProperty() {
  	assertEquals(24, objectTree.getNextProperty(7, 0));
  	assertEquals(20, objectTree.getNextProperty(7, 24));
  	assertEquals(8, objectTree.getNextProperty(7, 20));
  	assertEquals(1, objectTree.getNextProperty(7, 8));
  	assertEquals(0, objectTree.getNextProperty(7, 1));
  }

  @Test
  public void testGetPropertyLength() {
	  assertEquals(2, objectTree.getPropertyLength(ADDR_7_20));
	  assertEquals(6, objectTree.getPropertyLength(ADDR_7_1));
  }
  
  @Test
  public void testGetPropertyLengthAddress0() {
	  assertEquals(0, objectTree.getPropertyLength(0));
  }
}
