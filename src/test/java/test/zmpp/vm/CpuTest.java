/*
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

import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.CpuImpl;
import org.zmpp.vm.Machine;
import org.zmpp.vm.RoutineContext;
import org.zmpp.base.StoryFileHeader;

/**
 * Test class for the Cpu interface of the Machine object.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class CpuTest {
  Mockery context = new JUnit4Mockery();
  private Machine machine;
  private CpuImpl cpu;
  
  private RoutineContext routineInfo;
  
  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    routineInfo = new RoutineContext(3);
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(5));
      one (machine).readUnsigned16(StoryFileHeader.PROGRAM_START); will(returnValue((char) 1000));
      one (machine).readUnsigned16(StoryFileHeader.GLOBALS); will(returnValue((char) 5000));
    }});
    cpu = new CpuImpl(machine);
    cpu.reset();
  }
  
  @Test
  public void testInitialState() {
    assertEquals(1000, cpu.getPC());
    assertEquals(0, cpu.getSP());
    assertEquals(0, ((CpuImpl)cpu).getRoutineStackPointer());
  }
  
  @Test
  public void testSetProgramCounter() {
    cpu.setPC((char) 1234);
    assertEquals(1234, cpu.getPC());
  }  
  
  @Test
  public void testIncrementProgramCounter() {
    cpu.setPC((char) 1000);
    cpu.incrementPC(0);
    assertEquals(1000, cpu.getPC());
    
    cpu.setPC((char) 1000);
    cpu.incrementPC(123);
    assertEquals(1123, cpu.getPC());
    
    cpu.setPC((char) 1000);
    cpu.incrementPC(-32);
    assertEquals(968, cpu.getPC());
  }

  @Test
  public void testGetVariableType() {
    assertEquals(Cpu.VariableType.STACK, CpuImpl.getVariableType(0));
    assertEquals(Cpu.VariableType.LOCAL, CpuImpl.getVariableType(0x01));
    assertEquals(Cpu.VariableType.LOCAL, CpuImpl.getVariableType(0x0f));
    assertEquals(Cpu.VariableType.GLOBAL, CpuImpl.getVariableType(0x10));
    assertEquals(Cpu.VariableType.GLOBAL, CpuImpl.getVariableType(0xff));
  }
  
  @Test
  public void testVariableTypes() {
    assertTrue(Cpu.VariableType.STACK != Cpu.VariableType.LOCAL);
    assertTrue(Cpu.VariableType.LOCAL != Cpu.VariableType.GLOBAL);
    assertTrue(Cpu.VariableType.STACK != Cpu.VariableType.GLOBAL);
  }
  
  @Test
  public void testGetStackElement() {
    cpu.setVariable((char) 0, (char) 1);
    cpu.setVariable((char) 0, (char) 2);
    cpu.setVariable((char) 0, (char) 3);
    assertEquals(2, cpu.getStackElement(1));
  }
  
  @Test
  public void testSetRoutineContexts() {
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    RoutineContext routineContext = new RoutineContext(2);
    contexts.add(routineContext);
    cpu.setRoutineContexts(contexts);
    
    List<RoutineContext> currentContexts = cpu.getRoutineContexts();
    assertEquals(1, currentContexts.size());
    assertNotSame(contexts, currentContexts);
    assertEquals(routineContext, cpu.getCurrentRoutineContext());
  }
  
  @Test
  public void testGetCurrentRoutineContext() {
    // Initialize the routine context
    RoutineContext routineContext = new RoutineContext(0);
    
    // simulate a call
    cpu.pushRoutineContext(routineContext);
    
    // We can call this three times and it will stay the same
    assertEquals(routineContext, cpu.getCurrentRoutineContext());
    assertEquals(routineContext, cpu.getCurrentRoutineContext());
    assertEquals(routineContext, cpu.getCurrentRoutineContext());        
  }
  
  @Test
  public void testGetSetStackTopElement() {
    // initialize stack
    cpu.setVariable((char) 0, (char) 0);    
    cpu.setStackTop((char) 42);
    assertEquals(1, cpu.getSP());
    assertEquals(42, cpu.getStackTop());
    assertEquals(1, cpu.getSP());
  }
  
  @Test
  public void testGetStackTopElementStackEmpty() {
    try {
      cpu.getStackTop();
      fail("overflow error not thrown");
    } catch (ArrayIndexOutOfBoundsException overflow) {
      assertTrue(overflow.getMessage().length() > 0);
    }
  }
  
  @Test
  public void testGetVariableStackNonEmptyNoRoutineContext() {
    // Write something to the stack now
    cpu.setVariable((char) 0, (char) 4711);
    int oldStackPointer = cpu.getSP();
    int value = cpu.getVariable((char) 0);
    assertEquals(oldStackPointer - 1, cpu.getSP());
    assertEquals(value, 4711);
  }

  @Test
  public void testGetVariableStackNonEmptyWithRoutineContext() {
    // Write something to the stack now
    cpu.setVariable((char) 0, (char) 4711);
    
    RoutineContext routineContext = new RoutineContext(3);
    cpu.pushRoutineContext(routineContext);
    
    // Write a new value to the stack within the routine
    cpu.setVariable((char) 0, (char) 4712);
    
    int oldStackPointer = cpu.getSP();
    int value = cpu.getVariable((char) 0);
    assertEquals(oldStackPointer - 1, cpu.getSP());
    assertEquals(value, 4712);
  }
  
  @Test
  public void testSetVariableStack() {  
    int oldStackPointer = cpu.getSP();
    cpu.setVariable((char) 0, (char) 213);
    assertEquals(oldStackPointer + 1, cpu.getSP());
  }
  
  @Test
  public void testGetLocalVariableIllegal() {
    try {
      cpu.getVariable((char) 1);
      fail("accessing a local variable without a context should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("no routine context set", expected.getMessage());
    }

    cpu.pushRoutineContext(routineInfo);
    try {      
      cpu.getVariable((char) 5); // accessing a non-existent variable
      fail("accessing a non-existent local variable should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("access to non-existent local variable: 4",
          expected.getMessage());
    }
  }
  
  @Test
  public void testSetLocalVariable() {
    try {
      cpu.setVariable((char) 1, (char) 4711);
      fail("accessing a local variable without a context should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("no routine context set", expected.getMessage());
    }
    cpu.pushRoutineContext(routineInfo);
    cpu.setVariable((char) 1, (char) 4711); // Local variable 0
    assertEquals(4711, cpu.getVariable((char) 1));
    
    // access a non-existent variable
    try {
      cpu.setVariable((char) 6, (char) 2312);
      fail("accessing a non-existent local variable should yield an exception");
    } catch (IllegalStateException expected) { 
      assertEquals("access to non-existent local variable: 5",
          expected.getMessage());
    }
  }

  @Test
  public void testPopRoutineContextIllegal() {
    try {
      cpu.returnWith((char) 42);
      fail();
    } catch (IllegalStateException expected) {
     
      assertEquals("no routine context active", expected.getMessage());
    }
  }

  @Test
  public void testCallAndReturn() {
    // Setup the environment
    cpu.setVariable((char) 0, (char) 10); // write something on the stack
    int oldSp = cpu.getSP();
    // Use addresses, which exceed 16 Bit
    cpu.setPC(0x15747);
    int returnAddress = 0x15749;
    
    // Initialize the routine context
    RoutineContext routineContext = new RoutineContext(0);
    routineContext.setReturnVariable((char) 0x12);
    
    // simulate a call
    routineContext.setReturnAddress(returnAddress); // save the return address in the context
    cpu.pushRoutineContext(routineContext);
    cpu.setPC(0x15815);
    
    // assert that the context has saved the old stack pointer
    assertEquals(oldSp, routineContext.getInvocationStackPointer());
    
    // simulate some stack pushes
    cpu.setVariable((char) 0, (char) 213);
    cpu.setVariable((char) 0, (char) 214);
    cpu.setVariable((char) 0, (char) 215);

    // Set the variable
    context.checking(new Expectations() {{
      one (machine).writeUnsigned16(5004, (char) 42);
    }});
    
    assertNotSame(oldSp, cpu.getSP());
    cpu.returnWith((char) 42);
    assertEquals(returnAddress, cpu.getPC());
    assertEquals(oldSp, cpu.getSP());
  }  

  @Test
  public void testTranslatePackedAddressV3() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(3));
    }});
    int byteAddressR = cpu.unpackRoutineAddress((char) 65000);
    int byteAddressS = cpu.unpackStringAddress((char) 65000);
    assertEquals(65000 * 2, byteAddressR);
    assertEquals(65000 * 2, byteAddressS);
  }  

  @Test
  public void testTranslatePackedAddressV4() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(4));
    }});
    int byteAddressR = cpu.unpackRoutineAddress((char) 65000);
    int byteAddressS = cpu.unpackStringAddress((char) 65000);
    assertEquals(65000 * 4, byteAddressR);
    assertEquals(65000 * 4, byteAddressS);
  }

  @Test
  public void testTranslatePackedAddressV7() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(7));
      // routine offset
      one (machine).readUnsigned16(0x28); will(returnValue((char) 5));
      // static string offset
      one (machine).readUnsigned16(0x2a); will(returnValue((char) 6));
    }});
    int byteAddressR = cpu.unpackRoutineAddress((char) 65000);
    int byteAddressS = cpu.unpackStringAddress((char) 65000);
    assertEquals(65000 * 4 + 8 * 5, byteAddressR);
    assertEquals(65000 * 4 + 8 * 6, byteAddressS);
  }

  @Test
  public void testTranslatePackedAddressV8() {
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(8));
    }});
    int byteAddressR = cpu.unpackRoutineAddress((char) 65000);
    int byteAddressS = cpu.unpackStringAddress((char) 65000);
    assertEquals(65000 * 8, byteAddressR);
    assertEquals(65000 * 8, byteAddressS);
  }
}
