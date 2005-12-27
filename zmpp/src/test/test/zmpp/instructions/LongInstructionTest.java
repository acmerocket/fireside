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

import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.Machine;

/**
 * This class tests the LongInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class LongInstructionTest extends InstructionTestBase {

  public void testIsBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    
    LongInstruction info;
    info = new LongInstruction(machine, LongStaticInfo.OP_JE);
    assertTrue(info.isBranch());    
    info.setOpcode(LongStaticInfo.OP_JL);
    assertTrue(info.isBranch());    
    info.setOpcode(LongStaticInfo.OP_JG);
    assertTrue(info.isBranch());
    info.setOpcode(LongStaticInfo.OP_DEC_CHK);
    assertTrue(info.isBranch());
    info.setOpcode(LongStaticInfo.OP_INC_CHK);
    assertTrue(info.isBranch());
    info.setOpcode(LongStaticInfo.OP_JIN);
    assertTrue(info.isBranch());
    info.setOpcode(LongStaticInfo.OP_TEST);
    assertTrue(info.isBranch());
    info.setOpcode(LongStaticInfo.OP_TEST_ATTR);
    assertTrue(info.isBranch());
    
    // no branch
    info.setOpcode(LongStaticInfo.OP_ADD);
    assertFalse(info.isBranch());
  }
  
  public void testStoresResultV4() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));

    LongInstruction info;
    info = new LongInstruction(machine, LongStaticInfo.OP_OR);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_AND);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_LOADW);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_LOADB);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_PROP);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_PROP_ADDR);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_NEXT_PROP);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_ADD);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_SUB);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_MUL);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_DIV);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_MOD);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_CALL_2S);
    assertTrue(info.storesResult());

    // no store
    info.setOpcode(LongStaticInfo.OP_JG);
    assertFalse(info.storesResult());
  }
  
  public void testStoresResultV5() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    LongInstruction info = new LongInstruction(machine, LongStaticInfo.OP_CALL_2N);
    assertFalse(info.storesResult());
  }      
  
  public void testIllegalOpcode() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstruction illegal = createInstructionMock(0xee,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: LONG operand count: C2OP opcode: 238"        
        ));
    illegal.execute();
  }

  public void testCall2sVersion4() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4611));
    short[] args = { 2 };
    short returnvalue = 0;
    mockMachine.expects(once()).method("call").with(eq(1), eq(4616), eq(args), eq(returnvalue));
    
    LongInstruction call2s = createInstructionMock(LongStaticInfo.OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2s.execute();
  }
  
  public void testCall2SIllegalInVersion3() {

    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));
    
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: LONG operand count: C2OP opcode: 25"        
        ));
    LongInstruction call2s = createInstructionMock(LongStaticInfo.OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2s.execute();
  }
  
  // ***********************************************************************
  // ********* CALL_2N
  // ******************************************
  
  public void testCall2nIllegalInVersion4() {

    mockMachine.expects(once()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(once()).method("getVersion").will(returnValue(4));
    
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: LONG operand count: C2OP opcode: 26"        
        ));
    LongInstruction call2n = createInstructionMock(LongStaticInfo.OP_CALL_2N,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2n.execute();
  }
  
  
  // ***********************************************************************
  // ********* JE
  // ******************************************

  // According to Z-machine standard 1.1, one operand is not allowed
  public void testJe1Operand() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock je1 = createInstructionMockVarOps(LongStaticInfo.OP_JE);
    je1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) 0x01));
    mockMachine.expects(once()).method("halt").with(eq(
        "je expects at least two operands, only one provided"));
    je1.execute();
  }
  
  public void testJe3Operands() {
  
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock je3 = createInstructionMockVarOps(LongStaticInfo.OP_JE);    
    je3.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01));
    je3.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x04));
    je3.addOperand(new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11));
    
    mockMachine.expects(once()).method("getVariable").with(eq(0x11)).will(returnValue((short) 1));
    je3.execute();
    
    assertTrue(je3.branchOnTestCalled);
    assertTrue(je3.branchOnTestCondition);
  }
  
  public void testJe2() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock je_nobranch = createInstructionMock(LongStaticInfo.OP_JE,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3);
    
    je_nobranch.execute();
    assertTrue(je_nobranch.branchOnTestCalled);
    assertFalse(je_nobranch.branchOnTestCondition);
    
    LongInstructionMock je_branch = createInstructionMock(LongStaticInfo.OP_JE,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3);
    
    je_branch.execute();
    assertTrue(je_branch.branchOnTestCalled);
    assertTrue(je_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* JL
  // ******************************************
  
  public void testJl() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock jl_nobranch = createInstructionMock(LongStaticInfo.OP_JL,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3);
    
    jl_nobranch.execute();
    assertTrue(jl_nobranch.branchOnTestCalled);
    assertFalse(jl_nobranch.branchOnTestCondition);

    
    LongInstructionMock jl_branch = createInstructionMock(LongStaticInfo.OP_JL,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3);
    
    jl_branch.execute();
    assertTrue(jl_branch.branchOnTestCalled);
    assertTrue(jl_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* JG
  // ******************************************
  
  public void testJg() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock jg_nobranch = createInstructionMock(LongStaticInfo.OP_JG,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5);
    jg_nobranch.execute();
    assertTrue(jg_nobranch.branchOnTestCalled);
    assertFalse(jg_nobranch.branchOnTestCondition);
    
    LongInstructionMock jg_branch = createInstructionMock(LongStaticInfo.OP_JG,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    jg_branch.execute();
    assertTrue(jg_branch.branchOnTestCalled);
    assertTrue(jg_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* JIN
  // ******************************************
  
  public void testJinNotIn() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock jin_nobranch = createInstructionMock(LongStaticInfo.OP_JIN,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5);
    
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getParent").will(returnValue(12));
    jin_nobranch.execute();
    assertTrue(jin_nobranch.branchOnTestCalled);
    assertFalse(jin_nobranch.branchOnTestCondition);
  }
  
  public void testJinIn() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock jin_branch = createInstructionMock(LongStaticInfo.OP_JIN,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 36);
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getParent").will(returnValue(36));
    
    jin_branch.execute();
    assertTrue(jin_branch.branchOnTestCalled);
    assertTrue(jin_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* DEC_CHK
  // ******************************************
  
  public void testDecChkNoBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock dec_chk_nobranch = createInstructionMock(
        LongStaticInfo.OP_DEC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5);
    mockMachine.expects(once()).method("getVariable").with(eq(0x11)).will(returnValue((short) 6));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 5));
    dec_chk_nobranch.execute();
    assertTrue(dec_chk_nobranch.branchOnTestCalled);
    assertFalse(dec_chk_nobranch.branchOnTestCondition);
  }  
  
  public void testDecChkBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));

    LongInstructionMock dec_chk_branch = createInstructionMock(
        LongStaticInfo.OP_DEC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5);
    mockMachine.expects(once()).method("getVariable").with(eq(0x11)).will(returnValue((short) 5));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 4));
    
    dec_chk_branch.execute();
    assertTrue(dec_chk_branch.branchOnTestCalled);
    assertTrue(dec_chk_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* INC_CHK
  // ******************************************
  
  public void testIncChkNoBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock inc_chk_nobranch = createInstructionMock(
        LongStaticInfo.OP_INC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 7);
    mockMachine.expects(once()).method("getVariable").with(eq(0x11)).will(returnValue((short) 6));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 7));

    inc_chk_nobranch.execute();
    assertTrue(inc_chk_nobranch.branchOnTestCalled);
    assertFalse(inc_chk_nobranch.branchOnTestCondition);
  }  
  
  public void testIncChkBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock inc_chk_branch = createInstructionMock(
        LongStaticInfo.OP_INC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 5);
    mockMachine.expects(once()).method("getVariable").with(eq(0x11)).will(returnValue((short) 5));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 6));
    inc_chk_branch.execute();
    assertTrue(inc_chk_branch.branchOnTestCalled);
    assertTrue(inc_chk_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* TEST
  // ******************************************
  
  public void testTestNoBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock test_nobranch = createInstructionMock(LongStaticInfo.OP_TEST,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x7c,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x03);
    test_nobranch.execute();
    assertTrue(test_nobranch.branchOnTestCalled);
    assertFalse(test_nobranch.branchOnTestCondition);
  }
  
  public void testTestBranch() {

    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock test_branch = createInstructionMock(LongStaticInfo.OP_TEST,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x7c,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x14);
    test_branch.execute();
    assertTrue(test_branch.branchOnTestCalled);
    assertTrue(test_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* OR
  // ******************************************
  
  public void testOr() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock or = createInstructionMock(LongStaticInfo.OP_OR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0xff00);
    or.setStoreVariable((short) 0x12);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0xffff));
    or.execute();
    assertTrue(or.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* AND
  // ******************************************
  
  public void testAnd() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock and = createInstructionMock(LongStaticInfo.OP_AND,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0xff00);
    and.setStoreVariable((short) 0x12);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0x0000));
    and.execute();
    assertTrue(and.nextInstructionCalled);    
  }

  // ***********************************************************************
  // ********* ADD
  // ******************************************
  
  public void testAdd() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock add = createInstructionMock(LongStaticInfo.OP_ADD,
        Operand.TYPENUM_LARGE_CONSTANT, (short) -1,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 3);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) 2));
    add.setStoreVariable((short) 0x12);
    add.execute();
    assertTrue(add.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* SUB
  // ******************************************
  
  public void testSub() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock sub = createInstructionMock(LongStaticInfo.OP_SUB,
        Operand.TYPENUM_LARGE_CONSTANT, (short) -1,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 3);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) -4));
    sub.setStoreVariable((short) 0x12);
    sub.execute();
    assertTrue(sub.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* MUL
  // ******************************************
  
  public void testMul() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock mul = createInstructionMock(LongStaticInfo.OP_MUL,
        Operand.TYPENUM_LARGE_CONSTANT, (short) -4,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 3);
    mul.setStoreVariable((short) 0x12);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) -12));
    mul.execute();
    assertTrue(mul.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* DIV
  // ******************************************
  
  public void testDiv() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock div = createInstructionMock(LongStaticInfo.OP_DIV,
        Operand.TYPENUM_LARGE_CONSTANT, (short) -7,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 3);
    div.setStoreVariable((short) 0x12);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) -2));
    div.execute();
    assertTrue(div.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* MOD
  // ******************************************
  
  public void testMod() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock mod = createInstructionMock(LongStaticInfo.OP_MOD,
        Operand.TYPENUM_LARGE_CONSTANT, (short) -7,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 3);
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) -1));
    mod.setStoreVariable((short) 0x12);
    mod.execute();
    assertTrue(mod.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* TEST_ATTR
  // ******************************************
  
  public void testTestAttributeNoBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock test_attr_nobranch = createInstructionMock(
        LongStaticInfo.OP_TEST_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 2);
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isAttributeSet").with(eq(2)).will(returnValue(false));    
    test_attr_nobranch.execute();
    assertTrue(test_attr_nobranch.branchOnTestCalled);
    assertFalse(test_attr_nobranch.branchOnTestCondition);
  }

  public void testTestAttributeBranch() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock test_attr_branch = createInstructionMock(
        LongStaticInfo.OP_TEST_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 2);
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isAttributeSet").with(eq(2)).will(returnValue(true));    
    
    test_attr_branch.execute();
    assertTrue(test_attr_branch.branchOnTestCalled);
    assertTrue(test_attr_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* SET_ATTR
  // ******************************************
  
  public void testSetAttr() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock set_attr = createInstructionMock(
        LongStaticInfo.OP_SET_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 2);
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("setAttribute").with(eq(2));    
    set_attr.execute();    
    assertTrue(set_attr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CLEAR_ATTR
  // ******************************************
  
  public void testClearAttr() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock clear_attr = createInstructionMock(
        LongStaticInfo.OP_CLEAR_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 2);
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("clearAttribute").with(eq(2));
    clear_attr.execute();
    assertTrue(clear_attr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* STORE
  // ******************************************
  
  public void testStore() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock store = createInstructionMock(LongStaticInfo.OP_STORE,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 42);
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 42));
    store.execute();
    assertTrue(store.nextInstructionCalled);
  }
  
  // see standard 1.1
  public void testStoreOnStack() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock store = createInstructionMock(LongStaticInfo.OP_STORE,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 0x00,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 42);
    mockMachine.expects(once()).method("setStackTopElement").with(eq((short) 42));
    store.execute();
    assertTrue(store.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* LOADW
  // ******************************************
  
  public void testLoadw() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock loadw = createInstructionMock(LongStaticInfo.OP_LOADW,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1);
    loadw.setStoreVariable((short) 0x11);
    
    mockMachine.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockMemAccess.expects(once()).method("readShort").with(eq(0x0010 + 2)).will(returnValue((short) 123));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short)123));
    loadw.execute();
    assertTrue(loadw.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* LOADB
  // ******************************************
  
  // To be clarified: read unsigned or signed ? 
  public void testLoadb() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    LongInstructionMock loadb = createInstructionMock(LongStaticInfo.OP_LOADB,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (byte) 1);
    loadb.setStoreVariable((short) 0x11);
    mockMachine.expects(once()).method("getMemoryAccess").will(returnValue(memoryAccess));
    mockMemAccess.expects(once()).method("readUnsignedByte").with(eq(0x0010 + 1)).will(returnValue((short) 42));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 42));
    
    loadb.execute();
    assertTrue(loadb.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* GET_PROP
  // ******************************************
  
  public void testGetProp2Bytes() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getPropertySize").with(eq(18)).will(returnValue(2));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(18)).will(returnValue(true));
    mockZObject.expects(once()).method("getPropertyByte").with(eq(18), eq(0)).will(returnValue((byte) 0x01));
    mockZObject.expects(once()).method("getPropertyByte").with(eq(18), eq(1)).will(returnValue((byte) 0xee));
    mockMachine.expects(once()).method("setVariable").with(eq(17), eq((short) 494));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4717));
    
    // Two-byte property, Object 1, property 18
    LongInstruction get_prop_two = createInstruction(
        LongStaticInfo.OP_GET_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 18, 6);
    get_prop_two.setStoreVariable((short) 0x11);    
    get_prop_two.execute();    
  }
  
  public void testGetPropOneByte() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(2)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getPropertySize").with(eq(22)).will(returnValue(1));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(22)).will(returnValue(true));
    mockZObject.expects(once()).method("getPropertyByte").with(eq(22), eq(0)).will(returnValue((byte) 0x77));
    mockMachine.expects(once()).method("setVariable").with(eq(17), eq((short) 0x77));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4717));
    
    // One-byte property, Object 2, property 22
    LongInstruction get_prop_one = createInstruction(
        LongStaticInfo.OP_GET_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 22, 6);
    get_prop_one.setStoreVariable((short) 0x11);    
    get_prop_one.execute();
  }
  
  public void testGetPropFail() {

    // No defined property, take default, Object 1, property 1
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getPropertySize").with(eq(1)).will(returnValue(0));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(1)).will(returnValue(false));
    mockObjectTree.expects(once()).method("getPropertyDefault").with(eq(1)).will(returnValue((short) 2));
    mockMachine.expects(once()).method("setVariable").with(eq(17), eq((short) 0x02));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4717));
    
    LongInstruction get_prop_default = createInstruction(
        LongStaticInfo.OP_GET_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1, 6);
    get_prop_default.setStoreVariable((short) 0x11);
    get_prop_default.execute();
  }

  // ***********************************************************************
  // ********* INSERT_OBJ
  // ******************************************
  
  public void testInsertObj() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Make Object 7 a child of Object 2
    LongInstructionMock insert_obj = createInstructionMock(
        LongStaticInfo.OP_INSERT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 7,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("insertObject").with(eq(2), eq(7));
    insert_obj.execute();
    assertTrue(insert_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PROP_ADDR
  // ******************************************
  
  public void testGetPropAddr() {
   
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(18)).will(returnValue(true));
    mockZObject.expects(once()).method("getPropertyAddress").with(eq(18)).will(returnValue(0x0a55));
    mockMachine.expects(once()).method("setVariable").with(eq(17), eq((short) 0x0a55));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4717));

    LongInstruction get_prop_addr_exists = createInstruction(
        LongStaticInfo.OP_GET_PROP_ADDR,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 18, 6);

    get_prop_addr_exists.setStoreVariable((short) 0x11);    
    get_prop_addr_exists.execute();
  }
  
  public void testGetPropAddrNotExists() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(2)).will(returnValue(false));
    mockMachine.expects(once()).method("setVariable").with(eq(17), eq((short) 0x00));
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(4711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(4717));

    LongInstruction get_prop_addr_notexists = createInstruction(
        LongStaticInfo.OP_GET_PROP_ADDR,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2, 6);

    get_prop_addr_notexists.setStoreVariable((short) 0x11);    
    get_prop_addr_notexists.execute();
  }
  // ***********************************************************************
  // ********* GET_NEXT_PROP
  // ******************************************
  
  public void testGetNextProp() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getNextProperty").with(eq(12)).will(returnValue(15));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(12)).will(returnValue(true));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 15));

    LongInstructionMock get_next_prop = createInstructionMock(
        LongStaticInfo.OP_GET_NEXT_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 12);
    
    get_next_prop.setStoreVariable((short) 0x11);    
    get_next_prop.execute();
    
    assertTrue(get_next_prop.nextInstructionCalled);
  }
  
  // get_next_prop with 0 returns the first property
  public void testGetNextPropWith0() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getNextProperty").with(eq(0)).will(returnValue(15));
    mockMachine.expects(once()).method("setVariable").with(eq(0x11), eq((short) 15));

    LongInstructionMock get_next_prop = createInstructionMock(
        LongStaticInfo.OP_GET_NEXT_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0);
   
    get_next_prop.setStoreVariable((short) 0x11);    
    get_next_prop.execute();
    
    assertTrue(get_next_prop.nextInstructionCalled);
  }
  
  // get_next_prop with unavailable property
  public void testGetNextPropNotAvailable() {
    
    mockMachine.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(1)).will(returnValue(zobject));
    mockZObject.expects(once()).method("isPropertyAvailable").with(eq(13)).will(returnValue(false));
    mockMachine.expects(once()).method("halt").with(eq("the property [13] of object [1] does not exist"));

    LongInstructionMock get_next_prop = createInstructionMock(
        LongStaticInfo.OP_GET_NEXT_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 13);
    
    get_next_prop.setStoreVariable((short) 0x11);    
    get_next_prop.execute();        
  }
  
  // *******************************************************************
  // ********* GET_OP_NAME
  // *************************
  
  public void testGetOpName() {

    LongStaticInfo info = LongStaticInfo.getInstance();
    for (int i = LongStaticInfo.OP_JE; i <= LongStaticInfo.OP_CALL_2S; i++) {
      
      assertNotNull(info.getOpName(i, 3));      
    }    
    assertNotNull(info.getOpName(1234, 3));      
  }
  
  // **********************************************************************
  // ******* Private
  // *******************************
  class LongInstructionMock extends LongInstruction {
  
    
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public LongInstructionMock(Machine machine, int opcode) {
      super(machine, opcode);
    }
    
    public LongInstructionMock(Machine machine, OperandCount operandCount,
        int opcode) {
      
      super(machine, operandCount, opcode);
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
    
  private LongInstructionMock createInstructionMock(int opcode, int typenum1,
      short value1, int typenum2, short value2) {
    
    LongInstructionMock result = new LongInstructionMock(machine, opcode);
    result.addOperand(new Operand(typenum1, value1));
    result.addOperand(new Operand(typenum2, value2));
    result.setLength(5);
    return result;
  }
  
  private LongInstructionMock createInstructionMockVarOps(int opcode) {
    
    LongInstructionMock result = new LongInstructionMock(machine, OperandCount.VAR, opcode);
    result.setLength(5);
    return result;
  }  
  
  private LongInstruction createInstruction(int opcode, int typenum1,
      short value1, int typenum2, short value2, int length) {
    
    LongInstruction result = new LongInstruction(machine, opcode);
    result.addOperand(new Operand(typenum1, value1));
    result.addOperand(new Operand(typenum2, value2));
    result.setLength(length);
    return result;
  }    
}
