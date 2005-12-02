/*
 * $Id$
 * 
 * Created on 03.10.2005
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

import junit.framework.TestCase;

import org.zmpp.vm.RoutineContext;

public class RoutineContextTest extends TestCase {

  private RoutineContext context;
  
  public void setUp() {
    
    context = new RoutineContext(0x4711, 2);
  }
  
  public void testCreate() {
    
    assertEquals(0x4711, context.getStartAddress());
    assertEquals(2, context.getNumLocalVariables());
  }
  
  public void testSetters() {
    
    context.setLocalVariable(0, (short) 72);
    assertEquals(72, context.getLocalVariable(0));
    context.setLocalVariable(1, (short) 76);
    assertEquals(76, context.getLocalVariable(1));
    try {
      context.setLocalVariable(2, (short) 815);
      fail();
    } catch (IndexOutOfBoundsException expected) {

      // this is good
    }
    context.setReturnAddress(0x4711);
    assertEquals(0x4711, context.getReturnAddress());
    context.setReturnVariable((short) 0x13);
    assertEquals(0x13, context.getReturnVariable());
    context.setInvocationStackPointer(1234);
    assertEquals(1234, context.getInvocationStackPointer());
    context.setNumArguments(3);
    assertEquals(3, context.getNumArguments());
  }
}
