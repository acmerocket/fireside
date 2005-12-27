/*
 * $Id$
 * 
 * Created on 03.10.2005
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
package org.zmpp.instructions;

import java.util.ArrayList;
import java.util.List;

import org.zmpp.vm.Instruction;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.RoutineContext;

/**
 * This class represents can be considered as a mutable value object, which
 * basically stores an instruction's information in order to restrict the
 * Instruction class's responsibility to executing logic.
 * 
 * This information will be incrementally added by the decoder, therefore
 * there are setter methods to add information.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public abstract class AbstractInstruction implements Instruction {

  /**
   * The constant for false.
   */
  public static final short FALSE = 0;
  
  /**
   * The constant for true.
   */
  public static final short TRUE = 1;

  /**
   * The constant for true from restore.
   */
  public static final short RESTORE_TRUE = 2;
  
  /**
   * The available instruction forms.
   */
  public enum InstructionForm { LONG, SHORT, VARIABLE }
  
  /**
   * The available operand count types.
   */
  public enum OperandCount { C0OP, C1OP, C2OP, VAR, EXT }
  
  /**
   * This is the result of an instruction.
   */
  public class InstructionResult {
  
    private short value;
    private boolean branchCondition;
    
    public InstructionResult(short value, boolean branchCondition) {
      
      this.value = value;
      this.branchCondition = branchCondition;
    }
    
    public short getValue() {
      
      return value;
    }
    
    public boolean getBranchCondition() {
      
      return branchCondition;
    }
  }
  
  /**
   * The opcode.
   */
  private int opcode;
  
  /**
   * The operands.
   */    
  private List<Operand> operands;
  
  /**
   * The store variable.
   */
  private short storeVariable;
  
  /**
   * If this is a branch instruction, this flag indicates whether to branch
   * if the test condition is false or true.
   */
  private boolean branchIfConditionTrue;
  
  /**
   * The branch offset.
   */
  private short branchOffset;
  
  /**
   * The instruction length in bytes.
   */
  private int length;
  
  /**
   * The machine state.
   */
  private Machine machine;
    
  /**
   * Constructor.
   * 
   * @param machineState a reference to the machine state
   * @param opcode the opcode
   */
  public AbstractInstruction(Machine machineState, int opcode) {
    
    this.opcode = opcode;
    this.machine = machineState;
    this.operands = new ArrayList<Operand>();
    this.branchIfConditionTrue = true;
  }
  
  /**
   * Returns the reference to the machine state.
   * 
   * @return the machine state
   */
  protected Machine getMachine() {
    
    return machine;
  }
    
  /**
   * Returns the instruction's opcode.
   * 
   * @return the opcode
   */
  public int getOpcode() { return opcode; }
  
  /**
   * Returns the instruction's form.
   *  
   * @return the instruction form
   */
  public abstract InstructionForm getInstructionForm();
  
  /**
   * Returns the instruction's operand count type.
   * 
   * @return the operand count type
   */
  public abstract OperandCount getOperandCount();
  
  /**
   * Returns the instruction's static info object.
   * 
   * @return the static info object
   */
  protected abstract InstructionStaticInfo getStaticInfo();
  
  /**
   * Returns the operand at the specified position.
   * 
   * @param operandNum the operand number, starting with 0 as the first operand.
   * @return the specified operand
   */
  public Operand getOperand(int operandNum) {
    
    return operands.get(operandNum);
  }
  
  /**
   * Returns the number of operands.
   * 
   * @return the number of operands
   */
  public int getNumOperands() {
    
    return operands.size();
  }
  
  /**
   * Returns the instruction's store variable.
   * 
   * @return the store variable
   */
  public short getStoreVariable() { return storeVariable; }
  
  /**
   * Returns the branch offset.
   * 
   * @return the branch offset
   */
  public short getBranchOffset() { return branchOffset; }
  
  /**
   * Returns the instruction's length in bytes.
   * 
   * @return the instruction length
   */
  public int getLength() { return length; }
  
  /**
   * Sets the instruction's opcode.
   * 
   * @param opcode the opcode
   */
  public void setOpcode(int opcode) { this.opcode = opcode; }
  
  /**
   * Adds an operand to this object.
   * 
   * @param operand the operand to add
   */
  public void addOperand(Operand operand) { this.operands.add(operand); }
  
  /**
   * Sets the store variable.
   * 
   * @param var the store variable
   */
  public void setStoreVariable(short var) { this.storeVariable = var; }
  
  /**
   * Sets the branch offset.
   * 
   * @param offset the branch offset
   */
  public void setBranchOffset(short offset) { this.branchOffset = offset; }
  
  /**
   * Sets the branch if condition true flag.
   * 
   * @param flag the branch if condition true flag
   */
  public void setBranchIfTrue(boolean flag) { branchIfConditionTrue = flag; }
  
  /**
   * Sets the instruction's length in bytes.
   * 
   * @param length the length in bytes
   */
  public void setLength(int length) { this.length = length; }
  
  /**
   * Returns true, if this instruction stores a result, false, otherwise.
   * 
   * @return true if a result is stored, false otherwise
   */
  public boolean storesResult() {
    
    return getStaticInfo().storesResult(getOpcode(),
        getMachine().getStoryFileHeader().getVersion());
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isOutput() {

    return getStaticInfo().isOutput(getOpcode(),
        getMachine().getStoryFileHeader().getVersion());
  }
  
  /**
   * Returns true, if this instruction is a branch, false, otherwise.
   * 
   * @return true if branch, false otherwise
   */
  public boolean isBranch() {
    
    return getStaticInfo().isBranch(getOpcode(),
        getMachine().getStoryFileHeader().getVersion());
  }
  
  /**
   * Returns true if this is a branch condition and the branch is executed
   * if the test condition is true, false otherwise.
   * 
   * @return true if the branch is executed on a true test condition
   */
  public boolean branchIfTrue() {
    
    return branchIfConditionTrue;
  }

  /**
   * Converts the specified value into a signed value, depending on the
   * type of the operand. If the operand is LARGE_CONSTANT or VARIABLE,
   * the value is treated as a 16 bit signed integer, if it is SMALL_CONSTANT,
   * it is treated as an 8 bit signed integer.
   * 
   * @param operandNum the operand number
   * @return a signed value
   */
  public short getValue(int operandNum) {
    
    Operand operand = getOperand(operandNum);
    switch (operand.getType()) {
    
      case VARIABLE:
        return machine.getVariable(operand.getValue());
      case SMALL_CONSTANT:
      case LARGE_CONSTANT:
      default:
        return operand.getValue();
    }
  }
  
  /**
   * Retrieves the value of the specified operand as an unsigned 16 bit
   * integer.
   * 
   * @param operandNum the operand number
   * @return the value
   */
  public int getUnsignedValue(int operandNum) {
    
    short signedValue = getValue(operandNum);
    return signedValue & 0xffff;
  }
  
  /**
   * Stores the specified value in the result variable.
   * 
   * @param value the value to store
   */
  protected void storeResult(short value) {
    
    machine.setVariable(getStoreVariable(), value);
  }
  
  /**
   * Halt the virtual machine with an error message about this instruction.
   */
  protected void throwInvalidOpcode() {
    
    getMachine().halt("illegal instruction, type: " + getInstructionForm() +
        " operand count: " + getOperandCount() + " opcode: " + getOpcode());
  }
  
  public void execute() {
    
    if (isOpcodeAvailable()) {
      
      doInstruction();
      
    } else {
      
      throwInvalidOpcode();
    }
  }
  
  /**
   * Executes the instruction and returns a result.
   * 
   * @return the result of the instruction
   */
  protected abstract InstructionResult doInstruction();
  
  /**
   * Checks the availability of the instruction for the current version.
   * 
   * @return true if available, false otherwise
   */
  private boolean isOpcodeAvailable() {
    
    int version = getMachine().getStoryFileHeader().getVersion();
    int[] validVersions = getStaticInfo().getValidVersions(getOpcode());
    for (int validVersion : validVersions) {
      
      if (validVersion == version) return true;
    }
    return false;
  }
  
  public String toString() {
    
    StringBuilder buffer = new StringBuilder();
    buffer.append(getStaticInfo().getOpName(getOpcode(),
        getMachine().getStoryFileHeader().getVersion()));
    buffer.append(" ");
    buffer.append(getOperandString());
    if (storesResult()) {
      
      buffer.append(" -> ");
      buffer.append(getVarName(getStoreVariable()));
    }
    return buffer.toString();
  }
  
  private String getVarName(int varnum) {
    
    if (varnum == 0) return "(SP)";
    else if (varnum <= 15) {
      
      return String.format("L%02x", (varnum - 1));
      
    } else {
      
      return String.format("G%02x", (varnum - 16));
    }
  }
  
  private String getVarValue(int varnum) {

    int value = 0;
    if (varnum == 0) {
      
      value = getMachine().getStackTopElement();
      
    } else {
      
      value = getMachine().getVariable(varnum);
    }
    return String.format("$%02x", value);
  }
  
  protected String getOperandString() {

    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < getNumOperands(); i++) {

      if (i > 0) buffer.append(", ");
      Operand operand = getOperand(i);
      switch (operand.getType()) {
      
      case SMALL_CONSTANT:
        buffer.append(String.format("$%02x", operand.getValue()));
        break;
      case LARGE_CONSTANT:
        buffer.append(String.format("$%04x", operand.getValue()));
        break;
      case VARIABLE:
        buffer.append(getVarName(operand.getValue()));
        buffer.append("[");
        buffer.append(getVarValue(operand.getValue()));
        buffer.append("]");
        break;
      }
    }
    return buffer.toString();
  }
  
  // *********************************************************************
  // ******** Program flow control
  // ***********************************

  /**
   * Advances the program counter to the next instruction.
   */
  protected void nextInstruction() {
    
    Machine machineState = getMachine();
    machineState.setProgramCounter(machineState.getProgramCounter()
                                   + getLength());
  }  
  
  /**
   * Performs a branch, depending on the state of the condition flag.
   * If branchIfConditionTrue is true, the branch will be performed if
   * condition is true, if branchIfCondition is false, the branch will
   * be performed if condition is false. 
   *
   * @param condition the test condition
   */
  protected void branchOnTest(boolean condition) {
    
    boolean test = branchIfConditionTrue ? condition : !condition; 
    if (test) {
      
      applyBranch();
      
    } else {
      
      nextInstruction();
    }
  }
  
  /**
   * Applies a jump by applying the branch formula on the pc given the specified
   * offset.
   *  
   * @param offset the offset
   */
  private void applyBranch() {
    
    Machine machineState = getMachine();
    short offset = getBranchOffset();

    if (offset >= 2 || offset < 0) {
      
      machineState.setProgramCounter(
          machineState.computeBranchTarget(getBranchOffset(), getLength()));
      
    } else {
      
      // FALSE is defined as 0, TRUE as 1, so simply return the offset
      // since we do not have negative offsets
      returnFromRoutine(offset);
    }
  }
  
  /**
   * This function returns from the current routine, setting the return value
   * into the specified return variable.
   * 
   * @param returnValue the return value
   */
  protected void returnFromRoutine(short returnValue) {
    
    getMachine().popRoutineContext(returnValue);
  }
  
  /**
   * Calls in the Z-machine are all very similar and only differ in the
   * number of arguments.
   * 
   * @param numArgs the number of arguments
   * @param discardResult whether to discard the result
   */
  protected void call(int numArgs) {

    int packedAddress = getUnsignedValue(0);
    short[] args = new short[numArgs];
    for (int i = 0; i < numArgs; i++) {

      args[i] = getValue(i + 1);
    }
    call(packedAddress, args);
  }
  
  protected void call(int packedRoutineAddress, short[] args) {

    if (packedRoutineAddress != 0) {
      
      int returnAddress = getMachine().getProgramCounter() + getLength();
      short returnVariable = storesResult() ? getStoreVariable() :
        RoutineContext.DISCARD_RESULT;
      
      getMachine().call(packedRoutineAddress, returnAddress, args,
                        returnVariable);
      
    } else {
      
      if (storesResult()) {
        
        // only if this instruction stores a result
        storeResult(FALSE);
      }
      nextInstruction();
    }
  }
  
  protected void saveToStorage(int pc) {
    
    // This is a little tricky: In version 3, the program counter needs to
    // point to the branch offset, and not to an instruction position
    // In version 4, this points to the store variable. In both cases this
    // address is the instruction address + 1
    boolean success = getMachine().save(pc);
    
    if (getMachine().getStoryFileHeader().getVersion() <= 3) {
      
      //int target = getMachine().getProgramCounter() + getLength();
      //target--; // point to the previous branch offset
      //boolean success = getMachine().save(target);
      branchOnTest(success);
      
    } else {
      
      // changed behaviour in version >= 4
      storeResult((short) (success ? TRUE : FALSE));
      nextInstruction();
    }
  }
  
  protected void restoreFromStorage() {

    PortableGameState gamestate = getMachine().restore();
    if (getMachine().getStoryFileHeader().getVersion() <= 3) {

      if (gamestate == null) {

        // If failure on restore, just continue
        nextInstruction();
      }
      
    } else {
            
      // changed behaviour in version >= 4
      if (gamestate != null) {

        int storevar = gamestate.getStoreVariable(getMachine());        
        getMachine().setVariable(storevar, (short) RESTORE_TRUE);
        
      } else {
        
        storeResult((short) FALSE);
        
        // If failure on restore, just continue
        nextInstruction();
      }
    }
  }  
}
