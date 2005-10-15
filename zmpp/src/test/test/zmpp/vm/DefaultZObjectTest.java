/*
 * $Id$
 * 
 * Created on 14.10.2005
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

import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.Objects;
import org.zmpp.vm.ZObject;

public class DefaultZObjectTest extends MemoryMapSetup {

  private ObjectTree objectTree;
  private ZObject object;
  
  public void setUp() throws Exception {
    
    super.setUp();
    this.objectTree = new Objects(minizorkmap, fileheader.getObjectTableAddress());
    this.object = objectTree.getObject((short) 1);
  }
  
  public void testObjectSetters() {
    
    object.setParent((short) 38);
    assertEquals(38, object.getParent());
    object.setChild((short) 39);
    assertEquals(39, object.getChild());
    object.setSibling((short) 42);
    assertEquals(42, object.getSibling());
  }
  
  public void testSetAttributes() {
    
    assertFalse(object.isAttributeSet(5));
    object.setAttribute(5);
    assertTrue(object.isAttributeSet(5));
  }
  
  public void testClearAttributes() {
    
    // Set several bits in a row to make sure there will be no arithmetical
    // shift errors
    object.setAttribute(0);
    object.setAttribute(1);
    object.setAttribute(2);
    object.setAttribute(3);
    
    assertTrue(object.isAttributeSet(2));
    object.clearAttribute(2);
    
    assertTrue(object.isAttributeSet(0));
    assertTrue(object.isAttributeSet(1));
    assertFalse(object.isAttributeSet(2));
    assertTrue(object.isAttributeSet(3));    
  }
    
  public void testGetPropertiesTable() {
    
    assertEquals(0x0a4f, object.getPropertyTableAddress());
    int propaddress = object.getPropertiesDescriptionAddress();
    assertEquals("forest", converter.convert(minizorkmap, propaddress));
    assertEquals(2, object.getNumProperties());
    assertEquals(2, object.getPropertySize(18));
    assertEquals(4, object.getPropertySize(17));
    assertEquals(2644, object.getPropertyAddress(18));
    assertEquals(2647, object.getPropertyAddress(17));
    assertTrue(object.isPropertyAvailable(18));
    assertTrue(object.isPropertyAvailable(17));
    assertFalse(object.isPropertyAvailable(5));
    
    assertEquals(0x43, object.getPropertyByte(18, 0));
    assertEquals(0xa7, object.getPropertyByte(18, 1));

    assertEquals(0x2d, object.getPropertyByte(17, 0));
    assertEquals(0x23, object.getPropertyByte(17, 1));
    assertEquals(0x35, object.getPropertyByte(17, 2));
    assertEquals(0x8f, object.getPropertyByte(17, 3));    
  }
  
  public void testGetNextProperty() {
    
    assertEquals(18, object.getNextProperty(0));
    assertEquals(17, object.getNextProperty(18));
    assertEquals(0, object.getNextProperty(17));
  }
  
  public void testSetPropertyByte() {
    
    object.setPropertyByte(18, 0, (short) 0x12);
    assertEquals(0x12, object.getPropertyByte(18, 0));
  }
}
