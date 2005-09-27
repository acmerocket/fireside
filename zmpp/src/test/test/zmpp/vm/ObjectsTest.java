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

public class ObjectsTest extends MemoryMapSetup {

  private Objects objects;
  
  protected void setUp() throws Exception {
    super.setUp();
    this.objects = new Objects(minizorkmap, fileheader.getObjectTableAddress(),
                               converter);
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
    assertEquals(0x0a4f, objects.getObjectAt(1).getPropertiesAddress());
    assertEquals(33554432, objects.getObjectAt(1).getAttributeFlags());
    
    assertEquals(27, objects.getObjectAt(2).getParent());
    assertEquals(119, objects.getObjectAt(2).getSibling());
    assertEquals(95, objects.getObjectAt(2).getChild());
    assertEquals(0x0a5d, objects.getObjectAt(2).getPropertiesAddress());    
    assertEquals(83890176, objects.getObjectAt(2).getAttributeFlags());
    
    assertEquals(0, objects.getObjectAt(27).getAttributeFlags());
  }
  
  public void testGetNumObjects() {
    
    assertEquals(179, objects.getNumObjects());
  }
  
  public void testGetPropertiesTable() {
    
    assertEquals("forest", objects.getObjectAt(1).getPropertiesDescription());
    assertEquals(2, objects.getObjectAt(1).getNumPropertyRows());
    assertEquals(2, objects.getObjectAt(1).getNumPropertyColumns(0));
    assertEquals(4, objects.getObjectAt(1).getNumPropertyColumns(1));    
    
    assertEquals(0x43, objects.getObjectAt(1).getProperty(0, 0));
    assertEquals(0xa7, objects.getObjectAt(1).getProperty(0, 1));

    assertEquals(0x2d, objects.getObjectAt(1).getProperty(1, 0));
    assertEquals(0x23, objects.getObjectAt(1).getProperty(1, 1));
    assertEquals(0x35, objects.getObjectAt(1).getProperty(1, 2));
    assertEquals(0x8f, objects.getObjectAt(1).getProperty(1, 3));
  }
}
