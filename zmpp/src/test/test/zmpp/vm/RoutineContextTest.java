/*
 * Created on 10/03/2005
 * Copyright 2005-2009 by Wei-ju Wu
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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.vm.RoutineContext;

/**
 * Test class for RoutineContext.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class RoutineContextTest {

  private RoutineContext context;
  
  @Before
  public void setUp() {
    context = new RoutineContext(2);
  }
  
  @Test
  public void testCreate() {
    assertEquals(2, context.getNumLocalVariables());
  }
  
  @Test
  public void testSetters() {
    context.setLocalVariable((char) 0, (char) 72);
    assertEquals(72, context.getLocalVariable((char) 0));
    context.setLocalVariable((char) 1, (char) 76);
    assertEquals(76, context.getLocalVariable((char) 1));
    try {
      context.setLocalVariable((char) 2, (char) 815);
      fail();
    } catch (IndexOutOfBoundsException expected) {
      // this is good
    }
    context.setReturnAddress((char) 0x4711);
    assertEquals(0x4711, context.getReturnAddress());
    context.setReturnVariable((char) 0x13);
    assertEquals(0x13, context.getReturnVariable());
    context.setInvocationStackPointer((char) 1234);
    assertEquals(1234, context.getInvocationStackPointer());
    context.setNumArguments(3);
    assertEquals(3, context.getNumArguments());
  }
}
