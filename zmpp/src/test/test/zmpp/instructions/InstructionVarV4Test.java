/*
 * $Id: LongInstructionTest.java 524 2007-11-15 00:32:16Z weiju $
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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.vm.Instruction.OperandCount;
import org.zmpp.vm.ScreenModel;
import org.zmpp.windowing.TextCursor;

import test.zmpp.instructions.InstructionVarV3Test.VariableInstructionMock;

/**
 * Test class for VAR instructions on V4.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class InstructionVarV4Test extends InstructionTestBase {
  private ScreenModel screen;
  private TextCursor cursor;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    screen = context.mock(ScreenModel.class);
    cursor = context.mock(TextCursor.class);
    expectStoryVersion(4);
  }

  @Test
  public void testStoresValueV4() {
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

  @Test
  public void testCallVNIllegalForVersion4() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 25");
    }});
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    call.setLength(5);
    call.execute();
  }
  
  @Test
  public void testCallVN2IllegalForVersion4() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 26");
    }});
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    call.setLength(5);
    call.execute();
  }

  @Test
  public void testSetTextStyle() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setTextStyle(2);
    }});
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
    set_text_style.execute();
    assertTrue(set_text_style.nextInstructionCalled);
  }  

  @Test
  public void testBufferModeTrue() {    
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setBufferMode(true);
    }});
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }
  
  @Test
  public void testBufferModeFalse() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setBufferMode(false);
    }});
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }

  @Test
  public void testEraseWindow() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).eraseWindow(1);
    }});
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    erase_window.execute();
    assertTrue(erase_window.nextInstructionCalled);
  }

  @Test
  public void testEraseLine() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).eraseLine(1);
    }});
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    erase_line.execute();
    assertTrue(erase_line.nextInstructionCalled);
  }

  @Test
  public void testSetCursor() {
    context.checking(new Expectations() {{
      one (machine).getScreen(); will(returnValue(screen));
      one (screen).setTextCursor(1, 2, -3);
    }});
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 1));
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2));
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
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 4711));
    get_cursor.execute();
    assertTrue(get_cursor.nextInstructionCalled);
  }  

  @Test
  public void testNotIsIllegalPriorV5() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 24");
    }});
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);    
    not.execute();
  }

  @Test
  public void testTokeniseIllegalPriorV5() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: VARIABLE operand count: VAR opcode: 27");
    }});
    VariableInstructionMock tokenise =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_TOKENISE);    
    tokenise.execute();
  }
}
