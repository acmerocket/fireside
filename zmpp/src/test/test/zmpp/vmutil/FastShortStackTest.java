/*
 * $Id$
 * 
 * Created on 2006/05/10
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
package test.zmpp.vmutil;

import junit.framework.TestCase;

import org.zmpp.vmutil.FastShortStack;

public class FastShortStackTest extends TestCase {

  private FastShortStack stack;
  
  protected void setUp() throws Exception {

    this.stack = new FastShortStack(123);
  }

  public void testInitial() {
    
    assertEquals(0, stack.getStackPointer());
    assertEquals(0, stack.size());
  }
  
  public void testSize() {
    
    stack.push((short) 1);
    assertEquals(1, stack.size());
    stack.push((short) 3);
    assertEquals(2, stack.size());
    stack.pop();
    assertEquals(1, stack.size());
  }
  
  public void testPushTop() {
    
    stack.push((short) 3);
    assertEquals("stack pointer should have been increased", 1, stack.getStackPointer());
    assertEquals("the value 3 should be on top of the stack", 3, stack.top());
    assertEquals("stack pointer should not have been modified", 1, stack.getStackPointer());    
  }
  
  public void testPushPop() {
    
    stack.push((short) 3);
    assertEquals("the value 3 should be on top of the stack", 3, stack.pop());
    assertEquals("stack pointer should have been decreased", 0, stack.getStackPointer());
  }
    
  public void testGetValueAt() {
    
    stack.push((short) 3);
    stack.push((short) 5);
    stack.push((short) 7);
    
    assertEquals(3, stack.getValueAt(0));
    assertEquals(5, stack.getValueAt(1));
    assertEquals(7, stack.getValueAt(2));
    assertEquals("stack pointer should not have been modified", 3, stack.getStackPointer());
  }
  
  public void testReplaceTopElement() {
    
    stack.push((short) 3);
    stack.push((short) 5);
    stack.push((short) 7);
    stack.replaceTopElement((short) 11);
    assertEquals("top element should be 11 now", 11, stack.top());
    assertEquals("number of elements should be 3", 3, stack.size());
  }
  
}
