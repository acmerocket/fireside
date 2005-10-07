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
import org.zmpp.vm.Objects.Zobject;

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
  
  public void testGetObjectAt() {
    
    assertEquals(36, objects.getObjectAt(1).getParent());
    assertEquals(147, objects.getObjectAt(1).getSibling());
    assertEquals(0, objects.getObjectAt(1).getChild());
    assertEquals(0x0a4f, objects.getObjectAt(1).getPropertyTableAddress());
    assertTrue(objects.getObjectAt(1).isAttributeSet(6));
    
    assertEquals(27, objects.getObjectAt(2).getParent());
    assertEquals(119, objects.getObjectAt(2).getSibling());
    assertEquals(95, objects.getObjectAt(2).getChild());
    assertEquals(0x0a5d, objects.getObjectAt(2).getPropertyTableAddress());    
    assertTrue(objects.getObjectAt(2).isAttributeSet(5));
    assertFalse(objects.getObjectAt(2).isAttributeSet(6));
    assertTrue(objects.getObjectAt(2).isAttributeSet(7));
    assertTrue(objects.getObjectAt(2).isAttributeSet(19));
    
    assertFalse(objects.getObjectAt(27).isAttributeSet(0));
    assertFalse(objects.getObjectAt(27).isAttributeSet(1));    
  }
  
  public void testSetAttributes() {
    
    assertFalse(objects.getObjectAt(1).isAttributeSet(5));
    objects.getObjectAt(1).setAttribute(5);
    assertTrue(objects.getObjectAt(1).isAttributeSet(5));
  }
  
  public void testClearAttributes() {
    
    // Set several bits in a row to make sure there will be no arithmetical
    // shift errors
    objects.getObjectAt(1).setAttribute(0);
    objects.getObjectAt(1).setAttribute(1);
    objects.getObjectAt(1).setAttribute(2);
    objects.getObjectAt(1).setAttribute(3);
    
    assertTrue(objects.getObjectAt(1).isAttributeSet(2));
    objects.getObjectAt(1).clearAttribute(2);
    
    assertTrue(objects.getObjectAt(1).isAttributeSet(0));
    assertTrue(objects.getObjectAt(1).isAttributeSet(1));
    assertFalse(objects.getObjectAt(1).isAttributeSet(2));
    assertTrue(objects.getObjectAt(1).isAttributeSet(3));    
  }
  
  public void testObjectSetters() {
    
    Zobject obj = objects.getObjectAt(1);
    obj.setParent((short) 38);
    assertEquals(38, obj.getParent());
    obj.setChild((short) 39);
    assertEquals(39, obj.getChild());
    obj.setSibling((short) 42);
    assertEquals(42, obj.getSibling());
  }
  
  public void testGetNumObjects() {
    
    assertEquals(179, objects.getNumObjects());
  }
  
  public void testGetPropertiesTable() {
    
    int propaddress = objects.getObjectAt(1).getPropertiesDescriptionAddress();
    assertEquals("forest", converter.convert(minizorkmap, propaddress));
    assertEquals(2, objects.getObjectAt(1).getNumProperties());
    assertEquals(2, objects.getObjectAt(1).getPropertySize(18));
    assertEquals(4, objects.getObjectAt(1).getPropertySize(17));
    assertEquals(2644, objects.getObjectAt(1).getPropertyAddress(18));
    assertEquals(2647, objects.getObjectAt(1).getPropertyAddress(17));
    assertTrue(objects.getObjectAt(1).isPropertyAvailable(18));
    assertTrue(objects.getObjectAt(1).isPropertyAvailable(17));
    assertFalse(objects.getObjectAt(1).isPropertyAvailable(5));
    
    assertEquals(0x43, objects.getObjectAt(1).getPropertyByte(18, 0));
    assertEquals(0xa7, objects.getObjectAt(1).getPropertyByte(18, 1));

    assertEquals(0x2d, objects.getObjectAt(1).getPropertyByte(17, 0));
    assertEquals(0x23, objects.getObjectAt(1).getPropertyByte(17, 1));
    assertEquals(0x35, objects.getObjectAt(1).getPropertyByte(17, 2));
    assertEquals(0x8f, objects.getObjectAt(1).getPropertyByte(17, 3));
    
  }
}
