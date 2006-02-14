/*
 * $Id$
 * 
 * Created on 20.09.2005
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

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryAccess;
import org.zmpp.instructions.AbstractInstruction;
import org.zmpp.instructions.InstructionStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.AbstractInstruction.InstructionForm;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.GameData;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.ZObject;

/**
 * This class tests the InstructionInfo class.
 *
 * @author Wei-ju Wu
 * @version 1.0
 */
public class AbstractInstructionTest extends MockObjectTestCase {

  protected Mock mockMachine;
  protected Machine machine;
  protected Mock mockServices;
  protected Mock mockGameData;
  protected GameData gamedata;
  protected Mock mockOutputStream;
  protected OutputStream outputStream;
  protected Mock mockObjectTree;
  protected ObjectTree objectTree;
  protected Mock mockZObject;
  protected ZObject zobject;
  protected Mock mockMemAccess;
  protected MemoryAccess memoryAccess;
  protected Mock mockFileHeader;
  protected StoryFileHeader storyfileHeader;
  protected Mock mockDictionary;
  protected Dictionary dictionary;
  protected Mock mockCpu;
  protected Cpu cpu;
  
  private AbstractInstruction info;
  
  public void setUp() throws Exception {
    
    mockMachine = mock(Machine.class);
    machine = (Machine) mockMachine.proxy();
    mockOutputStream = mock(OutputStream.class);
    outputStream = (OutputStream) mockOutputStream.proxy();    
    mockObjectTree = mock(ObjectTree.class);
    objectTree = (ObjectTree) mockObjectTree.proxy();
    mockZObject = mock(ZObject.class);
    zobject = (ZObject) mockZObject.proxy();
    mockMemAccess = mock(MemoryAccess.class);
    memoryAccess = (MemoryAccess) mockMemAccess.proxy();
    mockFileHeader = mock(StoryFileHeader.class);
    storyfileHeader = (StoryFileHeader) mockFileHeader.proxy();
    mockDictionary = mock(Dictionary.class);
    dictionary = (Dictionary) mockDictionary.proxy();
    mockGameData = mock(GameData.class);
    gamedata = (GameData) mockGameData.proxy();
    mockCpu = mock(Cpu.class);
    cpu = (Cpu) mockCpu.proxy();
    
    info = new VariableInstruction(machine, OperandCount.VAR, 0xe0);
  }
  
  public void testCreateInstructionInfo() {
    
    assertEquals(InstructionForm.VARIABLE, info.getInstructionForm());
    assertEquals(OperandCount.VAR, info.getOperandCount());
    assertEquals(0xe0, info.getOpcode());
  }
  
  public void testSetters() {
   
    info.setOpcode(0xe1);
    assertEquals(0xe1, info.getOpcode());
    info.setLength(9);
    assertEquals(9, info.getLength());
    info.setStoreVariable((short) 0x10);
    assertEquals(0x10, info.getStoreVariable());
    info.setBranchOffset((short) 4711);
    assertEquals(4711, info.getBranchOffset());
    info.setBranchIfTrue(false);
    assertFalse(info.branchIfTrue());
    info.setBranchIfTrue(true);
    assertTrue(info.branchIfTrue());
  }
  
  public void testAddOperand() {
    
    assertEquals(0, info.getNumOperands());
    Operand operand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    info.addOperand(operand);
    assertEquals(1, info.getNumOperands());
    assertEquals(operand, info.getOperand(0));
  }
  
