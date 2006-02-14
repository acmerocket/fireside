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
public class Short1InstructionTest extends InstructionTestBase {

  public void testStoresResultV4() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    
    Short1Instruction info;
    info = new Short1Instruction(machine, Short1StaticInfo.OP_GET_SIBLING);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_GET_CHILD);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_GET_PARENT);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_GET_PROP_LEN);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_LOAD);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_NOT);
    assertTrue(info.storesResult());
    info.setOpcode(Short1StaticInfo.OP_CALL_1S);
    assertTrue(info.storesResult());
    
    // no store
    info.setOpcode(Short1StaticInfo.OP_DEC);
    assertFalse(info.storesResult());    
  }
  
  public void testStoresResultV5() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
    Short1Instruction info = new Short1Instruction(machine, Short1StaticInfo.OP_CALL_1N);
    assertFalse(info.storesResult());
  }
  
  public void testIsBranchV4() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    Short1Instruction info;

    info = new Short1Instruction(machine, Short1StaticInfo.OP_JZ);    
    assertTrue(info.isBranch());
    info.setOpcode(Short1StaticInfo.OP_GET_SIBLING);
    assertTrue(info.isBranch());
    info.setOpcode(Short1StaticInfo.OP_GET_CHILD);
    assertTrue(info.isBranch());
    
    // no branch
    info.setOpcode(Short1StaticInfo.OP_GET_PARENT);
    assertFalse(info.isBranch());    
  }
  
  // ***********************************************************************
  // ********* ILLEGAL OPCODES
  // ******************************************
  
  public void testIllegalOpcode() {

    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock illegal = createInstructionMock(0xdd);
    mockMachine.expects(once()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C1OP opcode: 221"));
    illegal.execute();
  }
  
  // ***********************************************************************
  // ********* INC
  // ******************************************

  public void testInc() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Create instruction
    Short1InstructionMock inc = createInstructionMock(Short1StaticInfo.OP_INC,
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
  
  public void testDec() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 6);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(6)).will(returnValue((short) 123));
    mockCpu.expects(once()).method("setVariable").with(eq(6), eq((short) 122));
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }

  public void testDec0() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock dec = createInstructionMock(Short1StaticInfo.OP_DEC,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 7);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(7)).will(returnValue((short) 0));
    mockCpu.expects(once()).method("setVariable").with(eq(7), eq((short) -1));    
    dec.execute();
    assertTrue(dec.nextInstructionCalled);
  }
  // ***********************************************************************
  // ********* NOT
  // ******************************************  
  
  public void testNot() {
	  
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0x5555));     
    
	  // Create instruction	  
	  Short1InstructionMock not = createInstructionMock(Short1StaticInfo.OP_NOT,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0xaaaa);      
    not.setStoreVariable((short) 0x12);
	  not.execute();
	  assertTrue(not.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PARENT
  // ******************************************
  
  public void testGetParent() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock get_parent = createInstructionMock(
        Short1StaticInfo.OP_GET_PARENT, Operand.TYPENUM_SMALL_CONSTANT,
        (short) 0x02);
    get_parent.setStoreVariable((short)0x10);
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(2)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getParent").will(returnValue(27));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x10), eq((short) 27));
    get_parent.execute();
    assertTrue(get_parent.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* JUMP
  // ******************************************
  
  public void testJump() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock jump = createInstructionMock(Short1StaticInfo.OP_JUMP,
        Operand.TYPENUM_LARGE_CONSTANT, (short)0x4711);
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockCpu.expects(once()).method("setProgramCounter").with(eq(1234 + 0x4711 + 1));
    jump.execute();
  }

  // ***********************************************************************
  // ********* LOAD
  // ******************************************

  public void testLoadOperandIsVariable() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
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

  public void testLoadOperandIsConstant() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
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
  public void testLoadOperandReferencesStack() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock load = createInstructionMock(Short1StaticInfo.OP_LOAD,
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
  public void testJzBranchIfTrueNotZero() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock jz = createInstructionMock(Short1StaticInfo.OP_JZ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x01);    
    
    jz.execute();
    assertTrue(jz.branchOnTestCalled);
    assertFalse(jz.branchOnTestCondition);
  }
  
  // Situation 2:
  // Is zero, and branch offset will be 0, so return false from current
  // routine
  public void testJzBranchIfTrueIsZero() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock jz = createInstructionMock(Short1StaticInfo.OP_JZ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x00);
    
    jz.execute();
    assertTrue(jz.branchOnTestCalled);
    assertTrue(jz.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* GET_SIBLING
  // ******************************************
  
  // Object has no next sibling
  public void testGetSiblingIs0() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock get_sibling = createInstructionMock(Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x08);
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(8)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getSibling").will(returnValue(0));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 0));

    get_sibling.setStoreVariable((short) 0x01);
    get_sibling.execute();
    assertTrue(get_sibling.branchOnTestCalled);
    assertFalse(get_sibling.branchOnTestCondition);
  }
  
  public void testGetSiblingHasSibling() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Object 6 has 152 as its sibling    
    Short1InstructionMock get_sibling = createInstructionMock(Short1StaticInfo.OP_GET_SIBLING,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x06);
    get_sibling.setStoreVariable((short) 0x01);
    
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(6)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getSibling").will(returnValue(152));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 152));

    get_sibling.execute();
    
    assertTrue(get_sibling.branchOnTestCalled);
    assertTrue(get_sibling.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* GET_CHILD
  // ******************************************

  public void testGetChildOfObject0() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Object 0 does not exist
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockMachine.expects(once()).method("warn").with(eq("@get_child illegal access to object 0"));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 0));
    
    Short1InstructionMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x00);
    get_child.execute();    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  public void testGetChildIs0() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Object 4 has no child
    Short1InstructionMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x04);
    get_child.setStoreVariable((short) 0x01);
    
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(4)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getChild").will(returnValue(0));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 0));
    
    get_child.execute();
    
    assertTrue(get_child.branchOnTestCalled);
    assertFalse(get_child.branchOnTestCondition);
  }
  
  public void testGetChildAndBranch() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    // Object 7 has 41 as its child    
    Short1InstructionMock get_child = createInstructionMock(Short1StaticInfo.OP_GET_CHILD,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x07);
    get_child.setStoreVariable((short) 0x02);
    
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(7)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getChild").will(returnValue(41));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(2), eq((short) 41));

    get_child.execute();
  
    assertTrue(get_child.branchOnTestCalled);
    assertTrue(get_child.branchOnTestCondition);
  }
  
  // ***********************************************************************
  // ********* PRINT_ADDR
  // ******************************************
  
  public void testPrintAddr() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock print_addr = createInstructionMock(Short1StaticInfo.OP_PRINT_ADDR,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x28bc);
    mockMachine.expects(once()).method("printZString").with(eq(0x28bc));
    print_addr.execute();
    assertTrue(print_addr.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* PRINT_PADDR
  // ******************************************
  
  public void testPrintPaddr() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock print_paddr = createInstructionMock(Short1StaticInfo.OP_PRINT_PADDR,
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
  
  public void testRet() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x145e);    

    ret.execute();
    assertTrue(ret.returned);
    assertEquals((short) 0x145e, ret.returnValue);
  }
  
  public void testRetWithVariable() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock ret = createInstructionMock(Short1StaticInfo.OP_RET,
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
  
  public void testPrintObj() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock print_obj = createInstructionMock(Short1StaticInfo.OP_PRINT_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x03);

    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getObject").with(eq(0x03)).will(returnValue(zobject));
    mockZObject.expects(once()).method("getPropertiesDescriptionAddress").will(returnValue(4712));
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    print_obj.execute();
    assertTrue(print_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* REMOVE_OBJ
  // ******************************************
  
  public void testRemoveObj() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock remove_obj = createInstructionMock(Short1StaticInfo.OP_REMOVE_OBJ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 0x03);
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("removeObject").with(eq(0x03));    
    remove_obj.execute();
    assertTrue(remove_obj.nextInstructionCalled);
  }
  
  // ***********************************************************************
  // ********* GET_PROP_LEN
  // ******************************************
  
  public void testGetPropLen() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    Short1InstructionMock get_prop_len = createInstructionMock(Short1StaticInfo.OP_GET_PROP_LEN,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1889);
    get_prop_len.setStoreVariable((short) 0x15);
    mockGameData.expects(once()).method("getObjectTree").will(returnValue(objectTree));
    mockObjectTree.expects(once()).method("getPropertyLength").with(eq(0x1889)).will(returnValue(4));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x15), eq((short) 4));
    get_prop_len.execute();
    assertTrue(get_prop_len.nextInstructionCalled);
  }

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************
  
  public void testCall1sVersion4() {

    short[] args = {};
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(4611));
    mockCpu.expects(once()).method("call").with(eq(4611), eq(4623), eq(args), eq((short) 0));
    
    Short1Instruction call1s = createInstructionMock(Short1StaticInfo.OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 4611);
    call1s.execute();
  }
  
  public void testCall1SIllegalInVersion3() {

    mockFileHeader.expects(once()).method("getVersion").will(returnValue(3));    
    mockMachine.expects(once()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: SHORT operand count: C1OP opcode: 8"));
    Short1Instruction call1s = createInstructionMock(Short1StaticInfo.OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 4611);
    call1s.execute();
  }
  
  // **********************************************************************
  // ****** Private helpers
  // ****************************************
  
  class Short1InstructionMock extends Short1Instruction {
  
    
    public boolean nextInstructionCalled;
    public boolean returned;
    public short returnValue;
    public boolean branchOnTestCalled;
    public boolean branchOnTestCondition;
    
    public Short1InstructionMock(Machine machine, int opcode) {
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
  
  private Short1InstructionMock createInstructionMock(int opcode) {
    
    Short1InstructionMock result = new Short1InstructionMock(machine, opcode);
    result.setLength(1);
    
    return result;
  }
  
  private Short1InstructionMock createInstructionMock(int opcode, int typenum,
      short value) {
    
    Short1InstructionMock result = new Short1InstructionMock(machine, opcode);
    result.addOperand(new Operand(typenum, value));
    result.setLength(12);
    
    return result;
  }  
}
