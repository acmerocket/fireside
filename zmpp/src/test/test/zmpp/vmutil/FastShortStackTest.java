/*
 * $Id$
 * 
 * Created on 2006/05/10
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
package test.zmpp.vmutil;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.vmutil.FastShortStack;

/**
 * Test class for FastShortStack.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class FastShortStackTest {

  private FastShortStack stack;
  
  @Before
  public void setUp() throws Exception {
    this.stack = new FastShortStack(123);
  }

  @Test
  public void testInitial() {
    assertEquals(0, stack.getStackPointer());
    assertEquals(0, stack.size());
  }
  
  @Test
  public void testSize() {
    stack.push((char) 1);
    assertEquals(1, stack.size());
    stack.push((char) 3);
    assertEquals(2, stack.size());
    stack.pop();
    assertEquals(1, stack.size());
  }
  
  @Test
  public void testPushTop() {
    stack.push((char) 3);
    assertEquals("stack pointer should have been increased", 1, stack.getStackPointer());
    assertEquals("the value 3 should be on top of the stack", 3, stack.top());
    assertEquals("stack pointer should not have been modified", 1, stack.getStackPointer());    
  }

  @Test
  public void testPushPop() {
    stack.push((char) 3);
    assertEquals("the value 3 should be on top of the stack", 3, stack.pop());
    assertEquals("stack pointer should have been decreased", 0, stack.getStackPointer());
  }

  @Test
  public void testGetValueAt() {
    stack.push((char) 3);
    stack.push((char) 5);
    stack.push((char) 7);
    assertEquals(3, stack.getValueAt(0));
    assertEquals(5, stack.getValueAt(1));
    assertEquals(7, stack.getValueAt(2));
    assertEquals("stack pointer should not have been modified", 3, stack.getStackPointer());
  }
  
  @Test
  public void testReplaceTopElement() { 
    stack.push((char) 3);
    stack.push((char) 5);
    stack.push((char) 7);
    stack.replaceTopElement((char) 11);
    assertEquals("top element should be 11 now", 11, stack.top());
    assertEquals("number of elements should be 3", 3, stack.size());
  }
}
