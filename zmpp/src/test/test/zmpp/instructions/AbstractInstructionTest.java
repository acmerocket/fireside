/*
 * $Id$
 * 
 * Created on 09/20/2005
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
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.base.Memory;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.InstructionStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.Instruction.InstructionForm;
import org.zmpp.vm.Instruction.OperandCount;
import org.zmpp.vm.Machine;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;

/**
 * This class tests the AbstractInstruction class.
 *
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class AbstractInstructionTest {
  private Mockery context = new JUnit4Mockery();
  protected Machine machine;
  protected OutputStream outputStream;
  protected Memory memory;
  protected Dictionary dictionary;
  private AbstractInstruction info;
  
  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    outputStream = context.mock(OutputStream.class);
    memory = context.mock(Memory.class);
    dictionary = context.mock(Dictionary.class);
    info = new VariableInstruction(machine, OperandCount.VAR, 0xe0);
  }
  
  @Test
  public void testCreateInstructionInfo() {
    assertEquals(InstructionForm.VARIABLE, info.getInstructionForm());
    assertEquals(OperandCount.VAR, info.getOperandCount());
    assertEquals(0xe0, info.getOpcode());
  }
  
  @Test
  public void testSetters() {
    info.setOpcode(0xe1);
    assertEquals(0xe1, info.getOpcode());
    info.setLength(9);
    assertEquals(9, info.getLength());
    info.setStoreVariable((char) 0x10);
    assertEquals(0x10, info.getStoreVariable());
    info.setBranchOffset((short) 4711);
    assertEquals(4711, info.getBranchOffset());
    info.setBranchIfTrue(false);
    assertFalse(info.branchIfTrue());
    info.setBranchIfTrue(true);
    assertTrue(info.branchIfTrue());
  }

  @Test
  public void testAddOperand() {
    assertEquals(0, info.getNumOperands());
    Operand operand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    info.addOperand(operand);
    assertEquals(1, info.getNumOperands());
    assertEquals(operand, info.getOperand(0));
  }

  @Test
  public void testGetValue() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 17); will(returnValue((char) 1234));
    }});
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand constOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0x11);
    info.addOperand(varOperand);
    info.addOperand(constOperand);
    assertEquals(1234, info.getUnsignedValue(0));
    assertEquals(0x11, info.getUnsignedValue(1));
  }
  
  @Test
  public void testGetUnsignedValueNegative() {
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 17); will(returnValue((char) -2));
    }});
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -4));
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -3));
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0xfffe, info.getUnsignedValue(0));
    assertEquals(0xfffc, info.getUnsignedValue(1));
    assertEquals(0xfffd, info.getUnsignedValue(2));
  }
  
  @Test
  public void testGetUnsignedValueMaxPositive() {    
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 17); will(returnValue((char) 32767));
    }});
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 32767);
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 127);
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0x7fff, info.getUnsignedValue(0));
    assertEquals(0x7fff, info.getUnsignedValue(1));
    assertEquals(0x7f, info.getUnsignedValue(2));
  }
  
  @Test
  public void testGetUnsignedValueMinNegative() {    
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 17); will(returnValue(signedToUnsigned16((short) -32768)));
    }});
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -32768));
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, signedToUnsigned16((short) -128));
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0x8000, info.getUnsignedValue(0));
    assertEquals(0x8000, info.getUnsignedValue(1));
    assertEquals(0xff80, info.getUnsignedValue(2));
  }
  
  @Test
  public void testConvertToSigned16() {   
    context.checking(new Expectations() {{
      one (machine).getVariable((char) 17); will(returnValue(signedToUnsigned16((short) -7)));
    }});
    Operand operandLargeConstant = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0xfffd);
    Operand operandVariable = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand operandByte = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0xfffb);
    
    info.addOperand(operandLargeConstant);
    info.addOperand(operandVariable);
    info.addOperand(operandByte);
    
    assertEquals(-3, info.getSignedValue(0));
    assertEquals(-7, info.getSignedValue(1));
    assertEquals(-5, info.getSignedValue(2)); // bytes values must be unsigned !!!
  }

  // *********************************************************************
  // ***** Branches
  // **********************************
  
  class BranchInstructionInfo implements InstructionStaticInfo {
    int[] versions = { 1, 2, 3, 4, 5, 6, 7, 8 };
    public int[] getValidVersions(int opcode) { return versions; }
    public boolean isBranch(int opcode, int version) { return true; }
    public boolean storesResult(int opcode, int version) { return true; }
    public boolean isOutput(int opcode, int version) { return false; }
    public String getOpName(int opcode, int version) { return "OP"; }
  }
  
  class BranchInstruction extends AbstractInstruction {
    private boolean branchTest;
    public BranchInstruction(Machine machine, boolean branchTest) {
      super(machine, 0);
      this.branchTest = branchTest;
    }
    public OperandCount getOperandCount() { return OperandCount.C2OP; }    
    public InstructionForm getInstructionForm() { return InstructionForm.LONG; }
    protected void doInstruction() { }
    @Override
    public void execute() { super.branchOnTest(branchTest); }
    protected InstructionStaticInfo getStaticInfo() {      
      return new BranchInstructionInfo();
    }
  }
  
  /**
   * Test positive offset branch.
   */
  @Test
  public void testBranchConditionTrue() {
    context.checking(new Expectations() {{
      one (machine).doBranch((short) 42, 12);
    }});
    AbstractInstruction branchInstr = new BranchInstruction(machine, true);
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(true);
    branchInstr.execute();
  }
  
  /**
   * Branch on true, but branch condition is false.
   */
  @Test
  public void testBranchIfTrueBranchConditionIsFalse() {    
    context.checking(new Expectations() {{
      one (machine).incrementPC(12);
    }});
    AbstractInstruction branchInstr = new BranchInstruction(machine, false);
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(true);
    branchInstr.execute();
  }

  /**
   * Branch on false, branch condition is true.
   */
  @Test
  public void testBranchIfFalseBranchConditionIsTrue() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(12);
    }});
    AbstractInstruction branchInstr = new BranchInstruction(machine, true);
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(false);
    branchInstr.execute();
  }
  
  /**
   * Branch on false, branch condition is false.
   */
  @Test
  public void testBranchIfFalseBranchConditionIsFalse() {    
    context.checking(new Expectations() {{
      one (machine).doBranch((short) 42, 12);
    }});
    AbstractInstruction branchInstr = new BranchInstruction(machine, false);
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(false);
    branchInstr.execute();
  }
  
  @Test
  public void testToString() {    
    context.checking(new Expectations() {{
      atLeast(1).of (machine).getVersion(); will(returnValue(3));
    }});
    AbstractInstruction branchInstr = new BranchInstruction(machine, false);
    assertNotNull(branchInstr.toString());
  }
}
