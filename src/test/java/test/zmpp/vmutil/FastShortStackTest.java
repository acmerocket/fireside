/*
 * Created on 2006/05/10
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