  public void testGetValue() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(17)).will(returnValue((short) 1234));
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11);
    Operand constOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) 0x11);
    info.addOperand(varOperand);
    info.addOperand(constOperand);
    assertEquals(1234, info.getValue(0));
    assertEquals(0x11, info.getValue(1));
  }
  
  public void testGetUnsignedValueNegative() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(17)).will(returnValue((short) -2));
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -4);
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) -3);
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0xfffe, info.getUnsignedValue(0));
    assertEquals(0xfffc, info.getUnsignedValue(1));
    assertEquals(0xfffd, info.getUnsignedValue(2));
  }
  
  public void testGetUnsignedValueMaxPositive() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(17)).will(returnValue((short) 32767));
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 32767);
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) 127);
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0x7fff, info.getUnsignedValue(0));
    assertEquals(0x7fff, info.getUnsignedValue(1));
    assertEquals(0x7f, info.getUnsignedValue(2));
  }
  
  public void testGetUnsignedValueMinNegative() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(17)).will(returnValue((short) -32768));
    Operand varOperand = new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11);
    Operand largeOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -32768);
    Operand smallOperand = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) -128);
    
    info.addOperand(varOperand);
    info.addOperand(largeOperand);
    info.addOperand(smallOperand);
    assertEquals(0x8000, info.getUnsignedValue(0));
    assertEquals(0x8000, info.getUnsignedValue(1));
    assertEquals(0xff80, info.getUnsignedValue(2));
  }
  
  public void testConvertToSigned16() {
   
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(17)).will(returnValue((short) -7));
    Operand operandLargeConstant = new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xfffd);
    Operand operandVariable = new Operand(Operand.TYPENUM_VARIABLE, (short) 0x11);
    Operand operandByte = new Operand(Operand.TYPENUM_SMALL_CONSTANT, (byte) 0xfb);
    
    info.addOperand(operandLargeConstant);
    info.addOperand(operandVariable);
    info.addOperand(operandByte);
    
    assertEquals(-3, info.getValue(0));
    assertEquals(-7, info.getValue(1));
    assertEquals(-5, info.getValue(2)); // bytes values must be unsigned !!!
  }

  // *********************************************************************
  // ***** Branches
  // **********************************
  
  class BranchInstructionInfo implements InstructionStaticInfo {
    
    int[] versions = { 1, 2, 3, 4, 5, 6, 7, 8 };
    
    public int[] getValidVersions(int opcode) {
    
      return versions;
    }
    
    public boolean isBranch(int opcode, int version) {
      
      return true;
    }
    
    public boolean storesResult(int opcode, int version) {
      
      return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isOutput(int opcode, int version) {
      
      return false;
    }

    public String getOpName(int opcode, int version) {
      
      return "OP";
    }
  }
  
  class BranchInstruction extends AbstractInstruction {
    
    public BranchInstruction(Machine machine) {

      super(machine, 0);
    }
    
    public OperandCount getOperandCount() {
      
      return OperandCount.C2OP;
    }
    public InstructionForm getInstructionForm() {
      
      return InstructionForm.LONG;
    }
    
    protected InstructionResult doInstruction() {
    
      return new InstructionResult(TRUE, false);
    } 
    
    public void execute() {
      
      super.branchOnTest(true);
    }
    
    protected InstructionStaticInfo getStaticInfo() {
      
      return new BranchInstructionInfo();
    }
  }
  
  /**
   * Test positive offset branch.
   */
  public void testBranchConditionTrue() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("computeBranchTarget").with(eq((short) 42), eq(12)).will(returnValue(321));
    mockCpu.expects(once()).method("setProgramCounter").with(eq(321));

    AbstractInstruction branchInstr = new BranchInstruction(machine);
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(true);
    branchInstr.execute();
  }
  
  /**
   * Branch on true, but branch condition is false.
   *
   */
  public void testBranchIfTrueBranchConditionIsFalse() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(12));
 
    AbstractInstruction branchInstr = new BranchInstruction(machine) {
      
      public void execute() {
        
        super.branchOnTest(false);
      }
    };
    
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(true);
    branchInstr.execute();
  }

  /**
   * Branch on false, branch condition is true.
   *
   */
  public void testBranchIfFalseBranchConditionIsTrue() {

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(12));
    
    AbstractInstruction branchInstr = new BranchInstruction(machine) {
      
      public void execute() {
        
        super.branchOnTest(true);
      }
    };
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(false);
    branchInstr.execute();
  }
  
  /**
   * Branch on false, branch condition is false.
   *
   */
  public void testBranchIfFalseBranchConditionIsFalse() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("computeBranchTarget").with(eq((short) 42), eq(12)).will(returnValue(321));
    mockCpu.expects(once()).method("setProgramCounter").with(eq(321));

    AbstractInstruction branchInstr = new BranchInstruction(machine) {
      
      public void execute() {
        
        super.branchOnTest(false);
      }
    };
    branchInstr.setLength(12);
    branchInstr.setBranchOffset((short) 42);
    branchInstr.setBranchIfTrue(false);
    branchInstr.execute();
  }
  
  public void testToString() {
    
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    mockGameData.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    AbstractInstruction branchInstr = new BranchInstruction(machine) {
      
      public void execute() {
        
        super.branchOnTest(false);
      }
    };
    assertNotNull(branchInstr.toString());
  }
}
