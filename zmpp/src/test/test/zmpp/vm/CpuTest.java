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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.Memory;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.CpuImpl;
import org.zmpp.vm.Instruction;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.StoryFileHeader;

public class CpuTest extends MockObjectTestCase {

  private Mock mockMachine, mockDecoder, mockFileHeader, mockMemory;
  private Machine machine;
  private InstructionDecoder decoder;
  private Cpu cpu;
  private StoryFileHeader fileheader;
  private Memory memory;
  private RoutineContext routineInfo;
  
  @Override
  public void setUp() throws Exception {
    mockMachine = mock(Machine.class);
    mockDecoder = mock(InstructionDecoder.class);
    mockFileHeader = mock(StoryFileHeader.class);
    mockMemory = mock(Memory.class);
    
    machine = (Machine) mockMachine.proxy();
    decoder = (InstructionDecoder) mockDecoder.proxy();
    fileheader = (StoryFileHeader) mockFileHeader.proxy();
    memory = (Memory) mockMemory.proxy();
    routineInfo = new RoutineContext(0x4711, 3);
    
    mockMachine.expects(atLeastOnce()).method("getFileHeader").will(returnValue(fileheader));
    mockFileHeader.expects(once()).method("getProgramStart").will(returnValue(1000));
    mockFileHeader.expects(once()).method("getGlobalsAddress").will(returnValue(5000));

    initializeMockDecoder();
    cpu = new CpuImpl(machine, decoder);
    cpu.reset();
  }
  
  private void initializeMockDecoder() {
    mockMachine.expects(once()).method("getVersion").will(returnValue(5));
    mockDecoder.expects(once()).method("initialize").with(eq(machine));
  }
  
  public void testInitialState() {
    assertEquals(1000, cpu.getPC());
    assertEquals(0, cpu.getStackPointer());
    assertEquals(0, ((CpuImpl)cpu).getRoutineStackPointer());
  }
  
  public void testSetProgramCounter() {
    cpu.setPC(1234);
    assertEquals(1234, cpu.getPC());
  }  
  
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

  public void testGetVariableType() {
    
    assertEquals(Cpu.VariableType.STACK, CpuImpl.getVariableType(0));
    assertEquals(Cpu.VariableType.LOCAL, CpuImpl.getVariableType(0x01));
    assertEquals(Cpu.VariableType.LOCAL, CpuImpl.getVariableType(0x0f));
    assertEquals(Cpu.VariableType.GLOBAL, CpuImpl.getVariableType(0x10));
    assertEquals(Cpu.VariableType.GLOBAL, CpuImpl.getVariableType(0xff));
  }
  
  public void testVariableTypes() {
    
    assertTrue(Cpu.VariableType.STACK != Cpu.VariableType.LOCAL);
    assertTrue(Cpu.VariableType.LOCAL != Cpu.VariableType.GLOBAL);
    assertTrue(Cpu.VariableType.STACK != Cpu.VariableType.GLOBAL);
  }
  
  public void testGetStackElement() {
    
    cpu.setVariable(0, (short) 1);
    cpu.setVariable(0, (short) 2);
    cpu.setVariable(0, (short) 3);
    assertEquals(2, cpu.getStackElement(1));
  }
  
  public void testSetRoutineContexts() {
    
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    RoutineContext context = new RoutineContext(4711, 2);
    contexts.add(context);
    cpu.setRoutineContexts(contexts);
    
    List<RoutineContext> currentContexts = cpu.getRoutineContexts();
    assertEquals(1, currentContexts.size());
    assertNotSame(contexts, currentContexts);
    assertEquals(context, cpu.getCurrentRoutineContext());
  }
  
  public void testGetCurrentRoutineContext() {
    
    // Initialize the routine context
    RoutineContext context = new RoutineContext(0x0815, 0);
    
    // simulate a call
    cpu.pushRoutineContext(context);
    
    // We can call this three times and it will stay the same
    assertEquals(context, cpu.getCurrentRoutineContext());
    assertEquals(context, cpu.getCurrentRoutineContext());
    assertEquals(context, cpu.getCurrentRoutineContext());        
  }
  
  public void testGetSetStackTopElement() {
    
    // initialize stack
    cpu.setVariable(0, (short) 0);    
    cpu.setStackTopElement((short) 42);
    assertEquals(1, cpu.getStackPointer());
    assertEquals(42, cpu.getStackTopElement());
    assertEquals(1, cpu.getStackPointer());
  }
  
