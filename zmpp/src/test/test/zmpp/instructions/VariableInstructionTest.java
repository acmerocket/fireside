/*
 * $Id$
 * 
 * Created on 04.10.2005
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
package test.zmpp.instructions;

import org.jmock.Mock;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.Machine;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.TextCursor;

/**
 * This class tests the VariableInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class VariableInstructionTest extends InstructionTestBase {

  private Mock mockScreen;
  private ScreenModel screen;
  private Mock mockCursor;
  private TextCursor cursor;
  
  protected void setUp() throws Exception {
    super.setUp();

    // A routine context to have valid local variable access
    RoutineContext routineInfo = new RoutineContext(0x4711, 2);
    machine.pushRoutineContext(routineInfo);    
    mockScreen = mock(ScreenModel.class);
    screen = (ScreenModel) mockScreen.proxy();
    mockCursor = mock(TextCursor.class);
    cursor = (TextCursor) mockCursor.proxy();
  }

  public void testStoresValueV4() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    VariableInstruction info;
    info = new VariableInstruction(machine, OperandCount.VAR,
        VariableStaticInfo.OP_CALL);    
    assertTrue(info.storesResult());
    
    info.setOpcode(VariableStaticInfo.OP_RANDOM);
    assertTrue(info.storesResult());
    
    info.setOpcode(VariableStaticInfo.OP_CALL_VS2);
    assertTrue(info.storesResult());
    
    info.setOpcode(VariableStaticInfo.OP_READ_CHAR);
    assertTrue(info.storesResult());
    
    info.setOpcode(VariableStaticInfo.OP_SCAN_TABLE);
    assertTrue(info.storesResult());
    
    // no store
    info.setOpcode(VariableStaticInfo.OP_PRINT_CHAR);
    assertFalse(info.storesResult());
    
    info.setOpcode(VariableStaticInfo.OP_SREAD);
    assertFalse(info.storesResult());
  }

  public void testAreadStoresValueInV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    VariableInstruction aread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_AREAD);    
    assertTrue(aread.storesResult());
  }
  
  public void testNotStoresValueInV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    
    VariableInstruction not5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_NOT);    
    assertTrue(not5.storesResult());
    
    VariableInstruction callvn5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);    
    assertFalse(callvn5.storesResult());
    
    VariableInstruction callvn2_5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);    
    assertFalse(callvn2_5.storesResult());
  }
  
  
  public void testIsBranch() {
    
    VariableInstruction info;
    info = new VariableInstruction(machine, OperandCount.VAR,
        VariableStaticInfo.OP_SCAN_TABLE);    
    assertTrue(info.isBranch());
  }
  
  public void testIllegalOpcode() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 238"));
    VariableInstruction illegal = new VariableInstruction(machine,
        OperandCount.VAR, 0xee);
    illegal.execute();
  }
  
  public void testCall() {
    
    VariableInstruction call_0 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call_0.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call_0.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call_0.setLength(5);    
    
    int oldpc = machine.getProgramCounter();
    call_0.execute();
    assertEquals(oldpc + call_0.getLength(), machine.getProgramCounter());
    
    // Real call
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1bc5));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x02));
    call.setStoreVariable((short) 0x11);
    call.setLength(5);
    
    oldpc = machine.getProgramCounter();
    call.execute();
    
    RoutineContext routineContext = machine.getCurrentRoutineContext();
    assertEquals(0x3797, machine.getProgramCounter());
    assertEquals(oldpc + call.getLength(), routineContext.getReturnAddress());
    assertEquals(0x11, routineContext.getReturnVariable());
    
    // Test parameters
    assertEquals(0x01, routineContext.getLocalVariable(0));
    assertEquals(0x02, routineContext.getLocalVariable(1));
    assertEquals(0x01, machine.getVariable(0x01));
    assertEquals(0x02, machine.getVariable(0x02));
  }
  
  // *******************************************************************
  // ********* CALL_VS2
  // *************************
  
  public void testCallVs2InvalidForVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 12"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VS2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }

  // *******************************************************************
  // ********* CALL_VN
  // *************************
  
  public void testCallVNIllegalForVersion4() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 25"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }
  
  public void testCallVN2IllegalForVersion4() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 26"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }
  // *******************************************************************
  // ********* STOREW
  // *************************
  
  public void testStorew() {
    
    VariableInstruction storew = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREW);
    
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    storew.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1000));
    storew.setLength(5);
    
    int oldpc = machine.getProgramCounter();
    storew.execute();
    assertEquals(0x1000, memoryAccess.readUnsignedShort(0x0002));
    assertEquals(oldpc + storew.getLength(), machine.getProgramCounter());
  }
  
  // *******************************************************************
  // ********* STOREB
  // *************************
  
  public void testStoreb() {
    
    VariableInstruction storeb = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREB);
    
    storeb.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x15));
    storeb.setLength(5);
    
    int oldpc = machine.getProgramCounter();
    storeb.execute();
    assertEquals(0x15, memoryAccess.readUnsignedByte(0x0001));
    assertEquals(oldpc + storeb.getLength(), machine.getProgramCounter());
  }
  
  // *******************************************************************
  // ********* PUT_PROP
  // *************************
  
  public void testPutPropLength1() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    mockObjectTree.expects(once()).method("getObject").with(eq(2)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(22)).will(returnValue(true));
    mockZObject.expects(once()).method("getPropertySize").with(eq(22)).will(returnValue(1));
    mockZObject.expects(once()).method("setPropertyByte").with(eq(22), eq(0), eq((byte) -1));
    
    VariableInstruction put_prop1 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUT_PROP);    
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 22));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xffff));
    put_prop1.setLength(5);
    put_prop1.execute();  

  }

  public void testPutPropLength2() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    mockObjectTree.expects(once()).method("getObject").with(eq(2)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(24)).will(returnValue(true));
    mockZObject.expects(once()).method("getPropertySize").with(eq(24)).will(returnValue(2));
    mockZObject.expects(once()).method("setPropertyByte").with(eq(24), eq(0), eq((byte) -1));
    mockZObject.expects(once()).method("setPropertyByte").with(eq(24), eq(1), eq((byte) -1));
    
    VariableInstruction put_prop1 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUT_PROP);    
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 24));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xffff));
    put_prop1.setLength(5);
    put_prop1.execute();
  }
  
  public void testPutPropNotExists() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockMachine.expects(once()).method("halt").with(eq("put_prop: the property [5] of object [1] does not exist"));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(5)).will(returnValue(false));
    
    VariableInstruction put_prop_halt = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUT_PROP);
    put_prop_halt.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    put_prop_halt.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 5));
    put_prop_halt.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xffff));
    put_prop_halt.execute();
  }
  
  // *******************************************************************
  // ********* PRINT_CHAR
  // *************************
  
  public void testPrintChar() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("printZsciiChar").with(eq((short) 97), eq(false));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    VariableInstruction print_char = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_CHAR);
    print_char.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x61));
    print_char.setLength(5);    
    print_char.execute();
  }
  
  // *******************************************************************
  // ********* PRINT_NUM
  // *************************
  
  public void testPrintNum() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("printNumber").with(eq((short) -12));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    VariableInstruction print_num = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_NUM);
    print_num.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) -12));
    print_num.setLength(5);
    print_num.execute();
  }
  
  // *******************************************************************
  // ********* PUSH
  // *************************
  
  public void testPush() {
    
    VariableInstruction push = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUSH);
    push.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x13));
    push.setLength(5);
    int oldpc = machine.getProgramCounter();
    push.execute();
    assertEquals(0x13, machine.getVariable(0));
    assertEquals(oldpc + push.getLength(), machine.getProgramCounter());
  }
  
  // *******************************************************************
  // ********* PULL
  // *************************
  
  public void testPull() {
    
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x13));
    pull.setLength(5);
    
    machine.setVariable(0, (short) 0x14);
    int oldpc = machine.getProgramCounter();
    pull.execute();
    assertEquals(0x14, machine.getVariable(0x13));
    assertEquals(oldpc + pull.getLength(), machine.getProgramCounter());
  }
  
  // As long as we did not use mock objects here, we have to initialize the
  // machine state. We check the Standard 1.1 enhancement that pull to the
  // stack will not modify the stack pointer
  public void testPullToStack() {
    
    // Initialize the stack with a value
    machine.setVariable(0, (short) 0);
    
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x00));
    pull.setLength(5);
    
    int oldSP = machine.getStackPointer();
    machine.setVariable(0, (short) 0x14); // will increment oldSP
    int oldpc = machine.getProgramCounter();
    
    pull.execute();
    
    assertEquals(oldSP, machine.getStackPointer());
    assertEquals(0x14, machine.getVariable(0x00));
    assertEquals(oldpc + pull.getLength(), machine.getProgramCounter());
  }
    
  // *******************************************************************
  // ********* INPUTSTREAM
  // *************************
  
  public void testInputStream() {
   
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("selectInputStream").with(eq(1));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    VariableInstruction inputstream = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_INPUTSTREAM);
    inputstream.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    inputstream.setLength(5);
    inputstream.execute();
  }

  // *******************************************************************
  // ********* OUTPUTSTREAM
  // *************************
  
  public void testOutputStreamDisable2() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("selectOutputStream").with(eq(2), eq(false));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    // disable
    VariableInstruction outputstream_disable = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_disable.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -2));
    outputstream_disable.setLength(5);
    outputstream_disable.execute();
  }
  
  public void testOutputStreamNoAction() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    // do nothing
    VariableInstruction outputstream_nothing = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_nothing.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    outputstream_nothing.setLength(5);
    outputstream_nothing.execute();
  }
  
  public void testOutputStreamEnable2() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("selectOutputStream").with(eq(2), eq(true));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4716));
    
    // enable
    VariableInstruction outputstream_enable = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_enable.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    outputstream_enable.setLength(5);
    outputstream_enable.execute();
  } 
  
  // *******************************************************************
  // ********* RANDOM
  // *************************
  
  public void testRandom() {
    
    VariableInstruction random = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_RANDOM);
    random.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1234));
    random.setLength(5);
    random.setStoreVariable((short) 0x13);
    int oldpc = machine.getProgramCounter();
    random.execute();
    assertTrue(machine.getVariable(0x13) > 0);
    assertEquals(oldpc + random.getLength(), machine.getProgramCounter());
  }
  
  // *******************************************************************
  // ********* SOUND_EFFECT
  // *************************
  
  public void testSoundEffect() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("playSoundEffect");
    VariableInstructionMock sound_effect =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SOUND_EFFECT);
    sound_effect.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    sound_effect.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    sound_effect.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    sound_effect.execute();
    assertTrue(sound_effect.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* SPLIT_WINDOW
  // *************************
  
  public void testSplitWindow() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("splitWindow").with(eq(12));
    
    VariableInstructionMock split_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SPLIT_WINDOW);
    split_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    split_window.execute();
    assertTrue(split_window.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* SET_WINDOW
  // *************************
  
  public void testSetWindow() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setWindow").with(eq(2));
    
    VariableInstructionMock set_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_WINDOW);
    set_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_window.execute();
    assertTrue(set_window.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* SET_TEXT_STYLE
  // *************************
  
  public void testSetTextStyle() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setTextStyle").with(eq(2));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_text_style.execute();
    assertTrue(set_text_style.nextInstructionCalled);
  }
  
  public void testSetTextStyleInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 17"));
    
    
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_text_style.execute();
  }
  
  // *******************************************************************
  // ********* BUFFER_MODE
  // *************************

  public void testBufferModeTrue() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setBufferMode").with(eq(true));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }
  
  public void testBufferModeFalse() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setBufferMode").with(eq(false));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }

  public void testBufferModeInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 18"));
    
    
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    buffer_mode.execute();
  }
  
  // *******************************************************************
  // ********* ERASE_WINDOW
  // *************************

  public void testEraseWindow() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("eraseWindow").with(eq(1));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
        
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);
    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_window.execute();
    assertTrue(erase_window.nextInstructionCalled);
  }
  
  public void testEraseWindowInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 13"));
    
    
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);
    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_window.execute();
  }

  // *******************************************************************
  // ********* ERASE_LINE
  // *************************

  public void testEraseLine() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("eraseLine").with(eq(1));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
        
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_line.execute();
    assertTrue(erase_line.nextInstructionCalled);
  }
  
  public void testEraseLineInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 14"));
    
    
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_line.execute();
  }

  // *******************************************************************
  // ********* SET_CURSOR
  // *************************

  public void testSetCursor() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setTextCursor").with(eq(1), eq(2));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
        
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_cursor.execute();
    assertTrue(set_cursor.nextInstructionCalled);
  }  
  
  public void testSetCursorInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 15"));    
    
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    set_cursor.execute();
  }

  // *******************************************************************
  // ********* GET_CURSOR
  // *************************

  public void testGetCursor() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockMachine.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockScreen.expects(once()).method("getTextCursor").will(returnValue(cursor));
    mockCursor.expects(once()).method("getLine").will(returnValue(1));
    mockCursor.expects(once()).method("getColumn").will(returnValue(1));
    mockMemAccess.expects(atLeastOnce()).method("writeShort").withAnyArguments();
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
        
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    get_cursor.execute();
    assertTrue(get_cursor.nextInstructionCalled);
  }
  
  public void testGetCursorInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 16"));    
    
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    get_cursor.execute();
  }

  // *******************************************************************
  // ********* SCAN_TABLE
  // *************************
  
  public void testScanTableInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 23"));    
    
    VariableInstructionMock scan_table =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SCAN_TABLE);    
    scan_table.execute();
  }

  // *******************************************************************
  // ********* READ_CHAR
  // *************************

  public void testReadCharInvalidInVersion3() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 22"));    
    
    VariableInstructionMock read_char =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_READ_CHAR);    
    read_char.execute();
  }

  // *******************************************************************
  // ********* NOT
  // *************************
  
  public void testNotIsIllegalPriorV5() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 24"));    
    
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);    
    not.execute();
  }
  
  public void testNotInV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0x5555));     
    
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);
    not.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xaaaa));
    not.setStoreVariable((short) 0x12);    
    not.execute();
    
    assertTrue(not.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* TOKENISE
  // *************************
  
  public void testTokeniseIllegalPriorV5() {
    
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 27"));    
    
    VariableInstructionMock tokenise =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_TOKENISE);    
    tokenise.execute();
  }
  
  // *******************************************************************
  // ********* GET_OP_NAME
  // *************************
  
  public void testGetOpName() {

    VariableStaticInfo info = VariableStaticInfo.getInstance();
    
    for (int i = VariableStaticInfo.OP_CALL; i <= VariableStaticInfo.OP_TOKENISE; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }    
    assertNotNull(info.getOpName(1234, 3));
  }
  
  public void testGetOpNameV5() {

    VariableStaticInfo info = VariableStaticInfo.getInstance();
    assertEquals("AREAD", info.getOpName(VariableStaticInfo.OP_AREAD, 5));
  }
  
  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  class VariableInstructionMock extends VariableInstruction {
  
    
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public VariableInstructionMock(Machine machine, int opcode) {
      
      super(machine, OperandCount.VAR, opcode);
    }
    
    protected void nextInstruction() {
      
      nextInstructionCalled = true;
    }
    
    protected void returnFromRoutine(short retval) {
      
      returned = true;
      returnValue = retval;
    }
    
    protected void branchOnTest(boolean flag) {

      branchOnTestCalled = true;
      branchOnTestCondition = flag;
    }
  }
}
