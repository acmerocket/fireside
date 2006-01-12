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
public class Short0InstructionTest extends InstructionTestBase {

  public void testIsBranchV3() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
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
  
  public void testIsBranchV4() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);    
    assertFalse(info.isBranch());
    info.setOpcode(Short0StaticInfo.OP_RESTORE);
    assertFalse(info.isBranch());
  }
  
  public void testIllegalOpcode() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short0InstructionMock illegal = createInstructionMock(0xee);
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C0OP opcode: 238"));
    illegal.execute();
  }
  
  public void testStoresResultV4() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    assertTrue(info.storesResult());
    info = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    assertTrue(info.storesResult());    
  }
  
  public void testNotStoresResultInV3() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    assertFalse(info.storesResult());
    info = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    assertFalse(info.storesResult());    
  }
  
  // ***********************************************************************
  // ********* RFALSE
  // ******************************************
  
  public void testRtrue() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock rtrue = createInstructionMock(Short0StaticInfo.OP_RTRUE);
    rtrue.execute();
    assertTrue(rtrue.returned);
    assertEquals(AbstractInstruction.TRUE, rtrue.returnValue);
  }
  
  // ***********************************************************************
  // ********* RTRUE
  // ******************************************
  
  public void testRfalse() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock rfalse = createInstructionMock(Short0StaticInfo.OP_RFALSE);    
    rfalse.execute();
    assertTrue(rfalse.returned);
    assertEquals(AbstractInstruction.FALSE, rfalse.returnValue);
  }

  // ***********************************************************************
  // ********* NOP
  // ******************************************
  
  public void testNop() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock nop = createInstructionMock(Short0StaticInfo.OP_NOP);
    nop.execute();
    assertTrue(nop.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* SAVE
  // ******************************************
  
  public void testSaveSuccessV3() {
    
    Short0InstructionMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(true));
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertTrue(save.branchOnTestCondition);
  }
  
  public void testSaveFailV3() {
    
    Short0InstructionMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(false));
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    save.execute();
    assertTrue(save.branchOnTestCalled);
    assertFalse(save.branchOnTestCondition);
  }

  public void testSaveSuccessV4() {
    
    Short0InstructionMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(true));
    mockMachine.expects(once()).method("setVariable").with(eq(0), eq((short) 1));
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    save.execute();
    assertTrue(save.nextInstructionCalled);
  }

  public void testSaveIllegalInV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 5"));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    Short0Instruction save = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    save.execute();
  }
    
  // ***********************************************************************
  // ********* RESTORE
  // ******************************************
  
  public void testRestoreSuccessV3() {
    
    PortableGameState gamestate = new PortableGameState();
    Short0InstructionMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("restore").will(returnValue(gamestate));
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  public void testRestoreFailV3() {
    
    Short0InstructionMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("restore").will(returnValue(null));
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  public void testRestoreSuccessV4() {
    
    PortableGameState gamestate = new PortableGameState();
    Short0InstructionMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("restore").will(returnValue(gamestate));
    mockMachine.expects(once()).method("setVariable").with(eq(5), eq((short) 2));
    
    // Store variable
    mockMachine.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(0)).will(returnValue((short) 5));
    
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  public void testRestoreFailV4() {
    
    Short0InstructionMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("restore").will(returnValue(null));
    mockMachine.expects(once()).method("setVariable").with(eq(0), eq((short) 0));
    
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }
  
  public void testRestoreIllegalInV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 6"));
    Short0Instruction restore = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    restore.execute();
  }
    
  // ***********************************************************************
  // ********* RESTART
  // ******************************************
  
  public void testRestart() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock restart = createInstructionMock(Short0StaticInfo.OP_RESTART);
    mockMachine.expects(once()).method("restart");
    restart.execute();
  }
  
  // ***********************************************************************
  // ********* QUIT
  // ******************************************
  
  public void testQuit() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock quit = createInstructionMock(Short0StaticInfo.OP_QUIT);  
    mockMachine.expects(once()).method("quit");
    quit.execute();
  }
  
  // ***********************************************************************
  // ********* NEW_LINE
  // ******************************************
  
  public void testNewLine() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock newline = createInstructionMock(Short0StaticInfo.OP_NEW_LINE);
    mockMachine.expects(once()).method("newline");
    newline.execute();
    assertTrue(newline.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* RET_POPPED
  // ******************************************
  
  public void testRetPopped() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock ret_popped = createInstructionMock(Short0StaticInfo.OP_RET_POPPED);
    mockMachine.expects(once()).method("getVariable").with(eq(0)).will(returnValue((short)15));
    ret_popped.execute();    
    assertTrue(ret_popped.returned);
    assertEquals(15, ret_popped.returnValue);
  }
  
  // ***********************************************************************
  // ********* POP
  // ******************************************
  
  public void testPop() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock pop = createInstructionMock(Short0StaticInfo.OP_POP);
    mockMachine.expects(once()).method("getVariable").with(eq(0)).will(returnValue((short)42));
    pop.execute();
    assertTrue(pop.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* VERIFY
  // ******************************************
  
  public void testVerifyTrue() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    mockMachine.expects(once()).method("hasValidChecksum").will(returnValue(true));    
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertTrue(verify.branchOnTestCondition);
  }
  
  public void testVerifyFalse() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    Short0InstructionMock verify = createInstructionMock(Short0StaticInfo.OP_VERIFY);
    mockMachine.expects(once()).method("hasValidChecksum").will(returnValue(false));    
    verify.execute();
    assertTrue(verify.branchOnTestCalled);
    assertFalse(verify.branchOnTestCondition);
  }
  
  public void testShowStatusVersion3() {
    
    Short0InstructionMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("updateStatusLine");
    
    showstatus.execute();
    assertTrue(showstatus.nextInstructionCalled);
  }

  public void testShowStatusVersion4IsIllegal() {
    
    Short0InstructionMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 12"));
    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    showstatus.execute();
  }

  // *******************************************************************
  // ********* GET_OP_NAME
  // *************************
  
  public void testGetOpName() {

    Short0StaticInfo info = Short0StaticInfo.getInstance();
    
    for (int i = Short0StaticInfo.OP_RTRUE; i <= Short0StaticInfo.OP_VERIFY; i++) {
      
      assertNotNull(info.getOpName(i, 3));
    }
    assertNotNull(info.getOpName(1234, 3));
  }
  
  // ***********************************************************************
  // ********* Private section
  // ******************************************
  
  class Short0InstructionMock extends Short0Instruction {
  
    
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public Short0InstructionMock(Machine machine, int opcode) {
      super(machine, opcode);
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
  
  private Short0InstructionMock createInstructionMock(int opcode) {
    
    Short0InstructionMock result = new Short0InstructionMock(machine, opcode);
    result.setLength(1);
    
    return result;
  }  
}