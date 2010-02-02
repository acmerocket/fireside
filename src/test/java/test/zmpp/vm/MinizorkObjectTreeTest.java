/*
 * Created on 10/14/2005
 * Copyright (c) 2005-2010, Wei-ju Wu.
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
import org.zmpp.vm.ClassicObjectTree;
import org.zmpp.vm.ObjectTree;
import org.zmpp.base.StoryFileHeader;

/**
 * Testing tree access with concrete data of Version 3 - Minizork.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class MinizorkObjectTreeTest extends MiniZorkSetup {

  private ObjectTree objectTree;
  private static final int OBJECT1 = 1; 
  private static final int OBJECT2 = 2; 
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.objectTree = new ClassicObjectTree(minizorkmap,
        machine.readUnsigned16(StoryFileHeader.OBJECT_TABLE));
  }
  
  @Test
  public void testObjectSetters() {
    objectTree.setParent(OBJECT1, 38);
    assertEquals(38, objectTree.getParent(OBJECT1));
    objectTree.setChild(OBJECT1, 39);
    assertEquals(39, objectTree.getChild(OBJECT1));
    objectTree.setSibling(OBJECT1, 42);
    assertEquals(42, objectTree.getSibling(OBJECT1));
  }
  
  @Test
  public void testMinizorkAttributes() { 
    assertFalse(objectTree.isAttributeSet(OBJECT1, 5));
    assertTrue(objectTree.isAttributeSet(OBJECT1, 6));
    assertFalse(objectTree.isAttributeSet(OBJECT1, 7));
    
    assertTrue(objectTree.isAttributeSet(OBJECT2, 5));
    assertTrue(objectTree.isAttributeSet(OBJECT2, 7));
    assertTrue(objectTree.isAttributeSet(OBJECT2, 19));
    objectTree.clearAttribute(OBJECT2, 19);
    assertFalse(objectTree.isAttributeSet(OBJECT2, 19));
  }
  
  @Test
  public void testSetAttributes() {
    assertFalse(objectTree.isAttributeSet(OBJECT1, 5));
    objectTree.setAttribute(OBJECT1, 5);
    assertTrue(objectTree.isAttributeSet(OBJECT1, 5));
  }
  
  @Test
  public void testClearAttributes() {  
    // Set several bits in a row to make sure there will be no arithmetical
    // shift errors
    objectTree.setAttribute(OBJECT1, 0);
    objectTree.setAttribute(OBJECT1, 1);
    objectTree.setAttribute(OBJECT1, 2);
    objectTree.setAttribute(OBJECT1, 3);
    
    assertTrue(objectTree.isAttributeSet(OBJECT1, 2));
    objectTree.clearAttribute(OBJECT1, 2);
    
    assertTrue(objectTree.isAttributeSet(OBJECT1, 0));
    assertTrue(objectTree.isAttributeSet(OBJECT1, 1));
    assertFalse(objectTree.isAttributeSet(OBJECT1, 2));
    assertTrue(objectTree.isAttributeSet(OBJECT1, 3));    
  }
    
  @Test
  public void testGetPropertiesDescriptionAddress() {
    int propaddress = objectTree.getPropertiesDescriptionAddress(OBJECT1);
    assertEquals("forest",
    		converter.decode2Zscii(minizorkmap, propaddress, 0).toString());
  }

  @Test
  public void testGetPropertyAddress() {
  	assertEquals(2645, objectTree.getPropertyAddress(OBJECT1, 18));
  	assertEquals(2648, objectTree.getPropertyAddress(OBJECT1, 17));	  
  	assertEquals(0, objectTree.getPropertyAddress(OBJECT1, 15));	  
  }

  @Test
  public void testGetProperty() {
  	assertEquals(0x77, objectTree.getProperty(OBJECT2, 22));
  	assertEquals(0xc6c5, objectTree.getProperty(8, 16));
  	// not defined, get default
  	assertEquals(0, objectTree.getProperty(OBJECT1, 20));
  }
  
  @Test
  public void testSetGetProperty() {
  	objectTree.setProperty(OBJECT2, 22, (char) 0xc5);
  	objectTree.setProperty(8, 16, (char) 0xcafe);
  	assertEquals(0xc5, objectTree.getProperty(OBJECT2, 22));
  	assertEquals(0xcafe, objectTree.getProperty(8, 16));
  }
  
  @Test
  public void testGetNextProperty() {
    assertEquals(18, objectTree.getNextProperty(OBJECT1, 0));
    assertEquals(17, objectTree.getNextProperty(OBJECT1, 18));
    assertEquals(0, objectTree.getNextProperty(OBJECT1, 17));
  }

  @Test
  public void testGetObject() {
	  assertEquals(36, objectTree.getParent(1));
	  assertEquals(147, objectTree.getSibling(1));
	  assertEquals(0, objectTree.getChild(1));
  }

  @Test
  public void testRemoveObjectFirstChild() {
	  // remove a thief's lair - object 170
	  int thiefslair = 170;
	  assertEquals(27, objectTree.getParent(thiefslair));
	  assertEquals(175, objectTree.getChild(thiefslair));
	  assertEquals(56, objectTree.getSibling(thiefslair));

	  objectTree.removeObject(thiefslair);

	  // parent needs to be 0
	  assertEquals(0, objectTree.getParent(thiefslair));

	  // the old parent needs to point to the next child
	  assertEquals(56, objectTree.getChild(27));
  }

  @Test
  public void testRemoveObjectNotFirstChild() {
	  // remove a cyclops room - object 56
	  int cyclopsroom = 56;
	  assertEquals(27, objectTree.getParent(cyclopsroom));
	  assertEquals(137, objectTree.getChild(cyclopsroom));
	  assertEquals(154, objectTree.getSibling(cyclopsroom));

	  objectTree.removeObject(cyclopsroom);

	  // parent needs to be 0
	  assertEquals(0, objectTree.getParent(cyclopsroom));

	  // the old parent does not need to change its child, but the
	  // sibling chain needs to be corrected, so after 170 there will
	  // follow 154 instead of 56
	  assertEquals(170, objectTree.getChild(27));
	  assertEquals(154, objectTree.getSibling(170));
  }

  @Test
  public void testRemoveObjectNotFirstButLastChild() {
	  // remove a burnt out lantern - object 62
	  int lantern = 62;
	  assertEquals(157, objectTree.getParent(lantern));
	  assertEquals(0, objectTree.getChild(lantern));
	  assertEquals(0, objectTree.getSibling(lantern));

	  objectTree.removeObject(lantern);

	  // parent needs to be 0
	  assertEquals(0, objectTree.getParent(lantern));

	  // the old parent does not need to change its child, but object 66
	  // will have 0 as its sibling
	  assertEquals(170, objectTree.getChild(27));
	  assertEquals(0, objectTree.getSibling(66));
  }

  @Test
  public void testRemoveObjectHasNoParent() {
	  int lantern = 62;
	  objectTree.setParent(lantern, 0);
	  objectTree.removeObject(lantern);
	  assertEquals(0, objectTree.getParent(lantern));
  }

  @Test
  public void testInsertObjectSimple() {
	  // Simplest and first case: Move a single object without any relationship
	  // to a new parent, in this case object 30 ("you") to object 46
	  // ("West of house")
	  int you = 30;
	  int westofhouse = 46;

	  objectTree.insertObject(westofhouse, you);

	  // object becomes direct child of the parent
	  assertEquals(westofhouse, objectTree.getParent(you));
	  assertEquals(you, objectTree.getChild(westofhouse));

	  // and the former direct child becomes the first sibling
	  assertEquals(82, objectTree.getSibling(you));
  }

  @Test
  public void testInsertObjectHasSiblingsAndChild() {
	  // In this case, the object to insert has siblings and we do not
	  // want to move them with it, furthermore it has a child, and we
	  // want to move it
	  // move obj 158 ("studio") to obj 46 ("west of house")
	  int studio = 158;
	  int westofhouse = 46;
	  objectTree.insertObject(westofhouse, studio);
	  assertEquals(westofhouse, objectTree.getParent(studio));
	  assertEquals(studio, objectTree.getChild(westofhouse));
	  assertEquals(61, objectTree.getChild(studio));    
	  assertEquals(82, objectTree.getSibling(studio));

	  // The old siblings line up correctly, i.e. 87 -> 22 instead of 158
	  assertEquals(22, objectTree.getSibling(87));
  }

  @Test
  public void testGetPropertyLength() {
	  assertEquals(4, objectTree.getPropertyLength(0x1889));    
  }

  @Test
  public void testGetPropertyLengthAddress0() {
	  assertEquals(0, objectTree.getPropertyLength(0));
  }
}
