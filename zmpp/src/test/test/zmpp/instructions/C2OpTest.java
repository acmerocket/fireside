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

import java.util.ArrayList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.zmpp.instructions.C2OpInstruction;
import org.zmpp.instructions.InstructionInfoDb;
import static org.junit.Assert.*;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.Machine;
import org.zmpp.vm.RoutineContext;
import static org.zmpp.base.MemoryUtil.*;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;

/**
 * This class tests the C2OpInstruction class.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class C2OpTest extends InstructionTestBase {

  @Override
	@Before
  public void setUp() throws Exception {
    super.setUp();
  }

	@Test
  public void testIsBranch() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C2OP, C2OP_JE, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_JL, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_JG, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_DEC_CHK, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_INC_CHK, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_JIN, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_TEST, 3).isBranch());
    assertTrue(infoDb.getInfo(C2OP, C2OP_TEST_ATTR, 3).isBranch());
    assertFalse(infoDb.getInfo(C2OP, C2OP_ADD, 3).isBranch());
  }

	@Test
  public void testCall2SIllegalInVersion3() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(C2OP, C2OP_CALL_2S, 3));
  }

  @Test	
  public void testStoresResultV4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C2OP, C2OP_OR, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_AND, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_LOADW, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_LOADB, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_GET_PROP, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_GET_PROP_ADDR, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_GET_NEXT_PROP, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_ADD, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_SUB, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_MUL, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_MOD, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_MUL, 4).isStore());
    assertTrue(infoDb.getInfo(C2OP, C2OP_CALL_2S, 4).isStore());
    assertFalse(infoDb.getInfo(C2OP, C2OP_JG, 4).isStore()); 
  }

  @Test
  public void testCall2nIllegalInVersion4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(C2OP, C2OP_CALL_2N, 4));
  }  

	@Test
  public void testStoresResultV5() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C2OP, C2OP_CALL_2S, 5).isStore());
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
    Operand operand1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);
    C2OpMock je1 = createInstructionMock(C2OP_JE, new Operand[] {operand1});
    je1.execute();
  }

	@Test
  public void testJe3Operands() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 0x11); will(returnValue((char) 1));
    }});
    Operand op1 = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);
    Operand op2 = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0x04);
    Operand op3 = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    C2OpMock je3 = createInstructionMock(C2OP_JE, new Operand[] {op1, op2, op3});
    je3.execute();    
    assertTrue(je3.branchOnTestCalled);
    assertTrue(je3.branchOnTestCondition);
  }
  
	@Test
  public void testJe2() {
    C2OpMock je_nobranch = createInstructionMock(C2OP_JE,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    je_nobranch.execute();
    assertTrue(je_nobranch.branchOnTestCalled);
    assertFalse(je_nobranch.branchOnTestCondition);
    
    C2OpMock je_branch = createInstructionMock(C2OP_JE,
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
    C2OpMock jl_nobranch = createInstructionMock(C2OP_JL,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3);    
    jl_nobranch.execute();
    assertTrue(jl_nobranch.branchOnTestCalled);
    assertFalse(jl_nobranch.branchOnTestCondition);
    
    C2OpMock jl_branch = createInstructionMock(C2OP_JL,
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
    C2OpMock jg_nobranch = createInstructionMock(C2OP_JG,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 3,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 5);
    jg_nobranch.execute();
    assertTrue(jg_nobranch.branchOnTestCalled);
    assertFalse(jg_nobranch.branchOnTestCondition);
    
    C2OpMock jg_branch = createInstructionMock(C2OP_JG,
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
    C2OpMock jin_nobranch = createInstructionMock(C2OP_JIN,
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
    C2OpMock jin_branch = createInstructionMock(C2OP_JIN,
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
    C2OpMock dec_chk_nobranch = createInstructionMock(
        C2OP_DEC_CHK,
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
    C2OpMock dec_chk_branch = createInstructionMock(
        C2OP_DEC_CHK,
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
    C2OpMock inc_chk_nobranch = createInstructionMock(
        C2OP_INC_CHK,
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
    C2OpMock inc_chk_branch = createInstructionMock(
        C2OP_INC_CHK,
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
    C2OpMock test_nobranch = createInstructionMock(C2OP_TEST,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x7c,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x03);
    test_nobranch.execute();
    assertTrue(test_nobranch.branchOnTestCalled);
    assertFalse(test_nobranch.branchOnTestCondition);
  }
  
	@Test
  public void testTestBranch() {
    C2OpMock test_branch = createInstructionMock(C2OP_TEST,
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
    char storevar = 0x12;
    C2OpMock or = createInstructionMock(C2OP_OR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0xff00, storevar);
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
    char storevar = 0x12;
    C2OpMock and = createInstructionMock(C2OP_AND,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x00ff,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0xff00, storevar);
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
    char storevar = 0x12;
    C2OpMock add = createInstructionMock(C2OP_ADD,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -1),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3, storevar);
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
    char storevar = 0x12;
    C2OpMock sub = createInstructionMock(C2OP_SUB,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -1),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3, storevar);
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
    char storevar = 0x12;
    C2OpMock mul = createInstructionMock(C2OP_MUL,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -4),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3, storevar);
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
    char storevar = 0x12;
    C2OpMock div = createInstructionMock(C2OP_DIV,
      Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -7),
      Operand.TYPENUM_LARGE_CONSTANT, (char) 3, storevar);
    div.execute();
    assertTrue(div.nextInstructionCalled);
  }

	@Test
  public void testDivBy0() {
    context.checking(new Expectations() {{
      one (machine).halt("@div division by zero");
    }});
    char storevar = 0x12;
    C2OpMock div0 = createInstructionMock(C2OP_DIV,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 7,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0, storevar);
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
    char storevar = 0x12;
    C2OpMock mod = createInstructionMock(C2OP_MOD,
        Operand.TYPENUM_LARGE_CONSTANT, signedToUnsigned16((short) -7),
        Operand.TYPENUM_LARGE_CONSTANT, (char) 3, storevar);
    mod.execute();
    assertTrue(mod.nextInstructionCalled);
  }

	@Test
  public void testModBy0() {
    context.checking(new Expectations() {{
      one (machine).halt("@mod division by zero");
    }});
    char storevar = 0x12;
    C2OpMock mod0 = createInstructionMock(C2OP_MOD,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 7,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0, storevar);
    mod0.execute();
  }
  
  // ***********************************************************************
  // ********* TEST_ATTR
  // ******************************************
  
	@Test
  public void testTestAttributeNoBranchV3() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).isAttributeSet(1, 2); will(returnValue(false));
    }});
    C2OpMock test_attr_nobranch = createInstructionMock(
        C2OP_TEST_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    test_attr_nobranch.execute();
    assertTrue(test_attr_nobranch.branchOnTestCalled);
    assertFalse(test_attr_nobranch.branchOnTestCondition);
  }

	@Test
  public void testTestAttributeBranchV3() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).isAttributeSet(1, 2); will(returnValue(true));
    }});
    C2OpMock test_attr_branch = createInstructionMock(
        C2OP_TEST_ATTR,
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
  public void testSetAttrV3() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).setAttribute(1, 2);
    }});
    C2OpMock set_attr = createInstructionMock(
        C2OP_SET_ATTR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    set_attr.execute();    
    assertTrue(set_attr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CLEAR_ATTR
  // ******************************************
  
	@Test
  public void testClearAttrV3() {
    expectStoryVersion(3);
    context.checking(new Expectations() {{
      one (machine).clearAttribute(1, 2);
    }});
    C2OpMock clear_attr = createInstructionMock(
        C2OP_CLEAR_ATTR,
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
    C2OpMock store = createInstructionMock(C2OP_STORE,
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
    C2OpMock store = createInstructionMock(C2OP_STORE,
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
    char storevar = 0x11;
    C2OpMock loadw = createInstructionMock(C2OP_LOADW,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1, storevar);
    loadw.execute();
    assertTrue(loadw.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* LOADB
  // ******************************************
  
  // TODO: To be clarified: read unsigned or signed ? 
	@Test
  public void testLoadbV3() {
    context.checking(new Expectations() {{
      one (machine).readUnsigned8(0x0010 + 1); will(returnValue((char) 42));
      one (machine).setVariable((char) 0x11, (char) 42);
    }});
    char storevar = 0x11;
    C2OpMock loadb = createInstructionMock(C2OP_LOADB,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x0010,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1, storevar);
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
      one (machine).setVariable((char) 0x12, (char) 494);
    }});
    char storevar = 0x12;
    // Two-byte property, Object 1, property 18
    C2OpMock get_prop_two = createInstructionMock(
        C2OP_GET_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 18, storevar);
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
    C2OpMock insert_obj = createInstructionMock(
        C2OP_INSERT_OBJ,
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
      one (machine).setVariable((char) 0x12, (char) 0x0a55);
    }});
    char storevar = 0x12;
    C2OpMock get_prop_addr_exists = createInstructionMock(
        C2OP_GET_PROP_ADDR,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 18, storevar);
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
    char storevar = 0x11;
    C2OpMock get_next_prop = createInstructionMock(
        C2OP_GET_NEXT_PROP,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 12, storevar);
    get_next_prop.execute();
    
    assertTrue(get_next_prop.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* Version 4
  // ******************************************
  @Test
  public void testCall2s() {
    expectStoryVersion(4);
    final char[] args = { 2 };
    final char returnvalue = 0;
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(4611));
      one (machine).call((char) 1, (char) 4616, args, returnvalue);
    }});
    C2OpMock call2s = createInstructionMock(C2OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    call2s.execute();
  }  
  // ***********************************************************************
  // ********* Version 5
  // ******************************************
  /**
   * We simulate the situation that the current stack is smaller than
   * it could be handled by throw, we should halt the machine, since it
   * is not specified how the machine should behave in this case.
   */
  @Test
  public void testThrowInvalid() {
    final List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1));
    contexts.add(new RoutineContext(2));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(contexts));
      one (machine).halt("@throw from an invalid stack frame state");
    }});
    C2OpMock z_throw = createInstructionMock(
        C2OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    z_throw.execute();
  }

  /**
   * This is the expected situation, in this case we expect that the
   * pop routine context is called as many times until the specified
   * stack frame number is reached and than the function returns with
   * the specified return value.
   */
  @Test
  public void testThrowUnwind() {
    final List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1));
    contexts.add(new RoutineContext(2));
    contexts.add(new RoutineContext(3));
    contexts.add(new RoutineContext(4));
    contexts.add(new RoutineContext(5));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(contexts));
      exactly(2).of (machine).returnWith(with(any(char.class)));
    }});
    C2OpMock z_throw = createInstructionMock(
        C2OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    z_throw.execute();
    assertTrue(z_throw.returned);
    assertEquals((short) 42, z_throw.returnValue);
  }  


  // **********************************************************************
  // ******* Private
  // *******************************
  static class C2OpMock extends C2OpInstruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public C2OpMock(Machine machine, int opcode, Operand[] operands, char storeVar) {
      super(machine, opcode, operands, storeVar, null, 4620, 5);
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
    
  static C2OpMock createInstructionMock(Machine machine,
  		int opcode, Operand[] operands, char storevar) {
    C2OpMock result = new C2OpMock(machine, opcode, operands, storevar);
    return result;
  }

  private C2OpMock createInstructionMock(int opcode, int typenum1,
  		char value1, int typenum2, char value2) {
    Operand operand1 = new Operand(typenum1, value1);
    Operand operand2 = new Operand(typenum2, value2);
  	return createInstructionMock(machine, opcode,
            new Operand[] {operand1, operand2}, (char) 0);
  }
  private C2OpMock createInstructionMock(int opcode, int typenum1,
  		char value1, int typenum2, char value2, char storevar) {
    Operand operand1 = new Operand(typenum1, value1);
    Operand operand2 = new Operand(typenum2, value2);
  	return createInstructionMock(machine, opcode,
            new Operand[] {operand1, operand2}, storevar);
  }

  private C2OpMock createInstructionMock(int opcode, Operand[] operands) {
  	return createInstructionMock(machine, opcode, operands, (char) 0);
  }
}
