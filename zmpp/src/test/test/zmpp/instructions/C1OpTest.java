/*
 * $Id: Short1InstructionTest.java 524 2007-11-15 00:32:16Z weiju $
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
import org.zmpp.instructions.C1OpInstruction;
import org.zmpp.instructions.InstructionInfoDb;
import static org.junit.Assert.*;

import org.zmpp.instructions.Operand;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;
import org.zmpp.vm.Machine;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;

/**
 * This class tests the static and dynamic aspects of C1OP instructions.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class C1OpTest extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
  }

  // ***********************************************************************
  // ********* INC
  // ******************************************

  @Test
  public void testInc() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 2); will(returnValue(signedToUnsigned16((short) -1)));
      oneOf (machine).setVariable((char) 2, (char) 0);
    }});
    C1OpMock inc = createInstructionMock(C1OP_INC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    inc.execute();
    assertTrue(inc.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* DEC
  // ******************************************
  
  @Test
  public void testDec() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 6); will(returnValue((char) 123));
      oneOf (machine).setVariable((char) 6, (char) 122);
    }});
    C1OpMock dec = createInstructionMock(C1OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 6);
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
    context.assertIsSatisfied();
  }

  @Test
  public void testDec0() {    
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 7); will(returnValue((char) 0));
      oneOf (machine).setVariable((char) 7, signedToUnsigned16((short) -1));
    }});
    C1OpMock dec = createInstructionMock(C1OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 7);
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
    context.assertIsSatisfied();
  }

  // ***********************************************************************
  // ********* GET_PARENT
  // ******************************************
  
  @Test
  public void testGetParent() {    
    context.checking(new Expectations() {{
      oneOf (machine).getParent(2); will(returnValue(27));
      oneOf (machine).setVariable((char) 0x10, (char) 27);
    }});
    char storevar = 0x10;
    C1OpMock get_parent = createInstructionMock(
        C1OP_GET_PARENT, Operand.TYPENUM_SMALL_CONSTANT, (char) 0x02, storevar);
    get_parent.execute();
    assertTrue(get_parent.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* JUMP
  // ******************************************
  
  @Test
  public void testJump() {
    context.checking(new Expectations() {{
      oneOf (machine).incrementPC(18194);
    }});
    C1OpMock jump = createInstructionMock(C1OP_JUMP,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x4711);
    jump.execute();
    context.assertIsSatisfied();
  }

  // ***********************************************************************
  // ********* LOAD
  // ******************************************

  @Test
  public void testLoadOperandIsVariable() {    
    // Simulate: value in variable 1 is to, indicating value is retrieved from
    // variable 2
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 1); will(returnValue((char) 2));
      oneOf (machine).getVariable((char) 2); will(returnValue((char) 4711));
      oneOf (machine).setVariable((char) 0x12, (char) 4711);
    }});
    char storevar = 0x12;
    C1OpMock load = createInstructionMock(C1OP_LOAD,
        Operand.TYPENUM_VARIABLE, (char) 0x01, storevar);
    load.execute();
    assertTrue(load.nextInstructionCalled);
    context.assertIsSatisfied();
  }

  @Test
  public void testLoadOperandIsConstant() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 1); will(returnValue((char) 4715));
      oneOf (machine).setVariable((char) 0x13, (char) 4715);
    }});
    char storevar = 0x13;
    C1OpMock load = createInstructionMock(C1OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01, storevar);
    load.execute();
    assertTrue(load.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // Standard 1.1: Stack reference, the top of stack is read only, not popped
  @Test
  public void testLoadOperandReferencesStack() {
    context.checking(new Expectations() {{
      oneOf (machine).getStackTop(); will(returnValue((char) 4715));
      oneOf (machine).setVariable((char) 0x13, (char) 4715);
    }});
    char storevar = 0x13;
    C1OpMock load = createInstructionMock(C1OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00, storevar);
    load.execute();
    assertTrue(load.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* JZ
  // ******************************************
  
  // Situation 1:
  // Sets operand != 0, so the jump will not be performed
  @Test
  public void testJzBranchIfTrueNotZero() {    
    C1OpMock jz = createInstructionMock(C1OP_JZ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);        
    jz.execute();
    assertTrue(jz.branchOnTestCalled);
    assertFalse(jz.branchOnTestCondition);
  }
  
  // Situation 2:
  // Is zero, and branch offset will be 0, so return false from current
  // routine
  @Test
  public void testJzBranchIfTrueIsZero() {    
    C1OpMock jz = createInstructionMock(C1OP_JZ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00);    
    jz.execute();
    assertTrue(jz.branchOnTestCalled);
    assertTrue(jz.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* GET_SIBLING
  // ******************************************
  
  // Object has no next sibling
  @Test
  public void testGetSiblingIs0() {    
    context.checking(new Expectations() {{
      oneOf (machine).getSibling(8); will(returnValue(0));
      oneOf (machine).setVariable((char) 0x01, (char) 0);
    }});
    char storevar = 0x01;
    C1OpMock get_sibling = createInstructionMock(
    		C1OP_GET_SIBLING, Operand.TYPENUM_SMALL_CONSTANT, (char) 0x08, storevar);
    get_sibling.execute();
    assertTrue(get_sibling.branchOnTestCalled);
    assertFalse(get_sibling.branchOnTestCondition);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testGetSiblingHasSibling() {    
    context.checking(new Expectations() {{
      oneOf (machine).getSibling(6); will(returnValue(152));
      oneOf (machine).setVariable((char) 0x01, (char) 152);
    }});
    // Object 6 has 152 as its sibling
    char storevar = 0x01;
    C1OpMock get_sibling = createInstructionMock(C1OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x06, storevar);
    get_sibling.execute();    
    assertTrue(get_sibling.branchOnTestCalled);
    assertTrue(get_sibling.branchOnTestCondition);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* GET_CHILD
  // ******************************************
  @Test
  public void testGetChildOfObject0() {
    context.checking(new Expectations() {{
      oneOf (machine).warn("@get_child illegal access to object 0");
      oneOf (machine).setVariable((char) 0x00, (char) 0);
    }});
    // Object 0 does not exist
    C1OpMock get_child = createInstructionMock(C1OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00);
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testGetChildIs0() {    
    context.checking(new Expectations() {{
      oneOf (machine).getChild(4); will(returnValue(0));
      oneOf (machine).setVariable((char) 0x01, (char) 0);
    }});
    // Object 4 has no child
    char storevar = 0x01;
    C1OpMock get_child = createInstructionMock(C1OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x04, storevar);
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testGetChildAndBranch() {
    context.checking(new Expectations() {{
      oneOf (machine).getChild(7); will(returnValue(41));
      oneOf (machine).setVariable((char) 0x02, (char) 41);
    }});
    // Object 7 has 41 as its child
    char storevar = 0x02;
    C1OpMock get_child = createInstructionMock(C1OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x07, storevar);
    get_child.execute();  
    assertTrue(get_child.branchOnTestCalled);
    assertTrue(get_child.branchOnTestCondition);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* PRINT_ADDR
  // ******************************************

  @Test
  public void testPrintAddr() {
    context.checking(new Expectations() {{
      oneOf (machine).printZString(0x28bc);
    }});
    C1OpMock print_addr = createInstructionMock(C1OP_PRINT_ADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x28bc);
    print_addr.execute();
    assertTrue(print_addr.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* PRINT_PADDR
  // ******************************************
  
  @Test
  public void testPrintPaddr() {
    context.checking(new Expectations() {{
      oneOf (machine).unpackStringAddress((char) 0x145e); will(returnValue(1234));
      oneOf (machine).printZString(1234);
    }});
    C1OpMock print_paddr = createInstructionMock(C1OP_PRINT_PADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x145e);
    print_paddr.execute();
    assertTrue(print_paddr.nextInstructionCalled);
    context.assertIsSatisfied();
  }

  // ***********************************************************************
  // ********* RET
  // ******************************************
  
  @Test
  public void testRet() {
    C1OpMock ret = createInstructionMock(C1OP_RET,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x145e);    
    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x145e, ret.returnValue);
  }
  
  @Test
  public void testRetWithVariable() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 1); will(returnValue((char) 0x23));
    }});
    C1OpMock ret = createInstructionMock(C1OP_RET,
        Operand.TYPENUM_VARIABLE, (char) 0x01);
    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x23, ret.returnValue);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* PRINT_OBJ
  // ******************************************
  
  @Test
  public void testPrintObj() {
    context.checking(new Expectations() {{
      oneOf (machine).getPropertiesDescriptionAddress(3); will(returnValue(4712));
      oneOf (machine).printZString(4712);
    }});
    C1OpMock print_obj = createInstructionMock(C1OP_PRINT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x03);
    print_obj.execute();
    assertTrue(print_obj.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* REMOVE_OBJ
  // ******************************************
  
  @Test
  public void testRemoveObj() {
    context.checking(new Expectations() {{
      oneOf (machine).removeObject(0x03);
    }});
    C1OpMock remove_obj = createInstructionMock(C1OP_REMOVE_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x03);
    remove_obj.execute();
    assertTrue(remove_obj.nextInstructionCalled);
    context.assertIsSatisfied();
  }
  
  // ***********************************************************************
  // ********* GET_PROP_LEN
  // ******************************************
  
  @Test
  public void testGetPropLen() {
    context.checking(new Expectations() {{
      oneOf (machine).getPropertyLength(0x1889); will(returnValue(4));
      oneOf (machine).setVariable((char) 0x15, (char) 4);
    }});
    char storeVar = (char) 0x15;
    C1OpMock get_prop_len = createInstructionMock(C1OP_GET_PROP_LEN,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1889, storeVar);
    get_prop_len.execute();
    assertTrue(get_prop_len.nextInstructionCalled);
    context.assertIsSatisfied();
  }

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************
 
  @Test
  public void testCall1SIllegalInVersion3() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.isValid(C1OP, C1OP_CALL_1S, 3));
  }

  // ***********************************************************************
  // ********* Version 4
  // ******************************************
  // ***********************************************************************
  // ********* NOT
  // ******************************************  
  
  @Test
  public void testNot() {
    expectStoryVersion(4);
    context.checking(new Expectations() {{
      oneOf (machine).setVariable((char) 0x12, (char) 0x5555);
    }});
	  // Create instruction	  
    char storevar = 0x12;
	  C1OpMock not = createInstructionMock(C1OP_NOT,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0xaaaa, storevar);
	  not.execute();
	  assertTrue(not.nextInstructionCalled);
    context.assertIsSatisfied();
  }  

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************

  @Test
  public void testCall1s() {
    expectStoryVersion(4);
    final char[] args = {};
    context.checking(new Expectations() {{
      oneOf (machine).getPC(); will(returnValue(4620));
      oneOf (machine).call((char) 4611, (char) 4623, args, (char) 0);
    }});
    C1OpMock call1s = createInstructionMock(C1OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 4611);
    call1s.execute();
    context.assertIsSatisfied();
  }
  
	@Test
	public void testStoresResultV4() {
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_SIBLING, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_CHILD, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_PARENT, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_PROP_LEN, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_LOAD, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_NOT, 4).isStore());
    assertTrue(infoDb.getInfo(C1OP, C1OP_CALL_1S, 4).isStore());
    assertFalse(infoDb.getInfo(C1OP, C1OP_DEC, 4).isStore());
	}

  @Test
  public void testIsBranchV4() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertTrue(infoDb.getInfo(C1OP, C1OP_JZ, 4).isBranch());
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_SIBLING, 4).isBranch());
    assertTrue(infoDb.getInfo(C1OP, C1OP_GET_CHILD, 4).isBranch());
    assertFalse(infoDb.getInfo(C1OP, C1OP_GET_PARENT, 4).isBranch());
  }  

  @Test
  public void testStoresResultV5() {    
    InstructionInfoDb infoDb = InstructionInfoDb.getInstance();
    assertFalse(infoDb.getInfo(C1OP, C1OP_CALL_1N, 5).isStore());
  }  
  // **********************************************************************
  // ****** Private helpers
  // ****************************************
  
  static class C1OpMock extends C1OpInstruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    public C1OpMock(Machine machine, int opcode, Operand[] operands, char storeVar) {
      super(machine, opcode, operands, storeVar, null, 3);
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
  
  private C1OpMock createInstructionMock(int opcode, int typenum,
  		char value) {
  	return createInstructionMock(machine, opcode, typenum, value, (char) 0);
  }
  private C1OpMock createInstructionMock(int opcode, int typenum,
  		char value, char storevar) {
  	return createInstructionMock(machine, opcode, typenum, value, storevar);
  }
  static C1OpMock createInstructionMock(Machine machine,
  		int opcode, int typenum, char value, char storevar) {    
    Operand operand1 = new Operand(typenum, value);
    C1OpMock result = new C1OpMock(machine, opcode, new Operand[] {operand1}, storevar);
    return result;
  }  
}
