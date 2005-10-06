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
import org.zmpp.vm.AbstractInstruction.InstructionForm;
import org.zmpp.vm.AbstractInstruction.OperandCount;

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
   * The machine state object.
   */
  private MachineState machineState;
  
  /**
   * Constructor.
   * 
   * @param memaccess the memory access object
   */
  public InstructionDecoder(MachineState machineState,
                            MemoryReadAccess memaccess) {
  
    this.memaccess = memaccess;
    this.machineState = machineState;
  }
  
  /**
   * Decode the instruction at the specified address.
   * 
   * @param instructionAddress the current instruction's address
   * @return the instruction at the specified address
   */
  public AbstractInstruction decodeInstruction(int instructionAddress) {
  
    AbstractInstruction info = createBasicInstructionInfo(instructionAddress);
    int currentAddress = extractOperands(info, instructionAddress);
    currentAddress = extractStoreVariable(info, currentAddress);
    currentAddress = extractBranchOffset(info, currentAddress);
    info.setLength(currentAddress - instructionAddress);    
    return info;
  }
  
  /**
   * Decodes the routine at the specified address and returns an
   * RoutineInfo object which describes the routine.
   * 
   * @param routineAddress the start address of the encoded routine
   * @return a RoutineInfo object describing the routine
   */
  public RoutineContext decodeRoutine(int routineAddress) {
    
    short numLocals = memaccess.readUnsignedByte(routineAddress);
    int[] locals = new int[numLocals];
    int currentAddress = routineAddress + 1;
    for (int i = 0; i < numLocals; i++) {
      
      locals[i] = memaccess.readUnsignedShort(currentAddress);
      currentAddress += 2;
    }
    
    RoutineContext info = new RoutineContext(currentAddress, numLocals);
    for (int i = 0; i < numLocals; i++) {
      
      info.setLocalVariable(i, locals[i]);
    }
    return info;
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
  private AbstractInstruction createBasicInstructionInfo(int instructionAddress) {
    
    OperandCount operandCount;
    int opcode;
    short firstByte = memaccess.readUnsignedByte(instructionAddress);
    
    // Determine form and operand count type
    if (0x00 <= firstByte && firstByte <= 0x7f) {
      
      opcode = firstByte & 0x1f; // Bottom five bits contain the opcode number
      operandCount = OperandCount.C2OP;
      return new LongInstruction(machineState, opcode);

    } else if (0x80 <= firstByte && firstByte <= 0xbf) {
      
      opcode = firstByte & 0x0f; // Bottom four bits contain the opcode number
      operandCount = (firstByte >= 0xb0) ? OperandCount.C0OP :
                                           OperandCount.C1OP;
      if (operandCount == OperandCount.C0OP) {
        
        // Special case: print and print_ret are classified as C0OP, but
        // in fact have a string literal as their parameter
        if (opcode == PrintLiteralInstruction.OP_PRINT
            || opcode == PrintLiteralInstruction.OP_PRINT_RET) {
          
          return new PrintLiteralInstruction(machineState, opcode, memaccess,
                                             instructionAddress);
        }
        return new Short0Instruction(machineState, opcode);
      }
      else
        return new Short1Instruction(machineState, opcode);
      
    } else {
      
      opcode = firstByte & 0x1f; // Bottom five bits contain the opcode number
      operandCount = (firstByte >= 0xe0) ? OperandCount.VAR : OperandCount.C2OP;
      return new VariableInstruction(machineState, operandCount, opcode);
    }
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
  private int extractOperands(AbstractInstruction info, int instructionAddress) {

    int currentAddress = instructionAddress;
    
    if (info.getInstructionForm() == InstructionForm.SHORT) {
      
      if (info.getOperandCount() == OperandCount.C1OP) {
        
        short firstByte = memaccess.readUnsignedByte(instructionAddress);
        /*
        short secondByte = memaccess.readUnsignedByte(instructionAddress + 1);
        System.out.printf("opcode: %x, firstByte: %x, secondByte: %x\n",
            info.getOpcode(), firstByte, secondByte);
            */
        byte optype = (byte) ((firstByte & 0x30) >> 4);
        //System.out.printf("optype: %x\n", optype);
        
        currentAddress = extractOperand(info, optype, instructionAddress + 1);
        
      } else {
        
        // 0 operand instructions of course still occupy 1 byte
        currentAddress = instructionAddress + 1;
      }
    } else if (info.getInstructionForm() == InstructionForm.LONG) {

      short firstByte = memaccess.readUnsignedByte(instructionAddress);      
      //System.out.printf("long opcode: %x\n", firstByte);
      byte optype1 = ((firstByte & 0x40) > 0) ? Operand.TYPENUM_VARIABLE :
                                                Operand.TYPENUM_SMALL_CONSTANT;
      byte optype2 = ((firstByte & 0x20) > 0) ? Operand.TYPENUM_VARIABLE :
                                                Operand.TYPENUM_SMALL_CONSTANT;
      currentAddress = extractOperand(info, optype1, instructionAddress + 1);
      currentAddress = extractOperand(info, optype2, currentAddress);
      
    } else if (info.getInstructionForm() == InstructionForm.VARIABLE){
    
      short secondByte = memaccess.readUnsignedByte(instructionAddress + 1);
          
      // operand types in next byte(s)
      currentAddress = instructionAddress + 2;    
      currentAddress = extractOperandsWithTypeByte(info, secondByte,
                                                 currentAddress);
    }
    return currentAddress;
  }
  
  /**
   * Extract operands for the given optype byte value starting at the given
   * decoding address. This is outfactored in order to be called at least two
   * times. The generated operands are added to the specified operand list.
   * 
   * @param info the InstructionInfo to add to
   * @param optypeByte the optype byte
   * @param currentAddress the current decoding address
   * @return the new decoding address after extracting the operands
   */
  private int extractOperandsWithTypeByte(AbstractInstruction info,
                                          int optypeByte, int currentAddress) {
    
    int nextAddress = currentAddress;
    int oldNumOperands;
    byte optype = (byte) ((optypeByte >> 6) & 0x03);
    
    for (int i = 0; i < 4; i++) {
      
      optype = (byte) ((optypeByte >> ((3 - i) * 2)) & 0x03);
      oldNumOperands = info.getNumOperands();
      nextAddress = extractOperand(info, optype, nextAddress);
      if (info.getNumOperands() == oldNumOperands) break;
    }
    return nextAddress;
  }
  
  /**
   * Extracts an operands from the current address with the specified operand
   * type number. If the type is unknown or OMITTED, no operand will be
   * added and the returned address will be equal to currentAddress.
   * 
   * @param info the instruction info to add to
   * @param optype the operand type number
   * @param currentAddress the current address in the instruction
   * @return the next address
   */
  private int extractOperand(AbstractInstruction info, byte optype,
                             int currentAddress) {
    //if (info.getInstructionForm() == InstructionForm.SHORT)
    //  System.out.printf("extractOperand() from address: %x, optype: %d\n",
    //      currentAddress, optype);
    
    int nextAddress = currentAddress;
    if (optype == Operand.TYPENUM_LARGE_CONSTANT) {
      
      info.addOperand(new Operand(optype,
          memaccess.readUnsignedShort(nextAddress)));
      nextAddress += 2;
      
    } else if (optype == Operand.TYPENUM_VARIABLE
        || optype == Operand.TYPENUM_SMALL_CONSTANT) {
      
      info.addOperand(new Operand(optype,
          memaccess.readUnsignedByte(nextAddress)));
      
      nextAddress += 1; 
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
  private int extractStoreVariable(AbstractInstruction info, int currentAddress) {
    
    if (info.storesResult()) {
      
      info.setStoreVariable(memaccess.readUnsignedByte(currentAddress));
      return currentAddress + 1;
    }
    return currentAddress;
  }
  
  /**
   * Extracts the branch offset if this instruction is a branch.
   * 
   * @param info the instruction info object
   * @param currentAddress the current address in decoding processing
   * @return the current decoding address after extraction
   */
  private int extractBranchOffset(AbstractInstruction info, int currentAddress) {
    
    if (info.isBranch()) {
      
      short offsetByte1 = memaccess.readUnsignedByte(currentAddress);
      info.setBranchIfTrue((offsetByte1 & 0x80) > 0);
      
      // Bit 6 set -> only one byte needs to be read
      if ((offsetByte1 & 0x40) > 0) {
        
        info.setBranchOffset(offsetByte1 & 0x3f);
        return currentAddress + 1;
        
      } else {
        
        // Join two bytes to a 14-bit offset
        short offsetByte2 = memaccess.readUnsignedByte(currentAddress + 1);
        info.setBranchOffset(((offsetByte1 & 0x3f) << 8) | offsetByte2);
        return currentAddress + 2;
      }
    }
    return currentAddress;
  }
}
