/*
 * $Id$
 * 
 * Created on 10/04/2005
 * Copyright 2005-2004 by Wei-ju Wu
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

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.Short0Instruction;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;

/**
 * This class tests the dynamic and static aspects of C0OP instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Instruction0OpV3Test extends InstructionTestBase {

  @Override
  @Before
  protected void setUp() throws Exception {
	  super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
  }

  @Test
  public void testIsBranch() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);    
    assertTrue(info.isBranch());
    info.setOpcode(Short0StaticInfo.OP_RESTORE);
    assertTrue(info.isBranch());
    info.setOpcode(Short0StaticInfo.OP_VERIFY);
    assertTrue(info.isBranch());

    // no branch
    info.setOpcode(Short0StaticInfo.OP_NEW_LINE);
    assertFalse(info.isBranch());
  }

  @Test
  public void testIllegalOpcode() {
    Instruction0OpMock illegal = createInstructionMock(0xee);
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C0OP opcode: 238"));
    illegal.execute();
  }
  
  @Test
  public void testNotStoresResult() {
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    assertFalse(info.storesResult());
    info = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    assertFalse(info.storesResult());    
  }
  
  // ***********************************************************************
  // ********* RFALSE
  // ******************************************
  
  @Test
  public void testRtrue() {
    Instruction0OpMock rtrue = createInstructionMock(Short0StaticInfo.OP_RTRUE);
    rtrue.execute();
    assertTrue(rtrue.returned);
    assertEquals(AbstractInstruction.TRUE, rtrue.returnValue);
  }
  
  // ***********************************************************************
  // ********* RTRUE
  // ******************************************
  
  @Test
  public void testRfalse() {
    Instruction0OpMock rfalse = createInstructionMock(Short0StaticInfo.OP_RFALSE);    
    rfalse.execute();
    assertTrue(rfalse.returned);
    assertEquals(AbstractInstruction.FALSE, rfalse.returnValue);
  }

  // ***********************************************************************
  // ********* NOP
  // ******************************************
  
  @Test
  public void testNop() {
    Instruction0OpMock nop = createInstructionMock(Short0StaticInfo.OP_NOP);
    nop.execute();
    assertTrue(nop.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* SAVE
  // ******************************************
  
  @Test
  public void testSaveSuccess() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(true));    
    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertTrue(save.branchOnTestCondition);
  }
  
  @Test
  public void testSaveFail() {    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(false));
    
    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertFalse(save.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* RESTORE
  // ******************************************
  
  @Test
  public void testRestoreSuccess() {    
    PortableGameState gamestate = new PortableGameState();
    mockMachine.expects(once()).method("restore").will(returnValue(gamestate));
    
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFail() {    
    mockMachine.expects(once()).method("restore").will(returnValue(null));    
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* RESTART
  // ******************************************
  
  @Test
  public void testRestart() {
    Instruction0OpMock restart = createInstructionMock(Short0StaticInfo.OP_RESTART);
    mockMachine.expects(once()).method("restart");
    restart.execute();
  }
  
  // ***********************************************************************
  // ********* QUIT
  // ******************************************
  
  @Test
  public void testQuit() {
    Instruction0OpMock quit = createInstructionMock(Short0StaticInfo.OP_QUIT);  
    mockMachine.expects(once()).method("quit");
    quit.execute();
  }
  
  // ***********************************************************************
  // ********* NEW_LINE
  // ******************************************
  
  @Test
  public void testNewLine() {
    Instruction0OpMock newline = createInstructionMock(Short0StaticInfo.OP_NEW_LINE);
    mockMachine.expects(once()).method("newline");
    newline.execute();
    assertTrue(newline.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* RET_POPPED
  // ******************************************
  
  @Test
  public void testRetPopped() {    
    Instruction0OpMock ret_popped = createInstructionMock(Short0StaticInfo.OP_RET_POPPED);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(0)).will(returnValue((short)15));
    ret_popped.execute();    
    assertTrue(ret_popped.returned);
    assertEquals(15, ret_popped.returnValue);
  }
  
  // ***********************************************************************
  // ********* POP
  // ******************************************
  
  @Test
  public void testPop() {
    Instruction0OpMock pop = createInstructionMock(Short0StaticInfo.OP_POP);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(0)).will(returnValue((short)42));
    pop.execute();
    assertTrue(pop.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* VERIFY
  // ******************************************
  
  @Test
  public void testVerifyTrue() {
    Instruction0OpMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    mockMachine.expects(once()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(once()).method("hasValidChecksum").will(returnValue(true));    
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertTrue(verify.branchOnTestCondition);
  }
  
  @Test
  public void testVerifyFalse() {
    Instruction0OpMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    mockMachine.expects(once()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(once()).method("hasValidChecksum").will(returnValue(false));    
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertFalse(verify.branchOnTestCondition);
  }
  
  @Test
  public void testShowStatus() {    
    mockMachine.expects(once()).method("updateStatusLine");
    Instruction0OpMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);    
    showstatus.execute();
    assertTrue(showstatus.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  public static class Instruction0OpMock extends Short0Instruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public Instruction0OpMock(Machine machine, int opcode) {
      super(machine, opcode);
    }
    
    @Override
    protected void nextInstruction() {
      nextInstructionCalled = true;
    }
    
    @Override
    protected void returnFromRoutine(short retval) {
      returned = true;
      returnValue = retval;
    }
    
    @Override
    protected void branchOnTest(boolean flag) {
      branchOnTestCalled = true;
      branchOnTestCondition = flag;
    }
  }
  
  private Instruction0OpMock createInstructionMock(int opcode) {
    return createInstructionMock(machine, opcode);
  }
  
  public static Instruction0OpMock createInstructionMock(Machine machine,
  		int opcode) {
    Instruction0OpMock result = new Instruction0OpMock(machine, opcode);
    result.setLength(1);
    return result;
  }  
}
