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
import static org.junit.Assert.*;

import org.zmpp.instructions.Operand;
import org.zmpp.instructions.Short1Instruction;
import org.zmpp.instructions.Short1StaticInfo;
import org.zmpp.vm.Machine;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;

/**
 * This class tests the static and dynamic aspects of C1OP instructions.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
public class Instruction1OpV3Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
    expectStoryVersion(3);
  }

  // ***********************************************************************
  // ********* ILLEGAL OPCODES
  // ******************************************
  
  @Test
  public void testIllegalOpcode() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C1OP opcode: 221");
    }});
    Instruction1OpMock illegal = createInstructionMock(machine, 0xdd);
    illegal.execute();
  }
  
  // ***********************************************************************
  // ********* INC
  // ******************************************

  @Test
  public void testInc() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 2); will(returnValue(signedToUnsigned16((short) -1)));
      one (machine).setVariable(2, (char) 0);
    }});
    Instruction1OpMock inc = createInstructionMock(Short1StaticInfo.OP_INC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    inc.execute();
    assertTrue(inc.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* DEC
  // ******************************************
  
  @Test
  public void testDec() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 6); will(returnValue((char) 123));
      one (machine).setVariable(6, (char) 122);
    }});
    Instruction1OpMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 6);
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }

  @Test
  public void testDec0() {    
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 7); will(returnValue((char) 0));
      one (machine).setVariable(7, signedToUnsigned16((short) -1));
    }});
    Instruction1OpMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 7);
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }
  // ***********************************************************************
  // ********* GET_PARENT
  // ******************************************
  
  @Test
  public void testGetParent() {    
    context.checking(new Expectations() {{
      one (machine).getParent(2); will(returnValue(27));
      one (machine).setVariable(0x10, (char) 27);
    }});
    Instruction1OpMock get_parent = createInstructionMock(
        Short1StaticInfo.OP_GET_PARENT, Operand.TYPENUM_SMALL_CONSTANT,
        (char) 0x02);
    get_parent.setStoreVariable((char)0x10);
    get_parent.execute();
    assertTrue(get_parent.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* JUMP
  // ******************************************
  
  @Test
  public void testJump() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(18194);
    }});
    Instruction1OpMock jump = createInstructionMock(Short1StaticInfo.OP_JUMP,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x4711);
    jump.execute();
  }

  // ***********************************************************************
  // ********* LOAD
  // ******************************************

  @Test
  public void testLoadOperandIsVariable() {    
    // Simulate: value in variable 1 is to, indicating value is retrieved from
    // variable 2
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 1); will(returnValue((char) 2));
      one (machine).getVariable((char) 2); will(returnValue((char) 4711));
      one (machine).setVariable(0x12, (char) 4711);
    }});
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_VARIABLE, (char) 0x01);
    // Result will be in variable 0x12
    load.setStoreVariable((char) 0x12);
    load.execute();
    assertTrue(load.nextInstructionCalled);
  }

  @Test
  public void testLoadOperandIsConstant() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 1); will(returnValue((char) 4715));
      one (machine).setVariable(0x13, (char) 4715);
    }});
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x01);
    // Result will be in variable 0x13
    load.setStoreVariable((char) 0x13);
    load.execute();
    assertTrue(load.nextInstructionCalled);
  }
  
  // Standard 1.1: Stack reference, the top of stack is read only, not popped
  @Test
  public void testLoadOperandReferencesStack() {
    context.checking(new Expectations() {{
      one (machine).getStackTop(); will(returnValue((char) 4715));
      one (machine).setVariable(0x13, (char) 4715);
    }});
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00);
    // Result will be in variable 0x13
    load.setStoreVariable((char) 0x13);
    load.execute();
    assertTrue(load.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* JZ
  // ******************************************
  
  // Situation 1:
  // Sets operand != 0, so the jump will not be performed
  @Test
  public void testJzBranchIfTrueNotZero() {    
    Instruction1OpMock jz = createInstructionMock(Short1StaticInfo.OP_JZ,
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
    Instruction1OpMock jz = createInstructionMock(Short1StaticInfo.OP_JZ,
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
      one (machine).getSibling(8); will(returnValue(0));
      one (machine).setVariable(0x01, (char) 0);
    }});
    Instruction1OpMock get_sibling = createInstructionMock(
    		Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x08);
    get_sibling.setStoreVariable((char) 0x01);
    get_sibling.execute();
    assertTrue(get_sibling.branchOnTestCalled);
    assertFalse(get_sibling.branchOnTestCondition);
  }
  
  @Test
  public void testGetSiblingHasSibling() {    
    context.checking(new Expectations() {{
      one (machine).getSibling(6); will(returnValue(152));
      one (machine).setVariable(0x01, (char) 152);
    }});
    // Object 6 has 152 as its sibling    
    Instruction1OpMock get_sibling = createInstructionMock(Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x06);
    get_sibling.setStoreVariable((char) 0x01);
    get_sibling.execute();    
    assertTrue(get_sibling.branchOnTestCalled);
    assertTrue(get_sibling.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* GET_CHILD
  // ******************************************
  @Test
  public void testGetChildOfObject0() {
    context.checking(new Expectations() {{
      one (machine).warn("@get_child illegal access to object 0");
      one (machine).setVariable(0x00, (char) 0);
    }});
    // Object 0 does not exist
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x00);
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  @Test
  public void testGetChildIs0() {    
    context.checking(new Expectations() {{
      one (machine).getChild(4); will(returnValue(0));
      one (machine).setVariable(0x01, (char) 0);
    }});
    // Object 4 has no child
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x04);
    get_child.setStoreVariable((char) 0x01);    
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  @Test
  public void testGetChildAndBranch() {
    context.checking(new Expectations() {{
      one (machine).getChild(7); will(returnValue(41));
      one (machine).setVariable(0x02, (char) 41);
    }});
    // Object 7 has 41 as its child    
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x07);
    get_child.setStoreVariable((char) 0x02);    
    get_child.execute();  
    assertTrue(get_child.branchOnTestCalled);
    assertTrue(get_child.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* PRINT_ADDR
  // ******************************************

  @Test
  public void testPrintAddr() {
    context.checking(new Expectations() {{
      one (machine).printZString(0x28bc);
    }});
    Instruction1OpMock print_addr = createInstructionMock(Short1StaticInfo.OP_PRINT_ADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x28bc);
    print_addr.execute();
    assertTrue(print_addr.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* PRINT_PADDR
  // ******************************************
  
  @Test
  public void testPrintPaddr() {
    context.checking(new Expectations() {{
      one (machine).unpackStringAddress(0x145e); will(returnValue(1234));
      one (machine).printZString(1234);
    }});
    Instruction1OpMock print_paddr = createInstructionMock(Short1StaticInfo.OP_PRINT_PADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x145e);
    print_paddr.execute();
    assertTrue(print_paddr.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* RET
  // ******************************************
  
  @Test
  public void testRet() {
    Instruction1OpMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x145e);    
    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x145e, ret.returnValue);
  }
  
  @Test
  public void testRetWithVariable() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 1); will(returnValue((char) 0x23));
    }});
    Instruction1OpMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
        Operand.TYPENUM_VARIABLE, (char) 0x01);
    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x23, ret.returnValue);
  }
  
  // ***********************************************************************
  // ********* PRINT_OBJ
  // ******************************************
  
  @Test
  public void testPrintObj() {
    context.checking(new Expectations() {{
      one (machine).getPropertiesDescriptionAddress(3); will(returnValue(4712));
      one (machine).printZString(4712);
    }});
    Instruction1OpMock print_obj = createInstructionMock(Short1StaticInfo.OP_PRINT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x03);
    print_obj.execute();
    assertTrue(print_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* REMOVE_OBJ
  // ******************************************
  
  @Test
  public void testRemoveObj() {
    context.checking(new Expectations() {{
      one (machine).removeObject(0x03);
    }});
    Instruction1OpMock remove_obj = createInstructionMock(Short1StaticInfo.OP_REMOVE_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 0x03);
    remove_obj.execute();
    assertTrue(remove_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PROP_LEN
  // ******************************************
  
  @Test
  public void testGetPropLen() {
    context.checking(new Expectations() {{
      one (machine).getPropertyLength(0x1889); will(returnValue(4));
      one (machine).setVariable(0x15, (char) 4);
    }});
    Instruction1OpMock get_prop_len = createInstructionMock(Short1StaticInfo.OP_GET_PROP_LEN,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 0x1889);
    get_prop_len.setStoreVariable((char) 0x15);
    get_prop_len.execute();
    assertTrue(get_prop_len.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************
 
  @Test
  public void testCall1SIllegalInVersion3() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C1OP opcode: 8");
    }});
    Short1Instruction call1s = createInstructionMock(Short1StaticInfo.OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (char) 4611);
    call1s.execute();
  }

  // **********************************************************************
  // ****** Private helpers
  // ****************************************
  
  static class Instruction1OpMock extends Short1Instruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public char returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public Instruction1OpMock(Machine machine, int opcode) {
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
  
  static Instruction1OpMock createInstructionMock(Machine machine,
  		int opcode) {
    Instruction1OpMock result = new Instruction1OpMock(machine, opcode);
    result.setLength(1);
    
    return result;
  }
  
  private Instruction1OpMock createInstructionMock(int opcode, int typenum,
  		char value) {
  	return createInstructionMock(machine, opcode, typenum, value);
  }

  static Instruction1OpMock createInstructionMock(Machine machine,
  		int opcode, int typenum, char value) {    
    Instruction1OpMock result = new Instruction1OpMock(machine, opcode);
    result.addOperand(new Operand(typenum, value));
    result.setLength(12);    
    return result;
  }  
}
