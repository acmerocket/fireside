/*
 * Created on 09/20/2005
 * Copyright (c) 2005-2009, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
import static org.zmpp.base.MemoryUtil.*;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.AbstractInstruction.BranchInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.Machine;

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

  /**
   * A Stub instruction class that exposes the protected methods to test
   * their behaviour.
   */
  static class StubInstruction extends AbstractInstruction {
    public StubInstruction(Machine machine, char opcodeNum, Operand[] operands,
                           char storeVar, BranchInfo branchInfo,
                           int opcodeLength) {
      super(machine, opcodeNum, operands, storeVar, branchInfo, opcodeLength);
    }
    @Override
    public int getNumOperands() { return super.getNumOperands(); }
    @Override
    public char getUnsignedValue(int operandNum) {
      return super.getUnsignedValue(operandNum);
    }
    @Override
    public short getSignedValue(int operandNum) {
      return super.getSignedValue(operandNum);
    }
    @Override
    public short getSignedVarValue(char varnum) {
      return super.getSignedVarValue(varnum);
    }
    @Override
    public void setSignedVarValue(char varnum, short value) {
      super.setSignedVarValue(varnum, value);
    }
    @Override
    public void storeUnsignedResult(char value) {
      super.storeUnsignedResult(value);
    }
    @Override
    public void storeSignedResult(short value) {
      super.storeSignedResult(value);
    }
    @Override
    public void nextInstruction() { super.nextInstruction(); }
    @Override
    public void branchOnTest(boolean cond) { super.branchOnTest(cond); }
    @Override
    public void returnFromRoutine(char retval) {
      super.returnFromRoutine(retval);
    }
    @Override
    public void call(int numArgs) { super.call(numArgs);}
    @Override
    public void call(char packedRoutineAddress, char[] args) {
      super.call(packedRoutineAddress, args);
    }
    
    @Override
    protected OperandCount getOperandCount() { return null; }
    public void execute() { }
  }
  
  private static final char STD_STOREVAR = 6;

  @Before
  public void setUp() throws Exception {
    machine = context.mock(Machine.class);
    outputStream = context.mock(OutputStream.class);
    memory = context.mock(Memory.class);
    dictionary = context.mock(Dictionary.class);
  }
  
  private StubInstruction createStubInstruction(Operand[] operands) {
    char opcodeNum = 12;
    char storeVar = STD_STOREVAR;
    BranchInfo branchInfo = new BranchInfo(true, 0, 0, (short) 0);
    int opcodeLength = 5;

    return new StubInstruction(machine, opcodeNum,
            operands, storeVar, branchInfo, opcodeLength);
  }
  private StubInstruction createStdInstruction() {
    Operand stackOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x00);
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (char) 0x11);
    Operand smallConstOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (char) 0xbe);
    Operand largeConstOperand = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0xface);
    return createStubInstruction(new Operand[] {
      stackOperand, varOperand, smallConstOperand, largeConstOperand});
  }
  
  @Test
  public void testCreateInstructionInfo() {
    assertEquals(4, createStdInstruction().getNumOperands());
  }

  @Test
  public void testGetUnsignedValue() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 0x00); will(returnValue((char) 0xcafe));
      oneOf (machine).getVariable((char) 0x11); will(returnValue((char) 0xdeca));
    }});
    StubInstruction instr = createStdInstruction();
    assertEquals(0xcafe, instr.getUnsignedValue(0));
    assertEquals(0xdeca, instr.getUnsignedValue(1));
    assertEquals(0x00be, instr.getUnsignedValue(2));
    assertEquals(0xface, instr.getUnsignedValue(3));
    context.assertIsSatisfied();
  }

  @Test
  public void testGetSignedValue() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 0x00); will(returnValue((char) 0xcafe));
      oneOf (machine).getVariable((char) 0x11); will(returnValue((char) 0xdeca));
    }});
    StubInstruction instr = createStdInstruction();
    assertEquals(unsignedToSigned16((char) 0xcafe), instr.getSignedValue(0));
    assertEquals(unsignedToSigned16((char) 0xdeca), instr.getSignedValue(1));
    // This is interesting, on small constants, it returns an unsigned !
    assertEquals(unsignedToSigned16((char) 0xbe), instr.getSignedValue(2));
    assertEquals(unsignedToSigned16((char) 0xface), instr.getSignedValue(3));
    context.assertIsSatisfied();
  }

  @Test
  public void testGetSignedVarValue() {
    context.checking(new Expectations() {{
      oneOf (machine).getVariable((char) 0x03); will(returnValue((char) 0xfffe));
    }});
    StubInstruction instr = createStdInstruction();
    assertEquals(-2, instr.getSignedVarValue((char) 3));
    context.assertIsSatisfied();
  }

  @Test
  public void testSetSignedVarValue() {
    context.checking(new Expectations() {{
      oneOf (machine).setVariable((char) 3, (char) 0xfffe);
    }});
    StubInstruction instr = createStdInstruction();
    instr.setSignedVarValue((char) 3, (short) -2);
    context.assertIsSatisfied();
  }
  
  @Test
  public void testStoreUnsignedResult() {
    context.checking(new Expectations() {{
      oneOf (machine).setVariable((char) STD_STOREVAR, (char) 0xfeee);
    }});
    StubInstruction instr = createStdInstruction();
    instr.storeUnsignedResult((char) 0xfeee);
    context.assertIsSatisfied();
  }

  @Test
  public void testStoreSignedResult() {
    context.checking(new Expectations() {{
      oneOf (machine).setVariable((char) STD_STOREVAR, (char) 0xfffd);
    }});
    StubInstruction instr = createStdInstruction();
    instr.storeSignedResult((short) -3);
    context.assertIsSatisfied();
  }

  /*
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
  } */
}
