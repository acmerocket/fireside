/*
 * $Id$
 * 
 * Created on 25.09.2005
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

import org.zmpp.vm.Objects;
import org.zmpp.vm.ZObject;

public class ObjectsTest extends MemoryMapSetup {

  private Objects objects;
  
  protected void setUp() throws Exception {
    super.setUp();
    this.objects = new Objects(minizorkmap, fileheader.getObjectTableAddress());
  }

  public void testGetPropertyDefault() {

    for (int i = 0; i < 12; i ++) {
      assertEquals(0, objects.getPropertyDefault(i));
    }
  }
  
  public void testGetObject() {
    
    ZObject obj1 = objects.getObject((short) 1);
    assertNotNull(obj1);
    assertEquals(36, obj1.getParent());
    assertEquals(147, obj1.getSibling());
    assertEquals(0, obj1.getChild());
  }
  
  public void testGetNumObjects() {
    
    assertEquals(179, objects.getNumObjects());
  }

  public void testRemoveObjectFirstChild() {
    
    // remove a thief's lair - object 170
    ZObject thiefslair = objects.getObject((short) 170);
    assertEquals(27, thiefslair.getParent());
    assertEquals(175, thiefslair.getChild());
    assertEquals(56, thiefslair.getSibling());
    
    objects.removeObject((short) 170);
    
    ZObject obj27 = objects.getObject((short) 27);
    
    // parent needs to be 0
    assertEquals(0, thiefslair.getParent());
    
    // the old parent needs to point to the next child
    assertEquals(56, obj27.getChild());
  }
  
  public void testRemoveObjectNotFirstChild() {
    
    // remove a cyclops room - object 56
    ZObject cyclopsroom = objects.getObject((short) 56);
    assertEquals(27, cyclopsroom.getParent());
    assertEquals(137, cyclopsroom.getChild());
    assertEquals(154, cyclopsroom.getSibling());
    
    objects.removeObject((short) 56);
    
    // parent needs to be 0
    assertEquals(0, cyclopsroom.getParent());
    
    // the old parent does not need to change its child, but the
    // sibling chain needs to be corrected, so after 170 there will
    // follow 154 instead of 56
    ZObject obj27 = objects.getObject((short) 27);
    assertEquals(170, obj27.getChild());
    ZObject obj170 = objects.getObject((short) 170);
    assertEquals(154, obj170.getSibling());
  }

  public void testRemoveObjectNotFirstButLastChild() {
    
    // remove a burnt out lantern - object 62
    ZObject lantern = objects.getObject((short) 62);
    assertEquals(157, lantern.getParent());
    assertEquals(0, lantern.getChild());
    assertEquals(0, lantern.getSibling());
    
    objects.removeObject((short) 62);
    
    // parent needs to be 0
    assertEquals(0, lantern.getParent());
    
    // the old parent does not need to change its child, but object 66
    // will have 0 as its sibling
    ZObject obj27 = objects.getObject((short) 27);
    assertEquals(170, obj27.getChild());
    ZObject obj66 = objects.getObject((short) 66);
    assertEquals(0, obj66.getSibling());
  }
  
  public void testInsertObjectSimple() {
    
    // Simplest and first case: Move a single object without any relationship
    // to a new parent, in this case object 30 ("you") to object 46
    // ("West of house")
    ZObject you = objects.getObject((short) 30);
    ZObject westofhouse = objects.getObject((short) 46);
    
    objects.insertObject((short) 46, (short) 30);
    
    // object becomes direct child of the parent
    assertEquals(46, you.getParent());
    assertEquals(30, westofhouse.getChild());
    
    // and the former direct child becomes the first sibling
    assertEquals(82, you.getSibling());
  }
  
  public void testInsertObjectHasSiblingsAndChild() {
    
    // In this case, the object to insert has siblings and we do not
    // want to move them with it, furthermore it has a child, and we
    // want to move it
    // move obj 158 ("studio") to obj 46 ("west of house")
    ZObject studio = objects.getObject((short) 158);
    ZObject westofhouse = objects.getObject((short) 46);
    objects.insertObject((short) 46, (short) 158);
    assertEquals(46, studio.getParent());
    assertEquals(158, westofhouse.getChild());
    assertEquals(61, studio.getChild());
    
    assertEquals(82, studio.getSibling());
    
    // The old siblings line up correctly, i.e. 87 -> 22 instead of 158
    ZObject obj87 = objects.getObject((short) 87);
    assertEquals(22, obj87.getSibling());
  }
}
