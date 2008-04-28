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

import org.junit.Test;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.Short1Instruction;
import org.zmpp.instructions.Short1StaticInfo;
import org.zmpp.vm.Machine;

/**
 * This class tests the static and dynamic aspects of C1OP instructions.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Instruction1OpV3Test extends InstructionTestBase {

  @Override
  protected void setUp() throws Exception {
	  super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion")
    	.will(returnValue(3));
  }

  // ***********************************************************************
  // ********* ILLEGAL OPCODES
  // ******************************************
  
  @Test
  public void testIllegalOpcode() {
    Instruction1OpMock illegal = createInstructionMock(machine, 0xdd);
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C1OP opcode: 221"));
    illegal.execute();
  }
  
  // ***********************************************************************
  // ********* INC
  // ******************************************

  @Test
  public void testInc() {
    // Create instruction
    Instruction1OpMock inc = createInstructionMock(Short1StaticInfo.OP_INC,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(2)).will(returnValue((short) -1));
    mockCpu.expects(once()).method("setVariable").with(eq(2), eq((short) 0));
    inc.execute();
    assertTrue(inc.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* DEC
  // ******************************************
  
  @Test
  public void testDec() {
    Instruction1OpMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 6);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(6)).will(returnValue((short) 123));
    mockCpu.expects(once()).method("setVariable").with(eq(6), eq((short) 122));
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }

  @Test
  public void testDec0() {    
    Instruction1OpMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 7);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(7)).will(returnValue((short) 0));
    mockCpu.expects(once()).method("setVariable").with(eq(7), eq((short) -1));    
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }
  // ***********************************************************************
  // ********* GET_PARENT
  // ******************************************
  
  @Test
  public void testGetParent() {    
    Instruction1OpMock get_parent = createInstructionMock(
        Short1StaticInfo.OP_GET_PARENT, Operand.TYPENUM_SMALL_CONSTANT,
        (short) 0x02);
    get_parent.setStoreVariable((short)0x10);
    mockMachine.expects(once()).method("getParent")
    	.with(eq(2))
    	.will(returnValue(27));
    mockMachine.expects(atLeastOnce()).method("getCpu")
    	.will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable")
    	.with(eq(0x10), eq((short) 27));
    get_parent.execute();
    assertTrue(get_parent.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* JUMP
  // ******************************************
  
  @Test
  public void testJump() {
    Instruction1OpMock jump = createInstructionMock(Short1StaticInfo.OP_JUMP,
        Operand.TYPENUM_LARGE_CONSTANT, (short)0x4711);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(18194));
    jump.execute();
  }

  // ***********************************************************************
  // ********* LOAD
  // ******************************************

  @Test
  public void testLoadOperandIsVariable() {    
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_VARIABLE, (short) 0x01);
    // Simulate: value in variable 1 is to, indicating value is retrieved from
    // variable 2
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(1)).will(returnValue((short) 2));
    mockCpu.expects(once()).method("getVariable").with(eq(2)).will(returnValue((short) 4711));    
    mockCpu.expects(once()).method("setVariable").with(eq(0x12), eq((short) 4711));
    
    // Result will be in variable 0x12
    load.setStoreVariable((short) 0x12);
    load.execute();
    assertTrue(load.nextInstructionCalled);
  }

  @Test
  public void testLoadOperandIsConstant() {
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(1)).will(returnValue((short) 4715));
    mockCpu.expects(once()).method("setVariable").with(eq(0x13), eq((short) 4715));
    
    // Result will be in variable 0x13
    load.setStoreVariable((short) 0x13);
    load.execute();
    assertTrue(load.nextInstructionCalled);
  }
  
  // Standard 1.1: Stack reference, the top of stack is read only, not popped
  @Test
  public void testLoadOperandReferencesStack() {
    Instruction1OpMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x00);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getStackTopElement").will(returnValue((short) 4715));
    mockCpu.expects(once()).method("setVariable").with(eq(0x13), eq((short) 4715));
    
    // Result will be in variable 0x13
    load.setStoreVariable((short) 0x13);
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
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01);    
    
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
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x00);
    
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
    Instruction1OpMock get_sibling = createInstructionMock(
    		Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x08);
    mockMachine.expects(once()).method("getSibling")
    	.with(eq(8))
    	.will(returnValue(0));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 0));

    get_sibling.setStoreVariable((short) 0x01);
    get_sibling.execute();
    assertTrue(get_sibling.branchOnTestCalled);
    assertFalse(get_sibling.branchOnTestCondition);
  }
  
  @Test
  public void testGetSiblingHasSibling() {    
    // Object 6 has 152 as its sibling    
    Instruction1OpMock get_sibling = createInstructionMock(Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x06);
    get_sibling.setStoreVariable((short) 0x01);
    
    mockMachine.expects(once()).method("getSibling")
    	.with(eq(6))
    	.will(returnValue(152));
    mockMachine.expects(atLeastOnce()).method("getCpu")
    	.will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 152));
    get_sibling.execute();
    
    assertTrue(get_sibling.branchOnTestCalled);
    assertTrue(get_sibling.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* GET_CHILD
  // ******************************************

  @Test
  public void testGetChildOfObject0() {
    // Object 0 does not exist
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockMachine.expects(once()).method("warn").with(eq("@get_child illegal access to object 0"));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 0));
    
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x00);
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  @Test
  public void testGetChildIs0() {    
    // Object 4 has no child
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x04);
    get_child.setStoreVariable((short) 0x01);
    
    mockMachine.expects(once()).method("getChild")
    	.with(eq(4))
    	.will(returnValue(0));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 0));
    
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  @Test
  public void testGetChildAndBranch() {
    // Object 7 has 41 as its child    
    Instruction1OpMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x07);
    get_child.setStoreVariable((short) 0x02);
    
    mockMachine.expects(once()).method("getChild").with(eq(7)).will(returnValue(41));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(2), eq((short) 41));

    get_child.execute();
  
    assertTrue(get_child.branchOnTestCalled);
    assertTrue(get_child.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* PRINT_ADDR
  // ******************************************

  @Test
  public void testPrintAddr() {
    Instruction1OpMock print_addr = createInstructionMock(Short1StaticInfo.OP_PRINT_ADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x28bc);
    mockMachine.expects(once()).method("printZString").with(eq(0x28bc));
    print_addr.execute();
    assertTrue(print_addr.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* PRINT_PADDR
  // ******************************************
  
  @Test
  public void testPrintPaddr() {
    Instruction1OpMock print_paddr = createInstructionMock(Short1StaticInfo.OP_PRINT_PADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x145e);
    mockMachine.expects(once()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("translatePackedAddress").with(eq(0x145e), eq(false)).will(returnValue(1234));
    mockMachine.expects(once()).method("printZString").with(eq(1234));
    
    print_paddr.execute();
    assertTrue(print_paddr.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* RET
  // ******************************************
  
  @Test
  public void testRet() {
    Instruction1OpMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x145e);    

    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x145e, ret.returnValue);
  }
  
  @Test
  public void testRetWithVariable() {
    Instruction1OpMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
        Operand.TYPENUM_VARIABLE, (short) 0x01);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(1)).will(returnValue((short) 0x23));
    
    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x23, ret.returnValue);
  }
  
  // ***********************************************************************
  // ********* PRINT_OBJ
  // ******************************************
  
  @Test
  public void testPrintObj() {    
    Instruction1OpMock print_obj = createInstructionMock(Short1StaticInfo.OP_PRINT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x03);

    mockMachine.expects(once()).method("getPropertiesDescriptionAddress")
    	.with(eq(3)).will(returnValue(4712));
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    print_obj.execute();
    assertTrue(print_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* REMOVE_OBJ
  // ******************************************
  
  @Test
  public void testRemoveObj() {
    Instruction1OpMock remove_obj = createInstructionMock(Short1StaticInfo.OP_REMOVE_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x03);
    mockMachine.expects(once()).method("removeObject").with(eq(0x03));    
    remove_obj.execute();
    assertTrue(remove_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PROP_LEN
  // ******************************************
  
  @Test
  public void testGetPropLen() {
    Instruction1OpMock get_prop_len = createInstructionMock(Short1StaticInfo.OP_GET_PROP_LEN,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1889);
    get_prop_len.setStoreVariable((short) 0x15);
    mockMachine.expects(once()).method("getPropertyLength").with(eq(0x1889)).will(returnValue(4));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x15), eq((short) 4));
    get_prop_len.execute();
    assertTrue(get_prop_len.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************
 
  @Test
  public void testCall1SIllegalInVersion3() {
    mockMachine.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C1OP opcode: 8"));
    Short1Instruction call1s = createInstructionMock(Short1StaticInfo.OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 4611);
    call1s.execute();
  }
  
  // **********************************************************************
  // ****** Private helpers
  // ****************************************
  
  static class Instruction1OpMock extends Short1Instruction {
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
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
  
  public static Instruction1OpMock createInstructionMock(Machine machine,
  		int opcode) {
    Instruction1OpMock result = new Instruction1OpMock(machine, opcode);
    result.setLength(1);
    
    return result;
  }
  
  private Instruction1OpMock createInstructionMock(int opcode, int typenum,
  		short value) {
  	return createInstructionMock(machine, opcode, typenum, value);
  }

  public static Instruction1OpMock createInstructionMock(Machine machine,
  		int opcode, int typenum, short value) {    
    Instruction1OpMock result = new Instruction1OpMock(machine, opcode);
    result.addOperand(new Operand(typenum, value));
    result.setLength(12);
    
    return result;
  }  
}
