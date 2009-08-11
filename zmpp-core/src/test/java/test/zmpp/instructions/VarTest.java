/*
 * Created on 10/04/2005
 * Copyright 2005-2009 by Wei-ju Wu
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
import org.zmpp.instructions.InstructionInfoDb;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VarInstruction;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;
import org.zmpp.vm.Machine;
import static org.zmpp.base.MemoryUtil.*;
import org.zmpp.windowing.ScreenModel;
import org.zmpp.windowing.TextCursor;

/**
 * This class tests the VariableInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class VarTest extends InstructionTestBase {

  private ScreenModel screen;
  private TextCursor cursor;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    screen = context.mock(ScreenModel.class);
    cursor = context.mock(TextCursor.class);
  }

  @Test
  public void testIsBranchV4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(VAR, VAR_SCAN_TABLE, 4).isBranch());
  }
  @Test
  public void testIsBranchV5() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(VAR, VAR_CHECK_ARG_COUNT, 5).isBranch());
  }
  @Test
  public void testIllegalForVersion3() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(VAR, VAR_CALL_VS2, 3));
    assertFalse(infoDb.isValid(VAR, VAR_SET_TEXT_STYLE, 3));
    assertFalse(infoDb.isValid(VAR, VAR_BUFFER_MODE, 3));
    assertFalse(infoDb.isValid(VAR, VAR_ERASE_WINDOW, 3));
    assertFalse(infoDb.isValid(VAR, VAR_ERASE_LINE, 3));
    assertFalse(infoDb.isValid(VAR, VAR_SET_CURSOR, 3));
    assertFalse(infoDb.isValid(VAR, VAR_GET_CURSOR, 3));
    assertFalse(infoDb.isValid(VAR, VAR_SCAN_TABLE, 3));
    assertFalse(infoDb.isValid(VAR, VAR_READ_CHAR, 3));
  }
  @Test
  public void testStoresValueV4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(VAR, VAR_CALL, 4).isStore());
    assertTrue(infoDb.getInfo(VAR, VAR_RANDOM, 4).isStore());
    assertTrue(infoDb.getInfo(VAR, VAR_CALL_VS2, 4).isStore());
    assertTrue(infoDb.getInfo(VAR, VAR_READ_CHAR, 4).isStore());
    assertTrue(infoDb.getInfo(VAR, VAR_SCAN_TABLE, 4).isStore());
    assertFalse(infoDb.getInfo(VAR, VAR_PRINT_CHAR, 4).isStore());
    assertFalse(infoDb.getInfo(VAR, VAR_SREAD, 4).isStore());
  }
  @Test
  public void testIllegalForVersion4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(VAR, VAR_CALL_VN, 4));
    assertFalse(infoDb.isValid(VAR, VAR_CALL_VN2, 4));
    assertFalse(infoDb.isValid(VAR, VAR_NOT, 4));
    assertFalse(infoDb.isValid(VAR, VAR_TOKENISE, 4));
  }
  @Test
  public void testStoresValueV5() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(VAR, VAR_AREAD, 5).isStore());
    assertTrue(infoDb.getInfo(VAR, VAR_NOT, 5).isStore());
    assertFalse(infoDb.getInfo(VAR, VAR_CALL_VN, 5).isStore());
    assertFalse(infoDb.getInfo(VAR, VAR_CALL_VN2, 5).isStore());
  }
  @Test
  public void testNewInVersion5() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.isValid(VAR, VAR_NOT, 5));
  }
  @Test
  public void testStoresValueV6() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(VAR, VAR_PULL, 6).isStore());
  }

  @Test
  public void testCall() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0, (char) 0);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);
    VarMock call_0 = createVarMock(VAR_CALL, new Operand[] {op1, op2});
    call_0.execute();
    assertTrue(call_0.nextInstructionCalled);
  }
  @Test
  public void testCallReal() {
    expectStoryVersion(3);
    final char[] args = { 1, 2 };
    final char retval = 17;
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(4711));
      one (machine).call((char) 7109, 4716, args, retval);
    }});
    // Real call
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1bc5);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x02);
    char storevar = 0x11;
    VarMock call = createVarMock(VAR_CALL, new Operand[] {op1, op2, op3}, storevar);
    call.execute();
  }
  
  // *******************************************************************
  // ********* STOREW
  // *************************
  @Test
  public void testStorew() {
    context.checking(new Expectations() {{
      one (machine).writeUnsigned16(2, (char) 0x1000);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op3 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1000);
    VarMock storew = createVarMock(VAR_STOREW, new Operand[] {op1, op2, op3});
    storew.execute();
    assertTrue(storew.nextInstructionCalled);
  }
  
  // *******************************************************************
  // ********* STOREB
  // *************************
  
  @Test
  public void testStoreb() {
    context.checking(new Expectations() {{
      one (machine).writeUnsigned8(1, (char) 0x15);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op3 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x15);
    VarMock storeb = createVarMock(VAR_STOREB, new Operand[] {op1, op2, op3});
    storeb.execute();
    assertTrue(storeb.nextInstructionCalled);
  }
  
  @Test
  public void testPutProp() {
    context.checking(new Expectations() {{
      one (machine).setProperty(2, 24, (char) 0xffff);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 24);
    Operand op3 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0xffff);
    VarMock put_prop1 = createVarMock(VAR_PUT_PROP, new Operand[] {op1, op2, op3});
    put_prop1.execute();
    assertTrue(put_prop1.nextInstructionCalled);
  }
  
  @Test
  public void testPrintChar() {
    context.checking(new Expectations() {{
      one (machine).printZsciiChar((char) 97);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x61);
    VarMock print_char = createVarMock(VAR_PRINT_CHAR, new Operand[] {op1});
    print_char.execute();
    assertTrue(print_char.nextInstructionCalled);
  }
  
  // *******************************************************************
  // ********* PRINT_NUM
  // *************************
  
  @Test
  public void testPrintNum() {
    context.checking(new Expectations() {{
      one (machine).printNumber((short) -12);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -12));
    VarMock print_num = createVarMock(VAR_PRINT_NUM, new Operand[] {op1});
    print_num.execute();
    assertTrue(print_num.nextInstructionCalled);
  }
  
  // *******************************************************************
  // ********* PUSH
  // *************************
  
  @Test
  public void testPush() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x00, (char) 0x13);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x13);
    VarMock push = createVarMock(VAR_PUSH, new Operand[] {op1});
    push.execute();
    assertTrue(push.nextInstructionCalled);
  }
  
  // *******************************************************************
  // ********* PULL
  // *************************
  
  @Test
  public void testPull() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x00); will(returnValue((char) 0x14));
      one (machine).setVariable((char) 0x13, (char) 0x14);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x13);
    VarMock pull = createVarMock(VAR_PULL, new Operand[] {op1});
    pull.execute();
    assertTrue(pull.nextInstructionCalled);
  }

  // We check the Standard 1.1 enhancement that pull to the
  // stack will not modify the stack pointer
  @Test
  public void testPullToStack() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x00); will(returnValue((char) 0x00));
      one (machine).setStackTop((char) 0x00);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00);
    VarMock pull = createVarMock(VAR_PULL, new Operand[] {op1});
    pull.execute();
    assertTrue(pull.nextInstructionCalled);
  }
    
  // *******************************************************************
  // ********* INPUTSTREAM
  // *************************
  
  @Test
  public void testInputStream() {
    context.checking(new Expectations() {{
      one (machine).selectInputStream(1);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    VarMock inputstream = createVarMock(VAR_INPUT_STREAM, new Operand[] {op1});
    inputstream.execute();
    assertTrue(inputstream.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* OUTPUTSTREAM
  // *************************
  
  @Test
  public void testOutputStreamDisable2() {
    context.checking(new Expectations() {{
      one (machine).selectOutputStream(2, false);
    }});
    // disable
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -2));
    VarMock outputstream_disable = createVarMock(VAR_OUTPUT_STREAM, new Operand[] {op1});
    outputstream_disable.execute();
    assertTrue(outputstream_disable.nextInstructionCalled);
  }
  
  @Test
  public void testOutputStreamNoAction() {
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);
    // do nothing
    VarMock outputstream_nothing = createVarMock(VAR_OUTPUT_STREAM, new Operand[] {op1});
    outputstream_nothing.execute();
    assertTrue(outputstream_nothing.nextInstructionCalled);
  }
  
  @Test
  public void testOutputStreamEnable2() {
    context.checking(new Expectations() {{
      one (machine).selectOutputStream(2, true);
    }});
    // enable
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    VarMock outputstream_enable = createVarMock(VAR_OUTPUT_STREAM, new Operand[] {op1});
    outputstream_enable.execute();
    assertTrue(outputstream_enable.nextInstructionCalled);
  } 
  // *******************************************************************
  // ********* RANDOM
  // *************************
  
  @Test
  public void testRandom() {
    context.checking(new Expectations() {{
      one (machine).random((short) 1234); will(returnValue((char) 3));
      one (machine).setVariable((char) 0x13, (char) 3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 1234);
    char storevar = 0x13;
    VarMock random = createVarMock(VAR_RANDOM, new Operand[] {op1}, storevar);
    random.execute();
    assertTrue(random.nextInstructionCalled);
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
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
    VarMock split_window = createVarMock(VAR_SPLIT_WINDOW, new Operand[] {op1});
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
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    VarMock set_window = createVarMock(VAR_SET_WINDOW, new Operand[] {op1});
    set_window.execute();
    assertTrue(set_window.nextInstructionCalled);
  }
  
  // *******************************************************************
  // ********* Version 4
  // *************************
  @Test
  public void testSetTextStyle() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setTextStyle(2);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    VarMock set_text_style = createVarMock(VAR_SET_TEXT_STYLE, new Operand[] {op1});
    set_text_style.execute();
    assertTrue(set_text_style.nextInstructionCalled);
  }  

  @Test
  public void testBufferModeTrue() {    
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setBufferMode(true);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    VarMock buffer_mode = createVarMock(VAR_BUFFER_MODE, new Operand[] {op1});
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }
  
  @Test
  public void testBufferModeFalse() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setBufferMode(false);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0);
    VarMock buffer_mode = createVarMock(VAR_BUFFER_MODE, new Operand[] {op1});
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }
  @Test
  public void testEraseWindow() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).eraseWindow(1);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    VarMock erase_window = createVarMock(VAR_ERASE_WINDOW, new Operand[] {op1});    
    erase_window.execute();
    assertTrue(erase_window.nextInstructionCalled);
  }

  @Test
  public void testEraseLine() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).eraseLine(1);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    VarMock erase_line = createVarMock(VAR_ERASE_LINE, new Operand[] {op1});
    erase_line.execute();
    assertTrue(erase_line.nextInstructionCalled);
  }

  @Test
  public void testSetCursor() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setTextCursor(1, 2, -3);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    Operand op2 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    VarMock set_cursor = createVarMock(VAR_SET_CURSOR, new Operand[] {op1, op2});
    set_cursor.execute();
    assertTrue(set_cursor.nextInstructionCalled);
  }  
  @Test
  public void testGetCursor() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).getTextCursor(); will(returnValue(cursor));
      one (cursor).getLine(); will(returnValue(1));
      one (cursor).getColumn(); will(returnValue(1));
      one (machine).writeUnsigned16(4711, (char) 1);
      one (machine).writeUnsigned16(4713, (char) 1);
    }});
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 4711);
    VarMock get_cursor = createVarMock(VAR_GET_CURSOR, new Operand[] {op1});
    get_cursor.execute();
    assertTrue(get_cursor.nextInstructionCalled);
  }

  // *******************************************************************
  // ********* Version 6
  // *************************
  @Test
  public void testPullV6NoUserStack() {
    expectStoryVersion(6);
    context.checking(new Expectations() {{
      one (machine).popStack((char) 0x00); will(returnValue((char) 0x14));
      one (machine).setVariable((char) 0x15, (char) 0x14);
    }});
    char storevar = 0x15;
    VarMock pull = createVarMock(VAR_PULL, new Operand[0], storevar);
    pull.execute();
    assertTrue(pull.nextInstructionCalled);
  }  
  @Test
  public void testPullV6UserStack() {
    expectStoryVersion(6);
    context.checking(new Expectations() {{
      one (machine).popStack((char) 0x1234); will(returnValue((char) 0x15));
      one (machine).setVariable((char) 0x15, (char) 0x15);
    }});
    char storevar = 0x15;
    Operand op1 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1234);
    VarMock pull = createVarMock(VAR_PULL, new Operand[] {op1}, storevar);
    pull.execute();
    assertTrue(pull.nextInstructionCalled);
  }    

  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  private VarMock createVarMock(int opcode, Operand[] operands) {
    return createVarMock(machine, opcode, operands, (char) 0);
  }
  private VarMock createVarMock(int opcode, Operand[] operands, char storevar) {
    return createVarMock(machine, opcode, operands, storevar);
  }
  static VarMock createVarMock(Machine machine, int opcode, Operand[] operands, char storevar) {
    return new VarMock(machine, opcode, operands, storevar);
  }

  static class VarMock extends VarInstruction {  
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public VarMock(Machine machine, int opcode, Operand[] operands, char storeVar) {
      super(machine, opcode, operands, storeVar, null, 5);
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
