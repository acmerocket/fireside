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

import org.jmock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.Input;
import org.zmpp.vm.Machine;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.ScreenModel;

/**
 * This class tests the VariableInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InstructionVarV3Test extends InstructionTestBase {

  private Mock mockScreen;
  private ScreenModel screen;

  @Before
  protected void setUp() throws Exception {
    super.setUp();
    mockScreen = mock(ScreenModel.class);
    screen = (ScreenModel) mockScreen.proxy();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
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
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 238"));
    VariableInstruction illegal = new VariableInstruction(machine,
        OperandCount.VAR, 0xee);
    illegal.execute();
  }
  
  @Test
  public void testCall() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 0));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction call_0 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call_0.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call_0.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call_0.setLength(5);    
    call_0.execute();
  }
  
  @Test
  public void testCallReal() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(4711));
    
    short[] args = { 1, 2 };
    short retval = 17;
    RoutineContext routineContext = new RoutineContext(1234, 2);
    mockCpu.expects(once()).method("call")
    	.with(eq(7109), eq(4716), eq(args), eq(retval))
    	.will(returnValue(routineContext));
    
    // Real call
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1bc5));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x02));
    call.setStoreVariable((short) 0x11);
    call.setLength(5);    
    call.execute();
  }
  
  // *******************************************************************
  // ********* CALL_VS2
  // *************************
  
  @Test
  public void testCallVs2InvalidForVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 12"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VS2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }

  // *******************************************************************
  // ********* STOREW
  // *************************
  
  @Test
  public void testStorew() {
    mockMachine.expects(atLeastOnce()).method("getGameData")
  		.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeShort").with(eq(2), eq((short) 0x1000)); 
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction storew = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREW);    
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    storew.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    storew.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1000));
    storew.setLength(5);    
    storew.execute();
  }
  
  // *******************************************************************
  // ********* STOREB
  // *************************
  
  @Test
  public void testStoreb() {
    mockMachine.expects(atLeastOnce()).method("getGameData")
  		.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("writeByte").with(eq(1), eq((byte) 0x15)); 
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction storeb = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_STOREB);
    
    storeb.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    storeb.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x15));
    storeb.setLength(5);
    storeb.execute();
  }
  
  @Test
  public void testPutProp() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    mockMachine.expects(once()).method("setProperty")
    	.with(eq(2), eq(24), eq(-1));
    
    VariableInstruction put_prop1 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUT_PROP);    
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 24));
    put_prop1.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xffff));
    put_prop1.setLength(5);
    put_prop1.execute();
  }
  
  @Test
  public void testPrintChar() {
    mockMachine.expects(once()).method("getOutput").will(returnValue(output));
    mockOutput.expects(once()).method("printZsciiChar").with(eq((char) 97), eq(false));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction print_char = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_CHAR);
    print_char.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x61));
    print_char.setLength(5);    
    print_char.execute();
  }
  
  // *******************************************************************
  // ********* PRINT_NUM
  // *************************
  
  @Test
  public void testPrintNum() {
    mockMachine.expects(once()).method("getOutput").will(returnValue(output));
    mockOutput.expects(once()).method("printNumber").with(eq((short) -12));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction print_num = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PRINT_NUM);
    print_num.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) -12));
    print_num.setLength(5);
    print_num.execute();
  }
  
  // *******************************************************************
  // ********* PUSH
  // *************************
  
  @Test
  public void testPush() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x00), eq((short) 0x13));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction push = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PUSH);
    push.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x13));
    push.setLength(5);
    push.execute();
  }
  
  // *******************************************************************
  // ********* PULL
  // *************************
  
  @Test
  public void testPull() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(0x00)).will(returnValue((short) 0x14));;
    mockCpu.expects(once()).method("setVariable").with(eq(0x13), eq((short) 0x14));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x13));
    pull.setLength(5);
    pull.execute();
  }

  // We check the Standard 1.1 enhancement that pull to the
  // stack will not modify the stack pointer
  @Test
  public void testPullToStack() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(0)).will(returnValue((short) 0));
    mockCpu.expects(once()).method("setStackTopElement").with(eq((short) 0));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x00));
    pull.setLength(5);
    pull.execute();
  }
    
  // *******************************************************************
  // ********* INPUTSTREAM
  // *************************
  
  @Test
  public void testInputStream() {
    Mock mockInput = mock(Input.class);
    Input input = (Input) mockInput.proxy();
    mockMachine.expects(once()).method("getInput").will(returnValue(input));
    mockInput.expects(once()).method("selectInputStream").with(eq(1));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    VariableInstruction inputstream = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_INPUTSTREAM);
    inputstream.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    inputstream.setLength(5);
    inputstream.execute();
  }

  // *******************************************************************
  // ********* OUTPUTSTREAM
  // *************************
  
  @Test
  public void testOutputStreamDisable2() {
    mockMachine.expects(once()).method("getOutput").will(returnValue(output));
    mockOutput.expects(once()).method("selectOutputStream").with(eq(2), eq(false));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    // disable
    VariableInstruction outputstream_disable = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_disable.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -2));
    outputstream_disable.setLength(5);
    outputstream_disable.execute();
  }
  
  @Test
  public void testOutputStreamNoAction() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
    // do nothing
    VariableInstruction outputstream_nothing = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_OUTPUTSTREAM);
    outputstream_nothing.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    outputstream_nothing.setLength(5);
    outputstream_nothing.execute();
  }
  
  @Test
  public void testOutputStreamEnable2() {
    mockMachine.expects(once()).method("getOutput").will(returnValue(output));
    mockOutput.expects(once()).method("selectOutputStream").with(eq(2), eq(true));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));
    
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
  
  @Test
  public void testRandom() {
    mockMachine.expects(once()).method("random").with(eq((short) 1234)).will(returnValue((short) 3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x13), eq((short) 3));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction random = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_RANDOM);
    random.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 1234));
    random.setLength(5);
    random.setStoreVariable((short) 0x13);
    random.execute();
  }
  
  // *******************************************************************
  // ********* SPLIT_WINDOW
  // *************************
  
  @Test
  public void testSplitWindow() {
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
  
  @Test
  public void testSetWindow() {
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

  @Test
  public void testSetTextStyleInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 17"));
    
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_text_style.execute();
  }
  
  @Test
  public void testBufferModeInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 18"));
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    buffer_mode.execute();
  }
  
  // *******************************************************************
  // ********* ERASE_WINDOW
  // *************************

  @Test
  public void testEraseWindowInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 13"));
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);
    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_window.execute();
  }

  
  public void testEraseLineInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 14"));
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_line.execute();
  }

  // *******************************************************************
  // ********* SET_CURSOR
  // *************************

  
  public void testSetCursorInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 15"));    
    
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    set_cursor.execute();
  }

  public void testGetCursorInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 16"));    
    
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    get_cursor.execute();
  }

  public void testScanTableInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 23"));    
    
    VariableInstructionMock scan_table =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SCAN_TABLE);    
    scan_table.execute();
  }

  public void testReadCharInvalidInVersion3() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 22"));    
    
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