  public void testGetStackTopElementStackEmpty() {
    
    assertEquals(-1, cpu.getStackTopElement());
  }
  
  public void testGetVariableStackNonEmptyNoRoutineContext() {
    // Write something to the stack now
    cpu.setVariable(0, (short) 4711);
    int oldStackPointer = cpu.getStackPointer();
    int value = cpu.getVariable(0);
    assertEquals(oldStackPointer - 1, cpu.getStackPointer());
    assertEquals(value, 4711);
  }

  public void testGetVariableStackNonEmptyWithRoutineContext() {
    
    // Write something to the stack now
    cpu.setVariable(0, (short) 4711);
    
    RoutineContext routineContext = new RoutineContext(12345, 3);
    cpu.pushRoutineContext(routineContext);
    
    // Write a new value to the stack within the routine
    cpu.setVariable(0, (short) 4712);
    
    int oldStackPointer = cpu.getStackPointer();
    int value = cpu.getVariable(0);
    assertEquals(oldStackPointer - 1, cpu.getStackPointer());
    assertEquals(value, 4712);
  }
  
  public void testSetVariableStack() {
    
    int oldStackPointer = cpu.getStackPointer();
    cpu.setVariable(0, (short) 213);
    assertEquals(oldStackPointer + 1, cpu.getStackPointer());
  }
  
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

  public void testPopRoutineContextIllegal() {
    
    try {
      cpu.returnWith((short) 42);
      fail();
    } catch (IllegalStateException expected) {
     
      assertEquals("no routine context active", expected.getMessage());
    }
  }

  public void testCallAndReturn() {
    
    mockMachine.expects(atLeastOnce()).method("getMemory").will(returnValue(memory));    
    // Setup the environment
    cpu.setVariable(0, (short) 10); // write something on the stack
    int oldSp = cpu.getStackPointer();
    cpu.setPC(0x747);
    int returnAddress = 0x749;
    
    // Initialize the routine context
    RoutineContext context = new RoutineContext(0x0815, 0);
    context.setReturnVariable(0x12);
    
    // simulate a call
    context.setReturnAddress(returnAddress); // save the return address in the context
    cpu.pushRoutineContext(context);
    cpu.setPC(0x0815);
    
    // assert that the context has saved the old stack pointer
    assertEquals(oldSp, context.getInvocationStackPointer());
    
    // simulate some stack pushes
    cpu.setVariable(0, (short) 213);
    cpu.setVariable(0, (short) 214);
    cpu.setVariable(0, (short) 215);

    // Set the variable
    mockMemory.expects(once()).method("writeShort").with(eq(5004), eq((short) 42));
    
    assertNotSame(oldSp, cpu.getStackPointer());
    cpu.returnWith((short) 42);
    assertEquals(returnAddress, cpu.getPC());
    assertEquals(oldSp, cpu.getStackPointer());
  }  

  public void testNextStep() {
    Instruction myinstr = new Instruction() {      
      public void execute() { }
      public boolean isOutput() { return false; }
    };
    mockDecoder.expects(once()).method("decodeInstruction").with(eq(1000)).will(returnValue(myinstr));
    Instruction instr = cpu.nextStep();
    assertEquals(myinstr, instr);
  }
  
  public void testTranslatePackedAddressV3() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    int byteAddress = cpu.translatePackedAddress(2312, true);
    assertEquals(2312 * 2, byteAddress);
  }  

  public void testTranslatePackedAddressV4() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    assertEquals(4711 * 4, cpu.translatePackedAddress(4711, true));
  }

  public void testTranslatePackedAddressV5() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    assertEquals(4711 * 4, cpu.translatePackedAddress(4711, true));
  }

  public void testTranslatePackedAddressV7Call() {
    mockFileHeader.expects(once()).method("getRoutineOffset").will(returnValue(5));
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(7));
    assertEquals(4711 * 4 + 8 * 5, cpu.translatePackedAddress(4711, true));
  }

  public void testTranslatePackedAddressV7String() {
    mockFileHeader.expects(once()).method("getStaticStringOffset").will(returnValue(6));
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(7));
    assertEquals(4711 * 4 + 8 * 6, cpu.translatePackedAddress(4711, false));
  }
  
  public void testTranslatePackedAddressV8() {
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(8));
    assertEquals(4711 * 8, cpu.translatePackedAddress(4711, true));
  }
}
