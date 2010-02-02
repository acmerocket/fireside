/*
 * Created on 10/03/2005
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
