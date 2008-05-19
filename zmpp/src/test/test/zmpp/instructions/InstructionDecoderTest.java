/*
 * $Id$
 * 
 * Created on 09/24/2005
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

import org.jmock.Mock;
import org.zmpp.base.DefaultMemory;
import org.zmpp.base.Memory;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.PrintLiteralInstruction;
import org.zmpp.instructions.PrintLiteralStaticInfo;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.instructions.Short1StaticInfo;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.InstructionForm;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.instructions.Operand.OperandType;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;

import test.zmpp.vm.MiniZorkSetup;

/**
 * This class contains tests for the InstructionDecoder class.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InstructionDecoderTest extends MiniZorkSetup {

  private InstructionDecoder decoder;
  
  private Memory amfvmem;
  private byte[] call_vs2 = {
      (byte) 0xec, 0x25, (byte) 0xbf, 0x3b, (byte) 0xf7, (byte) 0xa0,
      0x10, 0x20, 0x01, 0x00
  };
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    decoder = new InstructionDecoder();
    decoder.initialize(machine);
  }

  /**
   * Tests for minizork's instructions. This is more of an integration test,
   * leave it here anyways.
   */
  public void testMinizorkVariable() {
    
    // VARIABLE: Instruction at 0x37d9 is call 0x3b36, #3e88, #ffff
    AbstractInstruction info = (AbstractInstruction) decoder.decodeInstruction(0x37d9);
    assertEquals(InstructionForm.VARIABLE, info.getInstructionForm());
    assertEquals(OperandCount.VAR, info.getOperandCount());
    assertEquals(VariableStaticInfo.OP_CALL, info.getOpcode());
    assertEquals(3, info.getNumOperands());
    
    assertEquals(OperandType.LARGE_CONSTANT, info.getOperand(0).getType());
    assertEquals(OperandType.LARGE_CONSTANT, info.getOperand(1).getType());
    assertEquals(OperandType.LARGE_CONSTANT, info.getOperand(2).getType());
    
    assertEquals(0x3b36, info.getOperand(0).getValue() * 2);
    assertEquals(0x3e88, info.getOperand(1).getValue());
    assertEquals((short) (0xffff), info.getOperand(2).getValue());
    assertEquals(0x00, info.getStoreVariable());
    assertEquals(0x37e2, 0x37d9 + info.getLength());
    
    // VARIABLE: Instruction at 0x37e2 is storew (sp)+, #00, #01
    AbstractInstruction info2 = (AbstractInstruction) decoder.decodeInstruction(0x37e2);
    assertEquals(InstructionForm.VARIABLE, info2.getInstructionForm());
    assertEquals(OperandCount.VAR, info2.getOperandCount());
    assertEquals(VariableStaticInfo.OP_STOREW, info2.getOpcode());
    assertEquals(3, info2.getNumOperands());

    assertEquals(OperandType.VARIABLE, info2.getOperand(0).getType());
    assertEquals(OperandType.SMALL_CONSTANT, info2.getOperand(1).getType());
    assertEquals(OperandType.SMALL_CONSTANT, info2.getOperand(2).getType());
    
    assertEquals(0x00, info2.getOperand(0).getValue());
    assertEquals(0x00, info2.getOperand(1).getValue());
    assertEquals(0x01, info2.getOperand(2).getValue());
    assertEquals(0x37e7, 0x37e2 + info2.getLength());
  }
  
  public void testMinizorkBranch() {
    
    // SHORT 1OP: Instruction at 0x3773 is jz g17 [true] 0x377f
    AbstractInstruction jz = (AbstractInstruction) decoder.decodeInstruction(0x3773);
    assertEquals(InstructionForm.SHORT, jz.getInstructionForm());
    assertEquals(OperandCount.C1OP, jz.getOperandCount());
    assertEquals(1, jz.getNumOperands());
    assertEquals(Short1StaticInfo.OP_JZ, jz.getOpcode());
    assertEquals(OperandType.VARIABLE, jz.getOperand(0).getType());    
    assertEquals(0x27, jz.getOperand(0).getValue()); // g17 == 0x27
    assertEquals(11, jz.getBranchOffset());
    assertTrue(jz.branchIfTrue());
    assertEquals(0x377f, 0x3773 + jz.getLength() + jz.getBranchOffset() - 2);
  }
  
  public void testMinizorkRet() {
    
    // SHORT 1OP: Instruction at 0x37d5 is ret L04
    AbstractInstruction retL04 = (AbstractInstruction) decoder.decodeInstruction(0x37d5);
    assertEquals(InstructionForm.SHORT, retL04.getInstructionForm());
    assertEquals(OperandCount.C1OP, retL04.getOperandCount());
    assertEquals(1, retL04.getNumOperands());
    assertEquals(Short1StaticInfo.OP_RET, retL04.getOpcode());
    assertEquals(OperandType.VARIABLE, retL04.getOperand(0).getType());    
    assertEquals(5, retL04.getOperand(0).getValue());    
  }
  
  public void testMinizorkShort1OP() {
            
    // SHORT 1OP: Instruction at 0x379f is dec L01
    AbstractInstruction decL01 = (AbstractInstruction) decoder.decodeInstruction(0x379f);
    assertEquals(InstructionForm.SHORT, decL01.getInstructionForm());
    assertEquals(OperandCount.C1OP, decL01.getOperandCount());
    assertEquals(1, decL01.getNumOperands());
    assertEquals(Short1StaticInfo.OP_DEC, decL01.getOpcode());
    assertEquals(OperandType.SMALL_CONSTANT, decL01.getOperand(0).getType());    
    // L01 is value 2
    assertEquals(2, decL01.getOperand(0).getValue());
    assertEquals(0x37a1, 0x379f + decL01.getLength());
    
    // SHORT 1OP: Instruction at 0x3816 is jump 0x37d9
    AbstractInstruction jump = (AbstractInstruction) decoder.decodeInstruction(0x3816);
    assertEquals(InstructionForm.SHORT, jump.getInstructionForm());
    assertEquals(OperandCount.C1OP, jump.getOperandCount());
    assertEquals(1, jump.getNumOperands());
    assertEquals(Short1StaticInfo.OP_JUMP, jump.getOpcode());
    assertEquals(OperandType.LARGE_CONSTANT, jump.getOperand(0).getType());    
    assertEquals((short) (0xffc2), jump.getOperand(0).getValue());
    
    // SHORT 1OP: Instruction at 0x37c7 is inc L01
    AbstractInstruction incL02 = (AbstractInstruction) decoder.decodeInstruction(0x37c7);
    assertEquals(InstructionForm.SHORT, incL02.getInstructionForm());
    assertEquals(OperandCount.C1OP, incL02.getOperandCount());
    assertEquals(1, incL02.getNumOperands());
    assertEquals(Short1StaticInfo.OP_INC, incL02.getOpcode());
    assertEquals(OperandType.SMALL_CONSTANT, incL02.getOperand(0).getType());    
    assertEquals(3, incL02.getOperand(0).getValue());    
  }
  
  public void testMinizorkShort() {
  

    // SHORT 0OP: Instruction at 0x3788 is rfalse
    AbstractInstruction rfalse = (AbstractInstruction) decoder.decodeInstruction(0x3788);
    assertEquals(InstructionForm.SHORT, rfalse.getInstructionForm());
    assertEquals(OperandCount.C0OP, rfalse.getOperandCount());
    assertEquals(0, rfalse.getNumOperands());
    assertEquals(Short0StaticInfo.OP_RFALSE, rfalse.getOpcode());    
    assertEquals(0x3789, 0x3788 + rfalse.getLength());
  }
  
  public void testMinizorkLong() {
   
    // LONG: Instruction at 0x37c9 is je L02, L01
    AbstractInstruction je = (AbstractInstruction) decoder.decodeInstruction(0x37c9);
    
    assertEquals(InstructionForm.LONG, je.getInstructionForm());
    assertEquals(OperandCount.C2OP, je.getOperandCount());
    assertEquals(LongStaticInfo.OP_JE, je.getOpcode());
    assertEquals(2, je.getNumOperands());
    assertEquals(OperandType.VARIABLE, je.getOperand(0).getType());
    assertEquals(3, je.getOperand(0).getValue());
    assertEquals(OperandType.VARIABLE, je.getOperand(1).getType());
    assertEquals(2, je.getOperand(1).getValue());
  }
  
  public void testMinizorkPrint() {
    
    AbstractInstruction print = (AbstractInstruction) decoder.decodeInstruction(0x393f);
    assertEquals(InstructionForm.SHORT, print.getInstructionForm());
    assertEquals(OperandCount.C0OP, print.getOperandCount());
    assertEquals(PrintLiteralStaticInfo.OP_PRINT, print.getOpcode());
    assertTrue(print instanceof PrintLiteralInstruction);
    assertEquals(0, print.getNumOperands());
    assertEquals(5, print.getLength());
    
    AbstractInstruction print_ret = (AbstractInstruction) decoder.decodeInstruction(0x5761);
    assertEquals(InstructionForm.SHORT, print_ret.getInstructionForm());
    assertEquals(OperandCount.C0OP, print_ret.getOperandCount());
    assertEquals(PrintLiteralStaticInfo.OP_PRINT_RET, print_ret.getOpcode());
    assertTrue(print_ret instanceof PrintLiteralInstruction);
    assertEquals(0, print_ret.getNumOperands());
    assertEquals(5, print_ret.getLength());
    
  }
  
  public void testMinizorkAnd() {
    
    // AH !!! This is really a long instruction, but encoded as a
    // variable instruction, this is odd !!!!
    // This needs to be handled !!!
    AbstractInstruction and = (AbstractInstruction) decoder.decodeInstruction(0x58d4);
    assertEquals(OperandCount.VAR, and.getOperandCount());
    assertEquals(InstructionForm.LONG, and.getInstructionForm());
    assertEquals(LongStaticInfo.OP_AND, and.getOpcode());
    assertEquals(2, and.getNumOperands());
    assertEquals(OperandType.VARIABLE, and.getOperand(0).getType());
    assertEquals(0, and.getOperand(0).getValue()); // ????
    assertEquals(OperandType.LARGE_CONSTANT, and.getOperand(1).getType());
    assertEquals(0x07ff, and.getOperand(1).getValue());
    
    // Here is a store variable to come, because the operation defines it
    
    assertEquals(0, and.getStoreVariable());
    assertEquals(6, and.getLength());
  }
  
  public void testMinizorkJump() {
    
    AbstractInstruction jump = (AbstractInstruction) decoder.decodeInstruction(0x58f7);
    assertEquals(OperandCount.C1OP, jump.getOperandCount());
    assertEquals(InstructionForm.SHORT, jump.getInstructionForm());
    assertEquals(Short1StaticInfo.OP_JUMP, jump.getOpcode());
    assertEquals(OperandType.LARGE_CONSTANT, jump.getOperand(0).getType());
    assertEquals(-12, (short) jump.getOperand(0).getValue());
  }

  
  public void testMinizorkGetSibling() {
    //System.out.println("get_sibling");
    AbstractInstruction get_sibling = (AbstractInstruction) decoder.decodeInstruction(0x6dbd);
    assertEquals(OperandCount.C1OP, get_sibling.getOperandCount());
    assertEquals(InstructionForm.SHORT, get_sibling.getInstructionForm());
    assertEquals(Short1StaticInfo.OP_GET_SIBLING, get_sibling.getOpcode());
    assertEquals(OperandType.VARIABLE, get_sibling.getOperand(0).getType());
    assertEquals(4, (short) get_sibling.getOperand(0).getValue());
    assertEquals(4, (short) get_sibling.getStoreVariable());
    assertEquals(-85, (short) get_sibling.getBranchOffset());
  }
  
  public void testJe3Operands() {
    
    AbstractInstruction je3 = (AbstractInstruction) decoder.decodeInstruction(0x6dc5);
    assertEquals(InstructionForm.LONG, je3.getInstructionForm());    
    assertEquals(OperandCount.VAR, je3.getOperandCount());
    assertEquals(3, je3.getNumOperands());
    assertEquals(OperandType.VARIABLE, je3.getOperand(0).getType());
    assertEquals(4, je3.getOperand(0).getValue());
    assertEquals(OperandType.VARIABLE, je3.getOperand(1).getType());
    assertEquals(7, je3.getOperand(1).getValue());
    assertEquals(OperandType.SMALL_CONSTANT, je3.getOperand(2).getType());
    assertEquals(0x1e, je3.getOperand(2).getValue());
  }
  
  
  public void testDecodeCallVs2() {

    // Setup for machine 4
    amfvmem = new DefaultMemory(call_vs2);
    Mock mockMachine4 = mock(Machine.class);
    Machine machine4 = (Machine) mockMachine4.proxy();
    
    InstructionDecoder decoder4 = new InstructionDecoder();
    decoder4.initialize(machine4);
    
    mockMachine4.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(0)).
            will(returnValue((short) amfvmem.readUnsignedByte(0)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(1)).
            will(returnValue((short) amfvmem.readUnsignedByte(1)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(2)).
            will(returnValue((short) amfvmem.readUnsignedByte(2)));
    mockMachine4.expects(atLeastOnce()).method("readShort").with(eq(3)).
            will(returnValue((short) amfvmem.readShort(3)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(5)).
            will(returnValue((short) amfvmem.readUnsignedByte(5)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(6)).
            will(returnValue((short) amfvmem.readUnsignedByte(6)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(7)).
            will(returnValue((short) amfvmem.readUnsignedByte(7)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(8)).
            will(returnValue((short) amfvmem.readUnsignedByte(8)));
    mockMachine4.expects(atLeastOnce()).method("readUnsignedByte").with(eq(9)).
            will(returnValue((short) amfvmem.readUnsignedByte(9)));
    
    // Expected:
    // ecf4:  CALL_VS2        efdc (G90,#10,#20,L00) -> -(SP)
    AbstractInstruction call_vs2 = (AbstractInstruction) decoder4.decodeInstruction(0);
    assertEquals(InstructionForm.VARIABLE, call_vs2.getInstructionForm());
    assertTrue(call_vs2.storesResult());
    assertEquals(5, call_vs2.getNumOperands());
    assertEquals(OperandType.LARGE_CONSTANT, call_vs2.getOperand(0).getType());
    assertEquals(OperandType.VARIABLE, call_vs2.getOperand(1).getType());
    assertEquals(OperandType.SMALL_CONSTANT, call_vs2.getOperand(2).getType());
    assertEquals(OperandType.SMALL_CONSTANT, call_vs2.getOperand(3).getType());
    assertEquals(OperandType.VARIABLE, call_vs2.getOperand(4).getType());
    assertEquals(15351, call_vs2.getOperand(0).getValue());
    assertEquals(0xa0, call_vs2.getOperand(1).getValue());
    assertEquals(0x10, call_vs2.getOperand(2).getValue());
    assertEquals(0x20, call_vs2.getOperand(3).getValue());
    assertEquals(1, call_vs2.getOperand(4).getValue());
    assertEquals(0, call_vs2.getStoreVariable());
  }
}
