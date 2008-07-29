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

import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.zmpp.instructions.C0OpInstruction;
import org.zmpp.instructions.InstructionInfoDb;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.Instruction;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.RoutineContext;

/**
 * This class tests the dynamic and static aspects of C0OP instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class C0OpTest extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
  }
  
  @Test
  public void testIsBranchV3() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C0OP, C0OP_SAVE, 3).isBranch());
    assertTrue(infoDb.getInfo(C0OP, C0OP_RESTORE, 3).isBranch());
    assertTrue(infoDb.getInfo(C0OP, C0OP_VERIFY, 3).isBranch());
    assertFalse(infoDb.getInfo(C0OP, C0OP_NEW_LINE, 3).isBranch());
  }

  @Test
  public void testStoresResultV3() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.getInfo(C0OP, C0OP_SAVE, 3).isStore());
    assertFalse(infoDb.getInfo(C0OP, C0OP_RESTORE, 3).isStore());
  }

  @Test
  public void testIsBranchV4() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.getInfo(C0OP, C0OP_SAVE, 4).isBranch());
    assertFalse(infoDb.getInfo(C0OP, C0OP_RESTORE, 4).isBranch());
  }  

  @Test
  public void testStoresResultV4() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C0OP, C0OP_SAVE, 4).isStore());
    assertTrue(infoDb.getInfo(C0OP, C0OP_RESTORE, 4).isStore());
  }

  @Test
  public void testIllegalInV4() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(C0OP, C0OP_SHOW_STATUS, 4));
  }  

  @Test
  public void testStoresResultV5() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C0OP, C0OP_POP, 5).isStore());
  }
  
  @Test
  public void testIllegalInV5() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(C0OP, C0OP_SAVE, 5));
    assertFalse(infoDb.isValid(C0OP, C0OP_RESTORE, 5));
    assertFalse(infoDb.isValid(C0OP, C0OP_SHOW_STATUS, 5));
  }

  // ***********************************************************************
  // ********* RFALSE
  // ******************************************
  
  @Test
  public void testRtrue() {
    C0OpMock rtrue = createInstructionMock(C0OP_RTRUE);
    rtrue.execute();
    assertTrue(rtrue.returned);
    assertEquals(Instruction.TRUE, rtrue.returnValue);
  }
  
  // ***********************************************************************
  // ********* RTRUE
  // ******************************************
  
  @Test
  public void testRfalse() {
    C0OpMock rfalse = createInstructionMock(C0OP_RFALSE);    
    rfalse.execute();
    assertTrue(rfalse.returned);
    assertEquals(Instruction.FALSE, rfalse.returnValue);
  }

  // ***********************************************************************
  // ********* NOP
  // ******************************************
  
  @Test
  public void testNop() {
    C0OpMock nop = createInstructionMock(C0OP_NOP);
    nop.execute();
    assertTrue(nop.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* SAVE
  // ******************************************
  
  @Test
  public void testSaveSuccess() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(char.class))); will(returnValue(true));
    }});
    C0OpMock save = createInstructionMock(C0OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertTrue(save.branchOnTestCondition);
  }
  
  @Test
  public void testSaveFail() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(char.class))); will(returnValue(false));
    }});
    C0OpMock save = createInstructionMock(C0OP_SAVE);
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertFalse(save.branchOnTestCondition);
  }

  @Test
  public void testRestoreSuccess() {    
    expectStoryVersion(3);
    final PortableGameState gamestate = new PortableGameState();
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(gamestate));
    }});
    C0OpMock restore = createInstructionMock(C0OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFail() {    
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(null));
    }});
    C0OpMock restore = createInstructionMock(C0OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  @Test
  public void testRestart() {
    context.checking(new Expectations() {{
      one (machine).restart();
    }});
    C0OpMock restart = createInstructionMock(C0OP_RESTART);
    restart.execute();
  }
  
  @Test
  public void testQuit() {
    context.checking(new Expectations() {{
      one (machine).quit();
    }});
    C0OpMock quit = createInstructionMock(C0OP_QUIT);  
    quit.execute();
  }
  
  @Test
  public void testNewLine() {
    context.checking(new Expectations() {{
      one (machine).newline();
    }});
    C0OpMock newline = createInstructionMock(C0OP_NEW_LINE);
    newline.execute();
    assertTrue(newline.nextInstructionCalled);
  }
  
  @Test
  public void testRetPopped() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0); will(returnValue((char) 15));
    }});
    C0OpMock ret_popped = createInstructionMock(C0OP_RET_POPPED);
    ret_popped.execute();    
    assertTrue(ret_popped.returned);
    assertEquals(15, ret_popped.returnValue);
  }
  
  @Test
  public void testPop() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0); will(returnValue((char) 42));
    }});
    C0OpMock pop = createInstructionMock(C0OP_POP);
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
    C0OpMock verify = createInstructionMock(C0OP_VERIFY);
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertTrue(verify.branchOnTestCondition);
  }
  
  @Test
  public void testVerifyFalse() {
    context.checking(new Expectations() {{
      one (machine).hasValidChecksum(); will(returnValue(false));
    }});
    C0OpMock verify = createInstructionMock(C0OP_VERIFY);
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertFalse(verify.branchOnTestCondition);
  }
  
  @Test
  public void testShowStatus() {
    context.checking(new Expectations() {{
      one (machine).updateStatusLine();
    }});
    C0OpMock showstatus = createInstructionMock(C0OP_SHOW_STATUS);    
    showstatus.execute();
    assertTrue(showstatus.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* Version 4
  // ******************************************

  @Test
  public void testSaveSuccessV4() {
    expectStoryVersion(4);
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(char.class))); will(returnValue(true));
      one (machine).setVariable((char) 0, (char) 1);
    }});
    C0OpMock save = createInstructionMock(C0OP_SAVE);
    save.execute();
    assertTrue(save.nextInstructionCalled);
  }

  @Test
  public void testRestoreSuccessV4() {
    expectStoryVersion(4);
    final PortableGameState gamestate = new PortableGameState();
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(gamestate));
      // Store variable
      one (machine).setVariable((char) 5, (char) 2);
      one (machine).readUnsigned8(0); will(returnValue((char) 5));
    }});
    C0OpMock restore = createInstructionMock(C0OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFailV4() {
    expectStoryVersion(4);
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(null));
      one (machine).setVariable((char) 0, (char) 0);
    }});
    C0OpMock restore = createInstructionMock(C0OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* Version 5
  // ******************************************


  @Test
  public void testCatch() {
    expectStoryVersion(5);
    final List<RoutineContext> routineContexts = new ArrayList<RoutineContext>();
    routineContexts.add(new RoutineContext(1));
    routineContexts.add(new RoutineContext(0));
    routineContexts.add(new RoutineContext(2));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(routineContexts));
      one (machine).setVariable((char) 0x12, (char) 2);
    }});
    C0OpMock zcatch = createInstructionMock(C0OP_POP, (char) 0x12);    
    zcatch.execute();
    assertTrue(zcatch.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* Printing
  // ******************************************
  
  @Test
  public void testPrint() {    
    context.checking(new Expectations() {{
      one (machine).print("Hallo");
    }});
    C0OpMock print = createInstructionMock(C0OP_PRINT, "Hallo");
    print.execute();
    assertTrue(print.nextInstructionCalled);
  }
  
  @Test
  public void testPrintRet() {
    context.checking(new Expectations() {{
      one (machine).print("HalloRet");
      one (machine).newline();
    }});
    C0OpMock print_ret = createInstructionMock(C0OP_PRINT_RET, "HalloRet");
    print_ret.execute();
    assertTrue(print_ret.returned);
    assertEquals((char) 1, print_ret.returnValue);
  }  

  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  public static class C0OpMock extends C0OpInstruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public C0OpMock(Machine machine, int opcode) {
      super(machine, opcode, new Operand[0], null, (char) 0, null, 0);
    }
    public C0OpMock(Machine machine, int opcode, String str) {
      super(machine, opcode, new Operand[0], str, (char) 0, null, 11);
    }
    public C0OpMock(Machine machine, int opcode, Operand[] operands) {
      super(machine, opcode, operands, null, (char) 0, null, 0);
    }
    public C0OpMock(Machine machine, int opcode, char storeVar) {
      super(machine, opcode, new Operand[0], null, storeVar, null, 0);
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
  
  private C0OpMock createInstructionMock(int opcode) {
    return createInstructionMock(machine, opcode);
  }
  public C0OpMock createInstructionMock(int opcode, char storeVar) {
    C0OpMock result = new C0OpMock(machine, opcode, storeVar);
    return result;
  }  
  public C0OpMock createInstructionMock(int opcode, String str) {
    C0OpMock result = new C0OpMock(machine, opcode, str);
    return result;
  }
  
  public static C0OpMock createInstructionMock(Machine machine,
  		int opcode) {
    C0OpMock result = new C0OpMock(machine, opcode);
    return result;
  }
}
