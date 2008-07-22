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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.Short0Instruction;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;

/**
 * This class tests the dynamic and static aspects of C0OP instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class Instruction0OpV3Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
    expectStoryVersion(3);
  }
  
  private Short0Instruction createInstruction(int opcode) {
    return new Short0Instruction(machine, opcode);
  }

  @Test
  public void testIsBranch() {    
    Short0Instruction instr = createInstruction(Short0StaticInfo.OP_SAVE);    
    assertTrue(instr.isBranch());
    instr.setOpcode(Short0StaticInfo.OP_RESTORE);
    assertTrue(instr.isBranch());
    instr.setOpcode(Short0StaticInfo.OP_VERIFY);
    assertTrue(instr.isBranch());

    // no branch
    instr.setOpcode(Short0StaticInfo.OP_NEW_LINE);
    assertFalse(instr.isBranch());
  }

  @Test
  public void testIllegalOpcode() {
    Instruction0OpMock illegal = createInstructionMock(0xee);
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 238");
    }});
    illegal.execute();
  }
  
  @Test
  public void testNotStoresResult() {
    Short0Instruction info = createInstruction(Short0StaticInfo.OP_SAVE);
    assertFalse(info.storesResult());
    info = createInstruction(Short0StaticInfo.OP_RESTORE);
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
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(char.class))); will(returnValue(true));
    }});
    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertTrue(save.branchOnTestCondition);
  }
  
  @Test
  public void testSaveFail() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(char.class))); will(returnValue(false));
    }});
    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertFalse(save.branchOnTestCondition);
  }

  @Test
  public void testRestoreSuccess() {    
    final PortableGameState gamestate = new PortableGameState();
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(gamestate));
    }});
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFail() {    
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(null));
    }});
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  @Test
  public void testRestart() {
    context.checking(new Expectations() {{
      one (machine).restart();
    }});
    Instruction0OpMock restart = createInstructionMock(Short0StaticInfo.OP_RESTART);
    restart.execute();
  }
  
  @Test
  public void testQuit() {
    context.checking(new Expectations() {{
      one (machine).quit();
    }});
    Instruction0OpMock quit = createInstructionMock(Short0StaticInfo.OP_QUIT);  
    quit.execute();
  }
  
  @Test
  public void testNewLine() {
    context.checking(new Expectations() {{
      one (machine).newline();
    }});
    Instruction0OpMock newline = createInstructionMock(Short0StaticInfo.OP_NEW_LINE);
    newline.execute();
    assertTrue(newline.nextInstructionCalled);
  }
  
  @Test
  public void testRetPopped() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0); will(returnValue((char) 15));
    }});
    Instruction0OpMock ret_popped = createInstructionMock(Short0StaticInfo.OP_RET_POPPED);
    ret_popped.execute();    
    assertTrue(ret_popped.returned);
    assertEquals(15, ret_popped.returnValue);
  }
  
  @Test
  public void testPop() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0); will(returnValue((char) 42));
    }});
    Instruction0OpMock pop = createInstructionMock(Short0StaticInfo.OP_POP);
    pop.execute();
    assertTrue(pop.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* VERIFY
  // ******************************************
  
  @Test
  public void testVerifyTrue() {
    context.checking(new Expectations() {{
      one (machine).hasValidChecksum(); will(returnValue(true));
    }});
    Instruction0OpMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertTrue(verify.branchOnTestCondition);
  }
  
  @Test
  public void testVerifyFalse() {
    context.checking(new Expectations() {{
      one (machine).hasValidChecksum(); will(returnValue(false));
    }});
    Instruction0OpMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertFalse(verify.branchOnTestCondition);
  }
  
  @Test
  public void testShowStatus() {
    context.checking(new Expectations() {{
      one (machine).updateStatusLine();
    }});
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
    public char returnValue;
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
