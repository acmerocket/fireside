/*
 * $Id$
 *
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
import org.zmpp.vm.StoryFileHeader;
import test.zmpp.testutil.DummyStoryFileHeader;

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
  
  /**
   * Faked out file header.
   */
  private StoryFileHeader fileheader = new DummyStoryFileHeader() {
    @Override
    public int getProgramStart() { return 1000; }
    @Override
    public int getGlobalsAddress() { return 5000; }
    @Override
    public int getRoutineOffset() { return 5; }
    @Override
    public int getStaticStringOffset() { return 6; }
  };
  private RoutineContext routineInfo;
  
  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    routineInfo = new RoutineContext(0x4711, 3);
    context.checking(new Expectations() {{
      atLeast(1).of(machine).getFileHeader(); will(returnValue(fileheader));
      one (machine).getVersion(); will(returnValue(5));
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
    cpu.setPC(1234);
    assertEquals(1234, cpu.getPC());
  }  
  
  @Test
  public void testIncrementProgramCounter() {
    cpu.setPC(1000);
    cpu.incrementPC(0);
    assertEquals(1000, cpu.getPC());
    
    cpu.setPC(1000);
    cpu.incrementPC(123);
    assertEquals(1123, cpu.getPC());
    
    cpu.setPC(1000);
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
    cpu.setVariable(0, (short) 1);
    cpu.setVariable(0, (short) 2);
    cpu.setVariable(0, (short) 3);
    assertEquals(2, cpu.getStackElement(1));
  }
  
  @Test
  public void testSetRoutineContexts() {
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    RoutineContext routineContext = new RoutineContext(4711, 2);
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
    RoutineContext routineContext = new RoutineContext(0x0815, 0);
    
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
    cpu.setVariable(0, (short) 0);    
    cpu.setStackTop((short) 42);
    assertEquals(1, cpu.getSP());
    assertEquals(42, cpu.getStackTop());
    assertEquals(1, cpu.getSP());
  }
  
  @Test
  public void testGetStackTopElementStackEmpty() {  
    assertEquals(-1, cpu.getStackTop());
  }
  
  @Test
  public void testGetVariableStackNonEmptyNoRoutineContext() {
    // Write something to the stack now
    cpu.setVariable(0, (short) 4711);
    int oldStackPointer = cpu.getSP();
    int value = cpu.getVariable(0);
    assertEquals(oldStackPointer - 1, cpu.getSP());
    assertEquals(value, 4711);
  }

  @Test
  public void testGetVariableStackNonEmptyWithRoutineContext() {
    // Write something to the stack now
    cpu.setVariable(0, (short) 4711);
    
    RoutineContext routineContext = new RoutineContext(12345, 3);
    cpu.pushRoutineContext(routineContext);
    
    // Write a new value to the stack within the routine
    cpu.setVariable(0, (short) 4712);
    
    int oldStackPointer = cpu.getSP();
    int value = cpu.getVariable(0);
    assertEquals(oldStackPointer - 1, cpu.getSP());
    assertEquals(value, 4712);
  }
  
  @Test
  public void testSetVariableStack() {  
    int oldStackPointer = cpu.getSP();
    cpu.setVariable(0, (short) 213);
    assertEquals(oldStackPointer + 1, cpu.getSP());
  }
  
  @Test
  public void testGetLocalVariableIllegal() {
    try {
      cpu.getVariable(1);
      fail("accessing a local variable without a context should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("no routine context set", expected.getMessage());
    }

    cpu.pushRoutineContext(routineInfo);
    try {      
      cpu.getVariable(5); // accessing a non-existent variable
      fail("accessing a non-existent local variable should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("access to non-existent local variable: 4",
          expected.getMessage());
    }
  }
  
  @Test
  public void testSetLocalVariable() {
    try {
      cpu.setVariable(1, (short) 4711);
      fail("accessing a local variable without a context should yield an exception");
    } catch (IllegalStateException expected) {
      assertEquals("no routine context set", expected.getMessage());
    }
    cpu.pushRoutineContext(routineInfo);
    cpu.setVariable(1, (short) 4711); // Local variable 0
    assertEquals(4711, cpu.getVariable(1));
    
    // access a non-existent variable
    try {
      cpu.setVariable(6, (short) 2312);
      fail("accessing a non-existent local variable should yield an exception");
    } catch (IllegalStateException expected) { 
      assertEquals("access to non-existent local variable: 5",
          expected.getMessage());
    }
  }

  @Test
  public void testPopRoutineContextIllegal() {
    try {
      cpu.returnWith((short) 42);
      fail();
    } catch (IllegalStateException expected) {
     
      assertEquals("no routine context active", expected.getMessage());
    }
  }

  @Test
  public void testCallAndReturn() {
    // Setup the environment
    cpu.setVariable(0, (short) 10); // write something on the stack
    int oldSp = cpu.getSP();
    cpu.setPC(0x747);
    int returnAddress = 0x749;
    
    // Initialize the routine context
    RoutineContext routineContext = new RoutineContext(0x0815, 0);
    routineContext.setReturnVariable(0x12);
    
    // simulate a call
    routineContext.setReturnAddress(returnAddress); // save the return address in the context
    cpu.pushRoutineContext(routineContext);
    cpu.setPC(0x0815);
    
    // assert that the context has saved the old stack pointer
    assertEquals(oldSp, routineContext.getInvocationStackPointer());
    
    // simulate some stack pushes
    cpu.setVariable(0, (short) 213);
    cpu.setVariable(0, (short) 214);
    cpu.setVariable(0, (short) 215);

    // Set the variable
    context.checking(new Expectations() {{
      one (machine).writeShort(5004, (short) 42);
    }});
    
    assertNotSame(oldSp, cpu.getSP());
    cpu.returnWith((short) 42);
    assertEquals(returnAddress, cpu.getPC());
    assertEquals(oldSp, cpu.getSP());
  }  

  @Test
  public void testTranslatePackedAddressV3() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(3));
    }});
    int byteAddress = cpu.unpackAddress(2312, true);
    assertEquals(2312 * 2, byteAddress);
  }  

  @Test
  public void testTranslatePackedAddressV4() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(4));
    }});
    assertEquals(4711 * 4, cpu.unpackAddress(4711, true));
  }

  @Test
  public void testTranslatePackedAddressV5() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(5));
    }});
    assertEquals(4711 * 4, cpu.unpackAddress(4711, true));
  }

  @Test
  public void testTranslatePackedAddressV7Call() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(7));
    }});
    assertEquals(4711 * 4 + 8 * 5, cpu.unpackAddress(4711, true));
  }

  @Test
  public void testTranslatePackedAddressV7String() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(7));
    }});
    assertEquals(4711 * 4 + 8 * 6, cpu.unpackAddress(4711, false));
  }
  
  @Test
  public void testTranslatePackedAddressV8() {
    context.checking(new Expectations() {{
      one (machine).getVersion(); will(returnValue(8));
    }});
    assertEquals(4711 * 8, cpu.unpackAddress(4711, true));
  }
}
