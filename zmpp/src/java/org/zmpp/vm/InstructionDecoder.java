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

import java.util.ArrayList;
import java.util.List;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vm.InstructionInfo.InstructionForm;
import org.zmpp.vm.InstructionInfo.OperandCount;

/**
 * The instruction decoder decodes an instruction at a specified address.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class InstructionDecoder {
    
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
  
    InstructionInfo info = createBasicInstructionInfo(instructionAddress);
    int currentAddress = extractOperands(info, instructionAddress);
    currentAddress = extractStoreVariable(info, currentAddress);    
    info.setLength(currentAddress - instructionAddress);
    
    Instruction instr = new Instruction();
    instr.setInfo(info);
    return instr;
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
   * @param the instruction's start address
   * @return a DefaultInstructionInfo object with basic information
   */
  private InstructionInfo createBasicInstructionInfo(int instructionAddress) {
    
    InstructionForm form;
    OperandCount operandCount;
    int opcode;
    short firstByte = memaccess.readUnsignedByte(instructionAddress);
    
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
    return new InstructionInfo(form, operandCount, opcode);
  }
  
  /**
   * Extracts the operands from the instruction data. At this step of
   * decoding the some basic information about the instruction is available
   * and could be used for extraction of parameters.
   * 
   * @param info the instruction info object to write to
   * @param instructionAddress the instruction address
   * @return the current address in decoding
   */
  private int extractOperands(InstructionInfo info, int instructionAddress) {
    
    short secondByte = memaccess.readUnsignedByte(instructionAddress + 1);
          
    // operand types in next byte(s)
    List<Operand> operands = new ArrayList<Operand>();
    int currentAddress = instructionAddress + 2;
    
    currentAddress = extractOperandsWithType(operands, secondByte,
                                             currentAddress);    

    info.setOperands(operands.toArray(new Operand[0]));
    return currentAddress;
  }
  
  /**
   * Extract operands for the given optype byte value starting at the given
   * decoding address. This is outfactored in order to be called at least two
   * times. The generated operands are added to the specified operand list.
   * 
   * @param operands the operand list to add to
   * @param optypeByte the optype byte
   * @param currentAddress the current decoding address
   * @return the new decoding address after extracting the operands
   */
  private int extractOperandsWithType(List<Operand> operands, int optypeByte,
                                      int currentAddress) {
    
    int nextAddress = currentAddress;
    int optype = (optypeByte >> 6) & 0x03;
    for (int i = 0; i < 4; i++) {
      
      optype = (optypeByte >> ((3 - i) * 2)) & 0x03;
      
      if (optype == Operand.TYPENUM_LARGE_CONSTANT) {
        
        operands.add(new Operand(optype,
            memaccess.readUnsignedShort(nextAddress)));
        nextAddress += 2;
        
      } else if (optype == Operand.TYPENUM_VARIABLE
          || optype == Operand.TYPENUM_SMALL_CONSTANT) {
        
        operands.add(new Operand(optype,
            memaccess.readUnsignedByte(nextAddress)));
        
        nextAddress += 1;
        
      } else {
        
        break;
      }
    }
    return nextAddress;
  }
  
  /**
   * Extracts a store variable if this instruction has one.
   * 
   * @param info the instruction info
   * @param currentAddress the current address in the decoding
   * @return the current decoding address after extraction
   */
  private int extractStoreVariable(InstructionInfo info, int currentAddress) {
    
    if (info.storesResult()) {
      
      info.setStoreVariable(memaccess.readUnsignedByte(currentAddress));
      return currentAddress + 1;
    }
    return currentAddress;
  }  
}
