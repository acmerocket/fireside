/*
 * $Id$
 * 
 * Created on 24.09.2005
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
package org.zmpp.vm;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vm.InstructionInfo.InstructionForm;
import org.zmpp.vm.InstructionInfo.OperandCount;

/**
 * The instruction decoder is highly experimental at the moment.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InstructionDecoder {

  /**
   * This instruction info class's objects will be incrementally built
   * by the decoder.
   */
  private class DefaultInstructionInfo implements InstructionInfo {
  
    /**
     * The opcode.
     */
    private int opcode;
    
    /**
     * The instruction form.
     */
    private InstructionForm form;
    
    /**
     * The operand count type.
     */
    private OperandCount operandCount;
    
    /**
     * The operands.
     */    
    private Operand[] operands;
    
    /**
     * The instruction length in bytes.
     */
    private int length;
    
    /**
     * Constructor.
     * 
     * @param form the instruction form
     * @param opcount the operand count type
     * @param opcode the opcode
     */
    public DefaultInstructionInfo(InstructionForm form, OperandCount opcount,
        int opcode) {
      
      this.form = form;
      this.operandCount = opcount;
      this.opcode = opcode;
    }
    
    /**
     * {@inheritDoc}
     */
    public int getOpcode() { return opcode; }
    
    /**
     * {@inheritDoc}
     */
    public InstructionForm getInstructionForm() { return form; }

    /**
     * {@inheritDoc}
     */
    public OperandCount getOperandCount() { return operandCount; }

    /**
     * {@inheritDoc}
     */
    public Operand[] getOperands() { return operands; }    

    /**
     * {@inheritDoc}
     */
    public int getLength() { return length; }

    /**
     * Sets the instruction's opcode.
     * 
     * @param opcode the opcode
     */
    public void setOpcode(int opcode) { this.opcode = opcode; }
    
    /**
     * Sets the instruction's operands.
     * 
     * @param operands the operands
     */
    public void setOperands(Operand[] operands) { this.operands = operands; }
    
    /**
     * Sets the instruction's length in bytes.
     * 
     * @param length the length in bytes
     */
    public void setLength(int length) { this.length = length; }
  }
  
  /**
   * This class is simply a placeholder for experiments.
   */
  private class InstructionImpl implements Instruction {
    
    private InstructionInfo info;
    
    public InstructionImpl(InstructionInfo info) {
      
      this.info = info;
    }
    
    public InstructionInfo getInfo() {
      
      return info;
    }
    
    public void execute() {
     
      // Dummy for now
    }
  }
  
  /**
   * The memory access object.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * Constructor.
   * 
   * @param memaccess the memory access object
   */
  public InstructionDecoder(MemoryReadAccess memaccess) {
  
    this.memaccess = memaccess;
  }
  
  /**
   * Decode the instruction at the specified address.
   * 
   * @param instructionAddress the current instruction's address
   * @return the instruction at the specified address
   */
  public Instruction decodeInstruction(int instructionAddress) {
  
    short firstByte = memaccess.readUnsignedByte(instructionAddress);
    DefaultInstructionInfo info = createBasicInstructionInfo(firstByte);
    extractOperands(info, firstByte, instructionAddress);
   
    return new InstructionImpl(info);
  }
  
  // ***********************************************************************
  // ****** Private functions
  // ******************************************

  /**
   * Create the basic information about the current instruction to be
   * decoded. The returned object contains the instruction form, the
   * operand count type and possibly, the opcode, if it is not an extended
   * opcode.
   * 
   * @param firstByte the instruction's first byte
   * @return a DefaultInstructionInfo object with basic information
   */
  private DefaultInstructionInfo createBasicInstructionInfo(short firstByte) {
    
    InstructionForm form;
    OperandCount operandCount;
    int opcode;
    
    // Determine form and operand count type
    if (0x00 <= firstByte && firstByte <= 0x7f) {
      
      opcode = firstByte;
      form = InstructionForm.LONG;
      operandCount = OperandCount.C2OP;
      
    } else if (firstByte == 0xbe) {

      form = InstructionForm.EXTENDED;
      operandCount = OperandCount.VAR;
      opcode = 0;
      
    } else if (0x80 <= firstByte && firstByte <= 0xbf) {
      
      opcode = firstByte;
      form = InstructionForm.SHORT;
      operandCount = (firstByte >= 0xb0) ? OperandCount.C0OP :
                                           OperandCount.C1OP;
    } else {
      
      opcode = firstByte;
      form = InstructionForm.VARIABLE;
      operandCount = (firstByte >= 0xe0) ? OperandCount.VAR : OperandCount.C2OP;
    }
    return new DefaultInstructionInfo(form, operandCount, opcode);
  }
  
  /**
   * Extracts the operands from the instruction data.
   * 
   * @param info the instruction info object to write to
   * @param firstByte the instruction's first byte
   * @param instructionAddress the instruction address
   */
  private void extractOperands(DefaultInstructionInfo info, short firstByte,
      int instructionAddress) {
    
    short secondByte = memaccess.readUnsignedByte(instructionAddress + 1);
    Operand[] operands = null;
    
    if (0x00 <= firstByte && firstByte <= 0x1f) {
      
      // small constant, small constant
      
    } else if (0x20 <= firstByte && firstByte <= 0x3f) {
      
      // small constant, variable
      
    } else if (0x40 <= firstByte && firstByte <= 0x5f) {
      
      // variable, small constant
      
    } else if (0x60 <= firstByte && firstByte <= 0x7f) {
            
      // variable, variable
      
    } else if (0x80 <= firstByte && firstByte <= 0x8f) {
      
      // large constant
    } else if (0x90 <= firstByte && firstByte <= 0x9f) {
      
      // small constant
      
    } else if (0xa0 <= firstByte && firstByte <= 0xaf) {
      
      // variable
      
    } else if (0xb0 <= firstByte && firstByte <= 0xbf) {
      
      // no operands, NOTE: Handle 0xbe as EXTENDED !!!!
      
    } else if (0xc0 <= firstByte && firstByte <= 0xdf) {
      
      // operand types in next byte
      
    } else if (0xe0 <= firstByte && firstByte <= 0xff) {
      
      // FAKED
      // operand types in next byte(s)
      operands = new Operand[3];
      operands[0] = new Operand(secondByte >> 6,
          translatePackedAddress(memaccess.readUnsignedShort(
                                 instructionAddress + 2)));
      
      operands[1] = new Operand((secondByte >> 4) & 0x03,
          memaccess.readUnsignedShort(instructionAddress + 4));
      operands[2] = new Operand((secondByte >> 2) & 0x03,
          memaccess.readUnsignedShort(instructionAddress + 6));
      info.setLength(9);
    }
    info.setOperands(operands);
  }
  
  /**
   * This translates a packed address into a real address, consider this
   * function to be found elsewhere in the future.
   * 
   * @param packedAddress the packed address
   * @return the real address
   */
  private int translatePackedAddress(int packedAddress) {
    
    return packedAddress * 2;
  }
}
