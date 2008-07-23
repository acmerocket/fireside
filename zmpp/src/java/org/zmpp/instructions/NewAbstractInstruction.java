/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.instructions;

import org.zmpp.vm.Instruction;

/**
 * An abstract instruction to replace the old instruction scheme.
 * Goes with the NewInstructionDecoder.
 * @author Wei-ju Wu
 * @version 1.5
 */
public abstract class NewAbstractInstruction implements Instruction {
  public static class BranchInfo {
    public boolean branchOnTrue;
    public int numOffsetBytes;
    public int addressAfterBranchData;
    public int branchOffset;
  }

  private OperandCount operandCount;
  private int opcodeNum;
  private char[] operands;
  private char storeVariable;
  private BranchInfo branchInfo;
  private int address;
  private int opcodeLength;
}
