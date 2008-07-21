/*
 * $Id$
 * 
 * Created on 10/03/2005
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
package org.zmpp.instructions;

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.Memory;
import org.zmpp.base.MemoryUtil;
import org.zmpp.vm.Instruction;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.RoutineContext;
import org.zmpp.vm.ScreenModel6;
import org.zmpp.vm.Window6;

/**
 * This class represents can be considered as a mutable value object, which
 * basically stores an instruction's information in order to restrict the
 * Instruction class's responsibility to executing logic.
 * 
 * This information will be incrementally added by the decoder, therefore
 * there are setter methods to add information.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public abstract class AbstractInstruction implements Instruction {

  /**
   * The constant for false.
   */
  public static final char FALSE = 0;
  
  /**
   * The constant for true.
   */
  public static final char TRUE = 1;

  /**
   * The constant for true from restore.
   */
  public static final char RESTORE_TRUE = 2;
  
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
  public static class InstructionResult {
  
    private char value;
    private boolean branchCondition;
    
    public InstructionResult(char value, boolean branchCondition) {
      this.value = value;
      this.branchCondition = branchCondition;
    }
    
    public char getValue() {
      return value;
    }
    
    public boolean getBranchCondition() {
      return branchCondition;
    }
  }
  
  private int opcode;
  private List<Operand> operands;
  private char storeVariable;
  private boolean branchIfConditionTrue; 
  private short branchOffset;  
  private int length;  
  private Machine machine;
    
  /**
   * Constructor.
   * @param machine a reference to the machine state
   * @param opcode the opcode
   */
  public AbstractInstruction(final Machine machine, final int opcode) {
    this.opcode = opcode;
    this.machine = machine;
    this.operands = new ArrayList<Operand>();
    this.branchIfConditionTrue = true;
  }
  
  /**
   * Returns the reference to the machine state.
   * @return the machine state
   */
  protected Machine getMachine() {
    return machine;
  }
  
  /**
   * Returns the memory object.
   * @return the memory object
   */
  protected Memory getMemory() { return machine; }
  
  /**
   * Returns the instruction's opcode.
   * @return the opcode
   */
  public int getOpcode() { return opcode; }
  
  /**
   * Returns the instruction's form.
   * @return the instruction form
   */
  public abstract InstructionForm getInstructionForm();
  
  /**
   * Returns the instruction's operand count type.
   * @return the operand count type
   */
  public abstract OperandCount getOperandCount();
  
  /**
   * Returns the instruction's static info object.
   * @return the static info object
   */
  protected abstract InstructionStaticInfo getStaticInfo();
  
  /**
   * Returns the operand at the specified position.
   * @param operandNum the operand number, starting with 0 as the first operand.
   * @return the specified operand
   */
  public Operand getOperand(final int operandNum) {
    return operands.get(operandNum);
  }
  
  /**
   * Returns the story file version.
   * @return the story file version
   */
  protected int getStoryFileVersion() {
    return machine.getVersion();
  }
  
  /**
   * Returns the number of operands.
   * @return the number of operands
   */
  public int getNumOperands() {
    return operands.size();
  }
  
  /**
   * Returns the instruction's store variable.
   * @return the store variable
   */
  public char getStoreVariable() { return storeVariable; }
  
  /**
   * Returns the branch offset.
   * @return the branch offset
   */
  public short getBranchOffset() { return branchOffset; }
  
  /**
   * Returns the instruction's length in bytes.
   * @return the instruction length
   */
  public int getLength() { return length; }
  
  /**
   * Sets the instruction's opcode.
   * @param opcode the opcode
   */
  public void setOpcode(final int opcode) { this.opcode = opcode; }
  
  /**
   * Adds an operand to this object.
   * @param operand the operand to add
   */
  public void addOperand(final Operand operand) { this.operands.add(operand); }
  
  /**
   * Sets the store variable.
   * @param var the store variable
   */
  public void setStoreVariable(final char var) { this.storeVariable = var; }
  
  /**
   * Sets the branch offset.
   * @param offset the branch offset
   */
  public void setBranchOffset(final short offset) {
    this.branchOffset = offset;
  }
  
  /**
   * Sets the branch if condition true flag.
   * @param flag the branch if condition true flag
   */
  public void setBranchIfTrue(final boolean flag) {
    branchIfConditionTrue = flag;
  }
  
  /**
   * Sets the instruction's length in bytes.
   * @param length the length in bytes
   */
  public void setLength(final int length) { this.length = length; }
  
  /**
   * Returns true, if this instruction stores a result, false, otherwise.
   * @return true if a result is stored, false otherwise
   */
  public boolean storesResult() {
    return getStaticInfo().storesResult(getOpcode(), getStoryFileVersion());
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isOutput() {
    return getStaticInfo().isOutput(getOpcode(), getStoryFileVersion());
  }
  
  /**
   * Returns true, if this instruction is a branch, false, otherwise.
   * @return true if branch, false otherwise
   */
  public boolean isBranch() {
    return getStaticInfo().isBranch(getOpcode(), getStoryFileVersion());
  }
  
  /**
   * Returns true if this is a branch condition and the branch is executed
   * if the test condition is true, false otherwise.
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
   * @param operandNum the operand number
   * @return a signed value
   */
  public short getSignedValue(final int operandNum) {
    return MemoryUtil.unsignedToSigned16(getUnsignedValue(operandNum));
  }
  
  /**
   * A method to return the signed representation of the contents of a variable
   * @param varnum the variable number
   * @return the signed value
   */
  protected short getSignedVarValue(char varnum) {
    return MemoryUtil.unsignedToSigned16(getMachine().getVariable(varnum));
  }
  
  /**
   * A method to set a signed 16 Bit integer to the specified variable.
   * @param varnum the variable number
   * @param value the signed value
   */
  protected void setSignedVarValue(char varnum, short value) {
    getMachine().setVariable(varnum, MemoryUtil.signedToUnsigned16(value));
  }
  
  /**
   * Retrieves the value of the specified operand as an unsigned 16 bit
   * integer.
   * @param operandNum the operand number
   * @return the value
   */
  public char getUnsignedValue(final int operandNum) {
    final Operand operand = getOperand(operandNum);
    switch (operand.getType()) {
      case VARIABLE:
        return getMachine().getVariable(operand.getValue());
      case SMALL_CONSTANT:
      case LARGE_CONSTANT:
      default:
        return operand.getValue();
    }
  }
  
  /**
   * Stores the specified value in the result variable.
   * @param value the value to store
   */
  protected void storeResult(final char value) {
    getMachine().setVariable(getStoreVariable(), value);
  }
  
  /**
   * Stores a signed value in the result variable.
   * @param value the value to store
   */
  protected void storeSignedResult(final short value) {
    storeResult(MemoryUtil.signedToUnsigned16(value));
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
   */
  protected abstract void doInstruction();
  
  /**
   * Checks the availability of the instruction for the current version.
   * 
   * @return true if available, false otherwise
   */
  private boolean isOpcodeAvailable() {
    final int version = getStoryFileVersion();
    final int[] validVersions = getStaticInfo().getValidVersions(getOpcode());
    for (int validVersion : validVersions) {
      if (validVersion == version) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(getStaticInfo().getOpName(getOpcode(),
                  getStoryFileVersion()));
    buffer.append(" ");
    buffer.append(getOperandString());
    if (storesResult()) {
      buffer.append(" -> ");
      buffer.append(getVarName(getStoreVariable()));
    }
    return buffer.toString();
  }
  
  private String getVarName(final int varnum) {
    if (varnum == 0) {
      return "(SP)";
    } else if (varnum <= 15) {
      return String.format("L%02x", (varnum - 1));
    } else {
      return String.format("G%02x", (varnum - 16));
    }
  }
  
  private String getVarValue(final char varnum) {
    char value = 0;
    if (varnum == 0) {
      value = getMachine().getStackTop();
    } else {
      value = getMachine().getVariable(varnum);
    }
    return String.format("$%02x", (int) value);
  }
  
  protected String getOperandString() {
    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < getNumOperands(); i++) {
      if (i > 0) {
        buffer.append(", ");
      }
      final Operand operand = getOperand(i);
      switch (operand.getType()) {
        case SMALL_CONSTANT:
          buffer.append(String.format("$%02x", (int) operand.getValue()));
          break;
        case LARGE_CONSTANT:
          buffer.append(String.format("$%04x", (int) operand.getValue()));
          break;
        case VARIABLE:
          buffer.append(getVarName(operand.getValue()));
          buffer.append("[");
          buffer.append(getVarValue(operand.getValue()));
          buffer.append("]");
        default:
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
    getMachine().incrementPC(getLength());
  }  
  
  /**
   * Performs a branch, depending on the state of the condition flag.
   * If branchIfConditionTrue is true, the branch will be performed if
   * condition is true, if branchIfCondition is false, the branch will
   * be performed if condition is false. 
   * @param condition the test condition
   */
  protected void branchOnTest(final boolean condition) {
    final boolean test = branchIfConditionTrue ? condition : !condition; 
    if (test) {
      applyBranch();
    } else {
      nextInstruction();
    }
  }
  
  /**
   * Applies a jump by applying the branch formula on the pc given the specified
   * offset.
   * @param offset the offset
   */
  private void applyBranch() {
    getMachine().doBranch(getBranchOffset(), getLength());
  }
  
  /**
   * This function returns from the current routine, setting the return value
   * into the specified return variable.
   * 
   * @param returnValue the return value
   */
  protected void returnFromRoutine(final char returnValue) {
    getMachine().returnWith(returnValue);
  }
  
  /**
   * Calls in the Z-machine are all very similar and only differ in the
   * number of arguments.
   * @param numArgs the number of arguments
   * @param discardResult whether to discard the result
   */
  protected void call(final int numArgs) {
    final char packedAddress = getUnsignedValue(0);
    final char[] args = new char[numArgs];
    for (int i = 0; i < numArgs; i++) {
      args[i] = getUnsignedValue(i + 1);
    }
    call(packedAddress, args);
  }
  
  protected void call(final char packedRoutineAddress, final char[] args) {
    if (packedRoutineAddress == 0) {
      if (storesResult()) {
        // only if this instruction stores a result
        storeResult(FALSE);
      }
      nextInstruction();
    } else {
      final char returnAddress = (char) (getMachine().getPC() + getLength());
      final char returnVariable = storesResult() ? getStoreVariable() :
        RoutineContext.DISCARD_RESULT;      
      machine.call(packedRoutineAddress, returnAddress, args,
               returnVariable);
    }
  }
  
  protected void saveToStorage(final char pc) {
    // This is a little tricky: In version 3, the program counter needs to
    // point to the branch offset, and not to an instruction position
    // In version 4, this points to the store variable. In both cases this
    // address is the instruction address + 1
    final boolean success = getMachine().save(pc);
    
    if (getStoryFileVersion() <= 3) {
      //int target = getMachine().getProgramCounter() + getLength();
      //target--; // point to the previous branch offset
      //boolean success = getMachine().save(target);
      branchOnTest(success);
    } else {
      // changed behaviour in version >= 4
      storeResult(success ? TRUE : FALSE);
      nextInstruction();
    }
  }
  
  protected void restoreFromStorage() {
    final PortableGameState gamestate = getMachine().restore();
    if (getStoryFileVersion() <= 3) {
      if (gamestate == null) {
        // If failure on restore, just continue
        nextInstruction();
      }
    } else {
      // changed behaviour in version >= 4
      if (gamestate == null) {
        storeResult(FALSE);        
        // If failure on restore, just continue
        nextInstruction();
      } else {
        final char storevar = gamestate.getStoreVariable(getMachine());        
        getMachine().setVariable(storevar, RESTORE_TRUE);        
      }
    }
  }
  
  /**
   * Returns the window for a given window number.
   * @param windownum the window number
   * @return the window
   */
  protected Window6 getWindow(final int windownum) {
    return (windownum == ScreenModel6.CURRENT_WINDOW) ?
            getMachine().getScreen6().getSelectedWindow() :
            getMachine().getScreen6().getWindow(windownum);
  }  
}
