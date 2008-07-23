/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.zmpp.vm;

import org.zmpp.instructions.Operand;
import org.zmpp.vm.Instruction.InstructionForm;
import static org.zmpp.vm.Instruction.InstructionForm.*;
import static org.zmpp.vm.Instruction.OperandCount;
import static org.zmpp.vm.Instruction.OperandCount.*;

/**
 *
 * @author weiju
 */
public class NewInstructionDecoder {

  private static final char EXTENDED_MASK     = 0xbe;
  private static final char LOWER_4_BITS      = 0x0f; // 2#00001111
  private static final char BITS_4_5          = 0x30; // 2#00110000
  private static final int LEN_OPCODE         = 1;
  private static final int LEN_LONG_OPERANDS  = 2;
  private static final int LEN_STORE_VARIABLE = 1;
  private Machine machine;
  
  public void initialize(Machine machine) {
    this.machine = machine;
  }

  /**
   * Decode the instruction at the specified address.
   * 
   * @param instructionAddress the current instruction's address
   * @return the instruction at the specified address
   */
  public Instruction decodeInstruction(final int instructionAddress) {
    Instruction instr = null;
    char byte1 = machine.readUnsigned8(instructionAddress);
    InstructionForm form = getForm(byte1);
    switch (form) {
      case SHORT:
        instr = decodeShort(instructionAddress, byte1);
        break;
      case LONG:
        instr = decodeLong(instructionAddress);
        break;
      case VARIABLE:
        instr = decodeVariable(instructionAddress);
        break;
      case EXTENDED:
        instr = decodeExtended(instructionAddress);
        break;
    }
    return instr;
  }
  
  private Instruction decodeShort(int instrAddress, char byte1) {
    OperandCount opCount = (byte1 & BITS_4_5) == BITS_4_5 ? C0OP : C1OP;
    char opcodeNum = (char) (byte1 & LOWER_4_BITS);
    int operandType = getOperandType(byte1, 1);
    return null;
  }
  private Instruction decodeLong(int instrAddress) {
    return null;
  }
  private Instruction decodeVariable(int instrAddress) {
    return null;
  }
  private Instruction decodeExtended(int instrAddress) {
    return null;
  }
  
  private int getOperandType(char opTypeByte, int pos) {
    return ((opTypeByte >>> (6 - pos * 2)) & 0x03);
  }
  
  private int getOperandLength(int operandType) {
    switch (operandType) {
      case Operand.TYPENUM_SMALL_CONSTANT: return 1;
      case Operand.TYPENUM_LARGE_CONSTANT: return 2;
      default: return 0;
    }
  }

  private InstructionForm getForm(char byte1) {
    return EXTENDED;
  }
}
