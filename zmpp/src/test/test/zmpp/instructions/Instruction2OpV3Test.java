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
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.Instruction.OperandCount;
import org.zmpp.vm.Machine;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;

/**
 * This class tests the LongInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class Instruction2OpV3Test extends InstructionTestBase {

  @Override
	@Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(3);
  }

	@Test
  public void testIsBranch() {
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

	@Test
  public void testIllegalOpcode() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: LONG operand count: C2OP opcode: 238");
    }});
    LongInstruction illegal = createInstructionMock(0xee,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    illegal.execute();
  }

	@Test
  public void testCall2SIllegalInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: LONG operand count: C2OP opcode: 25");
    }});
    LongInstruction call2s = createInstructionMock(LongStaticInfo.OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    call2s.execute();
  }

  // ***********************************************************************
  // ********* JE
  // ******************************************

  // According to Z-machine standard 1.1, one operand is not allowed
	@Test
  public void testJe1Operand() {
    context.checking(new Expectations() {{
      one (machine).halt("je expects at least two operands, only one provided");
    }});
    Instruction2OpMock je1 = createInstructionMockVarOps(LongStaticInfo.OP_JE);
    je1.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    je1.execute();
  }
  
	@Test
  public void testJe3Operands() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 1));
    }});
    Instruction2OpMock je3 = createInstructionMockVarOps(LongStaticInfo.OP_JE);    
    je3.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01));
    je3.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x04));
    je3.addOperand(new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11));
    je3.execute();    
    assertTrue(je3.branchOnTestCalled);
    assertTrue(je3.branchOnTestCondition);
  }
  
	@Test
  public void testJe2() {
    Instruction2OpMock je_nobranch = createInstructionMock(LongStaticInfo.OP_JE,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    je_nobranch.execute();
    assertTrue(je_nobranch.branchOnTestCalled);
    assertFalse(je_nobranch.branchOnTestCondition);
    
    Instruction2OpMock je_branch = createInstructionMock(LongStaticInfo.OP_JE,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    je_branch.execute();
    assertTrue(je_branch.branchOnTestCalled);
    assertTrue(je_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* JL
  // ******************************************
  
	@Test
  public void testJl() {
    Instruction2OpMock jl_nobranch = createInstructionMock(LongStaticInfo.OP_JL,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    jl_nobranch.execute();
    assertTrue(jl_nobranch.branchOnTestCalled);
    assertFalse(jl_nobranch.branchOnTestCondition);
    
    Instruction2OpMock jl_branch = createInstructionMock(LongStaticInfo.OP_JL,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);
    jl_branch.execute();
    assertTrue(jl_branch.branchOnTestCalled);
    assertTrue(jl_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* JG
  // ******************************************
  
	@Test
  public void testJg() {
    Instruction2OpMock jg_nobranch = createInstructionMock(LongStaticInfo.OP_JG,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    jg_nobranch.execute();
    assertTrue(jg_nobranch.branchOnTestCalled);
    assertFalse(jg_nobranch.branchOnTestCondition);
    
    Instruction2OpMock jg_branch = createInstructionMock(LongStaticInfo.OP_JG,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    jg_branch.execute();
    assertTrue(jg_branch.branchOnTestCalled);
    assertTrue(jg_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* JIN
  // ******************************************
  
	@Test
  public void testJinNotIn() {
    context.checking(new Expectations() {{
      one (machine).getParent(1); will(returnValue(12));
    }});
    Instruction2OpMock jin_nobranch = createInstructionMock(LongStaticInfo.OP_JIN,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    jin_nobranch.execute();
    assertTrue(jin_nobranch.branchOnTestCalled);
    assertFalse(jin_nobranch.branchOnTestCondition);
  }
  
	@Test
  public void testJinIn() {
    context.checking(new Expectations() {{
      one (machine).getParent(1); will(returnValue(36));
    }});
    Instruction2OpMock jin_branch = createInstructionMock(LongStaticInfo.OP_JIN,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 36);
    jin_branch.execute();
    assertTrue(jin_branch.branchOnTestCalled);
    assertTrue(jin_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* DEC_CHK
  // ******************************************
  
	@Test
  public void testDecChkNoBranch() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 6));
      one (machine).setVariable((char) 0x11, (char) 5);
    }});
    Instruction2OpMock dec_chk_nobranch = createInstructionMock(
        LongStaticInfo.OP_DEC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    dec_chk_nobranch.execute();
    assertTrue(dec_chk_nobranch.branchOnTestCalled);
    assertFalse(dec_chk_nobranch.branchOnTestCondition);
  }  
  
	@Test
  public void testDecChkBranch() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 5));
      one (machine).setVariable((char) 0x11, (char) 4);
    }});
    Instruction2OpMock dec_chk_branch = createInstructionMock(
        LongStaticInfo.OP_DEC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    dec_chk_branch.execute();
    assertTrue(dec_chk_branch.branchOnTestCalled);
    assertTrue(dec_chk_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* INC_CHK
  // ******************************************
  
	@Test
  public void testIncChkNoBranch() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 6));
      one (machine).setVariable((char) 0x11, (char) 7);
    }});
    Instruction2OpMock inc_chk_nobranch = createInstructionMock(
        LongStaticInfo.OP_INC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 7);
    inc_chk_nobranch.execute();
    assertTrue(inc_chk_nobranch.branchOnTestCalled);
    assertFalse(inc_chk_nobranch.branchOnTestCondition);
  }  
  
	@Test
  public void testIncChkBranch() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 5));
      one (machine).setVariable((char) 0x11, (char) 6);
    }});
    Instruction2OpMock inc_chk_branch = createInstructionMock(
        LongStaticInfo.OP_INC_CHK,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    inc_chk_branch.execute();
    assertTrue(inc_chk_branch.branchOnTestCalled);
    assertTrue(inc_chk_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* TEST
  // ******************************************
  
	@Test
  public void testTestNoBranch() {
    Instruction2OpMock test_nobranch = createInstructionMock(LongStaticInfo.OP_TEST,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x7c,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x03);
    test_nobranch.execute();
    assertTrue(test_nobranch.branchOnTestCalled);
    assertFalse(test_nobranch.branchOnTestCondition);
  }
  
	@Test
  public void testTestBranch() {
    Instruction2OpMock test_branch = createInstructionMock(LongStaticInfo.OP_TEST,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x7c,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x14);
    test_branch.execute();
    assertTrue(test_branch.branchOnTestCalled);
    assertTrue(test_branch.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* OR
  // ******************************************
  
	@Test
  public void testOr() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, (char) 0xffff);
    }});
    Instruction2OpMock or = createInstructionMock(LongStaticInfo.OP_OR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0xff00);
    or.setStoreVariable((char) 0x12);
    or.execute();
    assertTrue(or.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* AND
  // ******************************************
  
	@Test
  public void testAnd() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, (char) 0x0000);
    }});
    Instruction2OpMock and = createInstructionMock(LongStaticInfo.OP_AND,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0xff00);
    and.setStoreVariable((char) 0x12);
    and.execute();
    assertTrue(and.nextInstructionCalled);    
  }

  // ***********************************************************************
  // ********* ADD
  // ******************************************
  
	@Test
  public void testAdd() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, (char) 0x0002);
    }});
    Instruction2OpMock add = createInstructionMock(LongStaticInfo.OP_ADD,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -1),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3);
    add.setStoreVariable((char) 0x12);
    add.execute();
    assertTrue(add.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* SUB
  // ******************************************
  
	@Test
  public void testSub() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, signedToUnsigned16((short) -4));
    }});
    Instruction2OpMock sub = createInstructionMock(LongStaticInfo.OP_SUB,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -1),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3);
    sub.setStoreVariable((char) 0x12);
    sub.execute();
    assertTrue(sub.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* MUL
  // ******************************************
  
	@Test
  public void testMul() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, signedToUnsigned16((short) -12));
    }});
    Instruction2OpMock mul = createInstructionMock(LongStaticInfo.OP_MUL,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -4),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3);
    mul.setStoreVariable((char) 0x12);
    mul.execute();
    assertTrue(mul.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* DIV
  // ******************************************
  
	@Test
  public void testDiv() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, signedToUnsigned16((short) -2));
    }});
    Instruction2OpMock div = createInstructionMock(LongStaticInfo.OP_DIV,
      Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -7),
      Operand.TYPENUM_LARGE_CONSTANT, (char) 3);
    div.setStoreVariable((char) 0x12);
    div.execute();
    assertTrue(div.nextInstructionCalled);
  }

	@Test
  public void testDivBy0() {
    context.checking(new Expectations() {{
      one (machine).halt("@div division by zero");
    }});
    Instruction2OpMock div0 = createInstructionMock(LongStaticInfo.OP_DIV,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 7,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0);
    div0.setStoreVariable((char) 0x12);
    div0.execute();
  }
  
  // ***********************************************************************
  // ********* MOD
  // ******************************************
  
	@Test
  public void testMod() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, signedToUnsigned16((short) -1));
    }});
    Instruction2OpMock mod = createInstructionMock(LongStaticInfo.OP_MOD,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -7),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3);
    mod.setStoreVariable((char) 0x12);
    mod.execute();
    assertTrue(mod.nextInstructionCalled);
  }

	@Test
  public void testModBy0() {
    context.checking(new Expectations() {{
      one (machine).halt("@mod division by zero");
    }});
    Instruction2OpMock mod0 = createInstructionMock(LongStaticInfo.OP_MOD,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 7,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0);
    mod0.setStoreVariable((char) 0x12);
    mod0.execute();
  }
  
  // ***********************************************************************
  // ********* TEST_ATTR
  // ******************************************
  
	@Test
  public void testTestAttributeNoBranch() {
    context.checking(new Expectations() {{
      one (machine).isAttributeSet(1, 2); will(returnValue(false));
    }});
    Instruction2OpMock test_attr_nobranch = createInstructionMock(
        LongStaticInfo.OP_TEST_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    test_attr_nobranch.execute();
    assertTrue(test_attr_nobranch.branchOnTestCalled);
    assertFalse(test_attr_nobranch.branchOnTestCondition);
  }

	@Test
  public void testTestAttributeBranch() {
    context.checking(new Expectations() {{
      one (machine).isAttributeSet(1, 2); will(returnValue(true));
    }});
    Instruction2OpMock test_attr_branch = createInstructionMock(
        LongStaticInfo.OP_TEST_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    test_attr_branch.execute();
    assertTrue(test_attr_branch.branchOnTestCalled);
    assertTrue(test_attr_branch.branchOnTestCondition);
  }

  // ***********************************************************************
  // ********* SET_ATTR
  // ******************************************
  
	@Test
  public void testSetAttr() {
    context.checking(new Expectations() {{
      one (machine).setAttribute(1, 2);
    }});
    Instruction2OpMock set_attr = createInstructionMock(
        LongStaticInfo.OP_SET_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    set_attr.execute();    
    assertTrue(set_attr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CLEAR_ATTR
  // ******************************************
  
	@Test
  public void testClearAttr() {
    context.checking(new Expectations() {{
      one (machine).clearAttribute(1, 2);
    }});
    Instruction2OpMock clear_attr = createInstructionMock(
        LongStaticInfo.OP_CLEAR_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    clear_attr.execute();
    assertTrue(clear_attr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* STORE
  // ******************************************
  
	@Test
  public void testStore() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x11, (char) 42);
    }});
    Instruction2OpMock store = createInstructionMock(LongStaticInfo.OP_STORE,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42);
    store.execute();
    assertTrue(store.nextInstructionCalled);
  }
  
  // see standard 1.1
	@Test
  public void testStoreOnStack() {
    context.checking(new Expectations() {{
      one (machine).setStackTop((char) 42);
    }});
    Instruction2OpMock store = createInstructionMock(LongStaticInfo.OP_STORE,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42);
    store.execute();
    assertTrue(store.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* LOADW
  // ******************************************
  
	@Test
  public void testLoadw() {
    context.checking(new Expectations() {{
      one (machine).readUnsigned16(0x0010 + 2); will(returnValue((char) 123));
      one (machine).setVariable((char) 0x11, (char) 123);
    }});
    Instruction2OpMock loadw = createInstructionMock(LongStaticInfo.OP_LOADW,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    loadw.setStoreVariable((char) 0x11);  
    loadw.execute();
    assertTrue(loadw.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* LOADB
  // ******************************************
  
  // TODO: To be clarified: read unsigned or signed ? 
	@Test
  public void testLoadb() {
    context.checking(new Expectations() {{
      one (machine).readUnsigned8(0x0010 + 1); will(returnValue((char) 42));
      one (machine).setVariable((char) 0x11, (char) 42);
    }});
    Instruction2OpMock loadb = createInstructionMock(LongStaticInfo.OP_LOADB,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1);
    loadb.setStoreVariable((char) 0x11);
    loadb.execute();
    assertTrue(loadb.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* GET_PROP
  // ******************************************
  
	@Test
  public void testGetProp() {
    context.checking(new Expectations() {{
      one (machine).getProperty(1, 18); will(returnValue((char) 0x01ee));
      one (machine).setVariable((char) 17, (char) 494);
      one (machine).incrementPC(6);
    }});
    // Two-byte property, Object 1, property 18
    LongInstruction get_prop_two = createInstruction(
        LongStaticInfo.OP_GET_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 18, 6);
    get_prop_two.setStoreVariable((char) 0x11);    
    get_prop_two.execute();    
  }

  // ***********************************************************************
  // ********* INSERT_OBJ
  // ******************************************
  
	@Test
  public void testInsertObj() {
    context.checking(new Expectations() {{
      one (machine).insertObject(2, 7);
    }});
    // Make Object 7 a child of Object 2
    Instruction2OpMock insert_obj = createInstructionMock(
        LongStaticInfo.OP_INSERT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 7,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    insert_obj.execute();
    assertTrue(insert_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PROP_ADDR
  // ******************************************
  
	@Test
  public void testGetPropAddr() {
    context.checking(new Expectations() {{
      one (machine).getPropertyAddress(1, 18); will(returnValue(0x0a55));
      one (machine).setVariable((char) 17, (char) 0x0a55);
      one (machine).incrementPC(6);
    }});
    LongInstruction get_prop_addr_exists = createInstruction(
        LongStaticInfo.OP_GET_PROP_ADDR,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 18, 6);
    get_prop_addr_exists.setStoreVariable((char) 0x11);    
    get_prop_addr_exists.execute();
  }
  
  // ***********************************************************************
  // ********* GET_NEXT_PROP
  // ******************************************
  
	@Test
  public void testGetNextProp() {
    context.checking(new Expectations() {{
      one (machine).getNextProperty(1, 12); will(returnValue(15));
      one (machine).setVariable((char) 0x11, (char) 15);
    }});
    Instruction2OpMock get_next_prop = createInstructionMock(
        LongStaticInfo.OP_GET_NEXT_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 12);
    
    get_next_prop.setStoreVariable((char) 0x11);    
    get_next_prop.execute();
    
    assertTrue(get_next_prop.nextInstructionCalled);
  }
  
  // **********************************************************************
  // ******* Private
  // *******************************
  static class Instruction2OpMock extends LongInstruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public Instruction2OpMock(Machine machine, int opcode) {
      super(machine, opcode);
    }
    
    public Instruction2OpMock(Machine machine, OperandCount operandCount,
        int opcode) {
      super(machine, operandCount, opcode);
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
    
  static Instruction2OpMock createInstructionMock(Machine machine,
  		int opcode, int typenum1, char value1, int typenum2, char value2) {
    Instruction2OpMock result = new Instruction2OpMock(machine, opcode);
    result.addOperand(new Operand(typenum1, value1));
    result.addOperand(new Operand(typenum2, value2));
    result.setLength(5);
    return result;
  }

  private Instruction2OpMock createInstructionMock(int opcode, int typenum1,
  		char value1, int typenum2, char value2) {
  	return createInstructionMock(machine, opcode, typenum1, value1, typenum2,
  			value2);
  }

  private Instruction2OpMock createInstructionMockVarOps(int opcode) {
    Instruction2OpMock result = new Instruction2OpMock(machine, OperandCount.VAR, opcode);
    result.setLength(5);
    return result;
  }  
  
  private LongInstruction createInstruction(int opcode, int typenum1,
      char value1, int typenum2, char value2, int length) {    
    LongInstruction result = new LongInstruction(machine, opcode);
    result.addOperand(new Operand(typenum1, value1));
    result.addOperand(new Operand(typenum2, value2));
    result.setLength(length);
    return result;
  }
}
