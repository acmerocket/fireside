package test.zmpp.instructions;

import org.jmock.Mock;
import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.TextCursor;

import test.zmpp.instructions.InstructionVarV3Test.VariableInstructionMock;

public class InstructionVarV4Test extends InstructionTestBase {

  private Mock mockScreen;
  private ScreenModel screen;
  private Mock mockCursor;
  private TextCursor cursor;

  @Before
  protected void setUp() throws Exception {
    super.setUp();
    mockScreen = mock(ScreenModel.class);
    screen = (ScreenModel) mockScreen.proxy();
    mockCursor = mock(TextCursor.class);
    cursor = (TextCursor) mockCursor.proxy();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
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
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 25"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }
  
  @Test
  public void testCallVN2IllegalForVersion4() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: VARIABLE operand count: VAR opcode: 26"));
    
    VariableInstruction call = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);
    call.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0000));
    call.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    call.setLength(5);
    call.execute();
  }

  @Test
  public void testSetTextStyle() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setTextStyle").with(eq(2));
    VariableInstructionMock set_text_style =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_TEXT_STYLE);
    set_text_style.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_text_style.execute();
    assertTrue(set_text_style.nextInstructionCalled);
  }  

  @Test
  public void testBufferModeTrue() {    
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setBufferMode").with(eq(true));
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }
  
  @Test
  public void testBufferModeFalse() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setBufferMode").with(eq(false));
    VariableInstructionMock buffer_mode =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_BUFFER_MODE);
    
    buffer_mode.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    buffer_mode.execute();
    assertTrue(buffer_mode.nextInstructionCalled);
  }

  @Test
  public void testEraseWindow() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("eraseWindow").with(eq(1));
        
    VariableInstructionMock erase_window =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_WINDOW);
    
    erase_window.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_window.execute();
    assertTrue(erase_window.nextInstructionCalled);
  }

  @Test
  public void testEraseLine() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("eraseLine").with(eq(1));
    VariableInstructionMock erase_line =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_ERASE_LINE);
    
    erase_line.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    erase_line.execute();
    assertTrue(erase_line.nextInstructionCalled);
  }

  @Test
  public void testSetCursor() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockScreen.expects(once()).method("setTextCursor").with(eq(1), eq(2), eq(-3));
        
    VariableInstructionMock set_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_SET_CURSOR);
    
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    set_cursor.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2));
    set_cursor.execute();
    assertTrue(set_cursor.nextInstructionCalled);
  }  

  public void testGetCursor() {
    mockMachine.expects(once()).method("getScreen").will(returnValue(screen));
    mockMachine.expects(atLeastOnce()).method("getGameData")
    	.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockScreen.expects(once()).method("getTextCursor").will(returnValue(cursor));
    mockCursor.expects(once()).method("getLine").will(returnValue(1));
    mockCursor.expects(once()).method("getColumn").will(returnValue(1));
    mockMemory.expects(atLeastOnce()).method("writeShort").withAnyArguments();
        
    VariableInstructionMock get_cursor =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_GET_CURSOR);
    
    get_cursor.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    get_cursor.execute();
    assertTrue(get_cursor.nextInstructionCalled);
  }  

  public void testNotIsIllegalPriorV5() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 24"));    
    
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);    
    not.execute();
  }

  public void testTokeniseIllegalPriorV5() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: VARIABLE operand count: VAR opcode: 27"));    
    
    VariableInstructionMock tokenise =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_TOKENISE);    
    tokenise.execute();
  }
}
