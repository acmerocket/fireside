/*
 * $Id$
 * 
 * Created on 10/04/2005
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
package test.zmpp.instructions;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;

/**
 * This class tests the VariableInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class InstructionVarV3Test extends InstructionTestBase {

  private ScreenModel screen;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    screen = context.mock(ScreenModel.class);
    expectStoryVersion(3);
  }

  @Test
  public void testIsBranch() {
    VariableInstruction info;
    info = new VariableInstruction(machine, OperandCount.VAR,
        VariableStaticInfo.OP_SCAN_TABLE);    
    assertTrue(info.isBranch());
  }

  @Test
  public void testIllegalOpcode() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 238");
    }});
    VariableInstruction illegal = new VariableInstruction(machine,
        OperandCount.VAR, 0xee);
    illegal.execute();
  }
  
  @Test
  public void testCall() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0, (char) 0);
      one (machine).incrementPC(5);
    }});
    VariableInstruction call_0 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call_0.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    call_0.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    call_0.setLength(5);    
    call_0.execute();
  }
  
  @Test
  public void testCallReal() {
    final char[] args = { 1, 2 };
    final char retval = 17;
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue((char) 4711));
      one (machine).call((char) 7109, (char) 4716, args, retval);
    }});
    // Real call
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1bc5));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x02));
    call.setStoreVariable((char) 0x11);
    call.setLength(5);    
    call.execute();
  }
  
  // *******************************************************************
  // ********* CALL_VS2
  // *************************
  
  @Test
  public void testCallVs2InvalidForVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 12");
    }});
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VS2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    call.setLength(5);
    call.execute();
  }

  // *******************************************************************
  // ********* STOREW
  // *************************
  
  @Test
  public void testStorew() {
    context.checking(new Expectations() {{
      one (machine).writeUnsigned16(2, (char) 0x1000);
      one (machine).incrementPC(5);
    }});
    VariableInstruction storew = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREW);    
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    storew.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1000));
    storew.setLength(5);    
    storew.execute();
  }
  
  // *******************************************************************
  // ********* STOREB
  // *************************
  
  @Test
  public void testStoreb() {
    context.checking(new Expectations() {{
      one (machine).writeUnsigned8(1, (char) 0x15);
      one (machine).incrementPC(5);
    }});
    VariableInstruction storeb = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREB);    
    storeb.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x15));
    storeb.setLength(5);
    storeb.execute();
  }
  
  @Test
  public void testPutProp() {
    context.checking(new Expectations() {{
      one (machine).setProperty(2, 24, (char) 0xffff);
      one (machine).incrementPC(5);
    }});
    VariableInstruction put_prop1 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUT_PROP);    
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 24));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0xffff));
    put_prop1.setLength(5);
    put_prop1.execute();
  }
  
  @Test
  public void testPrintChar() {
    context.checking(new Expectations() {{
      one (machine).printZsciiChar((char) 97);
      one (machine).incrementPC(5);
    }});
    VariableInstruction print_char = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_CHAR);
    print_char.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x61));
    print_char.setLength(5);    
    print_char.execute();
  }
  
  // *******************************************************************
  // ********* PRINT_NUM
  // *************************
  
  @Test
  public void testPrintNum() {
    context.checking(new Expectations() {{
      one (machine).printNumber((short) -12);
      one (machine).incrementPC(5);
    }});
    VariableInstruction print_num = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_NUM);
    print_num.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -12)));
    print_num.setLength(5);
    print_num.execute();
  }
  
  // *******************************************************************
  // ********* PUSH
  // *************************
  
  @Test
  public void testPush() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x00, (char) 0x13);
      one (machine).incrementPC(5);
    }});
    VariableInstruction push = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUSH);
    push.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x13));
    push.setLength(5);
    push.execute();
  }
  
  // *******************************************************************
  // ********* PULL
  // *************************
  
  @Test
  public void testPull() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x00); will(returnValue((char) 0x14));
      one (machine).setVariable((char) 0x13, (char) 0x14);
      one (machine).incrementPC(5);
    }});
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x13));
    pull.setLength(5);
    pull.execute();
  }

  // We check the Standard 1.1 enhancement that pull to the
  // stack will not modify the stack pointer
  @Test
  public void testPullToStack() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x00); will(returnValue((char) 0x00));
      one (machine).setStackTop((char) 0x00);
      one (machine).incrementPC(5);
    }});
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00));
    pull.setLength(5);
    pull.execute();
  }
    
  // *******************************************************************
  // ********* INPUTSTREAM
  // *************************
  
  @Test
  public void testInputStream() {
    context.checking(new Expectations() {{
      one (machine).selectInputStream(1);
      one (machine).incrementPC(5);
    }});
    VariableInstruction inputstream = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_INPUTSTREAM);
    inputstream.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    inputstream.setLength(5);
    inputstream.execute();
  }

  // *******************************************************************
  // ********* OUTPUTSTREAM
  // *************************
  
  @Test
  public void testOutputStreamDisable2() {
    context.checking(new Expectations() {{
      one (machine).selectOutputStream(2, false);
      one (machine).incrementPC(5);
    }});
    // disable
    VariableInstruction outputstream_disable = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_disable.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT,
            signedToUnsigned16((short) -2)));
    outputstream_disable.setLength(5);
    outputstream_disable.execute();
  }
  
  @Test
  public void testOutputStreamNoAction() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(5);
    }});
    // do nothing
    VariableInstruction outputstream_nothing = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_nothing.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0));
    outputstream_nothing.setLength(5);
    outputstream_nothing.execute();
  }
  
  @Test
  public void testOutputStreamEnable2() {
    context.checking(new Expectations() {{
      one (machine).selectOutputStream(2, true);
      one (machine).incrementPC(5);
    }});
    // enable
    VariableInstruction outputstream_enable = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_enable.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    outputstream_enable.setLength(5);
    outputstream_enable.execute();
  } 

  // *******************************************************************
  // ********* RANDOM
  // *************************
  
  @Test
  public void testRandom() {
    context.checking(new Expectations() {{
      one (machine).random((short) 1234); will(returnValue((char) 3));
      one (machine).setVariable((char) 0x13, (char) 3);
      one (machine).incrementPC(5);
    }});
    VariableInstruction random = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_RANDOM);
    random.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1234));
    random.setLength(5);
    random.setStoreVariable((char) 0x13);
    random.execute();
  }
  
  // *******************************************************************
  // ********* SPLIT_WINDOW
  // *************************
  
  @Test
  public void testSplitWindow() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).splitWindow(12);
    }});
    VariableInstructionMock split_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SPLIT_WINDOW);
    split_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12));
    split_window.execute();
    assertTrue(split_window.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* SET_WINDOW
  // *************************
  
  @Test
  public void testSetWindow() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setWindow(2);
    }});
    VariableInstructionMock set_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_WINDOW);
    set_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    set_window.execute();
    assertTrue(set_window.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* SET_TEXT_STYLE
  // *************************

  @Test
  public void testSetTextStyleInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 17");
    }});
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    set_text_style.execute();
  }
  
  @Test
  public void testBufferModeInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 18");
    }});
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    buffer_mode.execute();
  }
  
  // *******************************************************************
  // ********* ERASE_WINDOW
  // *************************

  @Test
  public void testEraseWindowInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 13");
    }});
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);
    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    erase_window.execute();
  }

  @Test
  public void testEraseLineInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 14");
    }});
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    erase_line.execute();
  }

  // *******************************************************************
  // ********* SET_CURSOR
  // *************************

  @Test
  public void testSetCursorInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 15");
    }});
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    set_cursor.execute();
  }

  @Test
  public void testGetCursorInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 16");
    }});
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 4711));
    get_cursor.execute();
  }

  @Test
  public void testScanTableInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 23");
    }});
    VariableInstructionMock scan_table =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SCAN_TABLE);    
    scan_table.execute();
  }

  @Test
  public void testReadCharInvalidInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 22");
    }});
    VariableInstructionMock read_char =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_READ_CHAR);    
    read_char.execute();
  }

  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  static class VariableInstructionMock extends VariableInstruction {  
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public VariableInstructionMock(Machine machine, int opcode) {
      super(machine, OperandCount.VAR, opcode);
    }
    
    @Override
    protected void nextInstruction() {
      nextInstructionCalled = true;
    }
    
    @Override
    protected void returnFromRoutine(char retval) {
      returned = true;
      returnValue = retval;
    }
    
    @Override
    protected void branchOnTest(boolean flag) {
      branchOnTestCalled = true;
      branchOnTestCondition = flag;
    }
  }
}
