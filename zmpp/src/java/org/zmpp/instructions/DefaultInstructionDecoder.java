package org.zmpp.instructions;

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.instructions.AbstractInstruction.InstructionForm;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;
import org.zmpp.vm.Operand;

public class DefaultInstructionDecoder implements InstructionDecoder {
  
  /**
   * The memory access object.
   */
  private MemoryReadAccess memaccess;
  
  /**
   * The machine state object.
   */
  private Machine machineState;
  
  /**
   * Constructor.
   * 
   * @param memaccess the memory access object
   */
  public DefaultInstructionDecoder() {
  
  }
  
  public void initialize(Machine machine, MemoryReadAccess memaccess) {
    
    this.memaccess = memaccess;
    this.machineState = machine;
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
    if (info.getInstructionForm() == InstructionForm.VARIABLE
        && info.getOperandCount() == OperandCount.C2OP) {
      
      // Handle the VAR form of C2OP instructions here
      AbstractInstruction info2 =
        new LongInstruction(machineState, OperandCount.VAR, info.getOpcode());
      
      for (int i = 0; i < info.getNumOperands(); i++) {
      
        info2.addOperand(info.getOperand(i));
      }
      info = info2;
    }
    currentAddress = extractStoreVariable(info, currentAddress);
    currentAddress = extractBranchOffset(info, currentAddress);
    info.setLength(currentAddress - instructionAddress);    
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
    if (firstByte == 0xbe) {
            
      opcode = memaccess.readUnsignedByte(instructionAddress + 1);
      return new ExtendedInstruction(machineState, opcode);
      
    } else if (0x00 <= firstByte && firstByte <= 0x7f) {
      
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
        if (opcode == Short1StaticInfo.OP_PRINT
            || opcode == Short1StaticInfo.OP_PRINT_RET) {
          
          return new PrintLiteralInstruction(machineState, opcode, memaccess,
                                             instructionAddress);
        }
        return new Short0Instruction(machineState, opcode);
        
      } else {
        
        return new Short1Instruction(machineState, opcode);
      }
      
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
        
        short firstByte = memaccess.readUnsignedByte(currentAddress);
        byte optype = (byte) ((firstByte & 0x30) >> 4);
        
        currentAddress = extractOperand(info, optype, currentAddress + 1);
        
      } else {
        
        // 0 operand instructions of course still occupy 1 byte
        currentAddress++;
      }
      
    } else if (info.getInstructionForm() == InstructionForm.LONG) {

      short firstByte = memaccess.readUnsignedByte(instructionAddress);      
      byte optype1 = ((firstByte & 0x40) > 0) ? Operand.TYPENUM_VARIABLE :
                                                Operand.TYPENUM_SMALL_CONSTANT;
      byte optype2 = ((firstByte & 0x20) > 0) ? Operand.TYPENUM_VARIABLE :
                                                Operand.TYPENUM_SMALL_CONSTANT;
      currentAddress = extractOperand(info, optype1, currentAddress + 1);
      currentAddress = extractOperand(info, optype2, currentAddress);
      
    } else if (info.getInstructionForm() == InstructionForm.VARIABLE) {
    
      // The operand types start after the second opcode byte in EXT form,
      // and after the first otherwise
      currentAddress += (info.getOperandCount() == OperandCount.EXT) ? 2 : 1;      
      short optypeByte1 = memaccess.readUnsignedByte(currentAddress++);
      short optypeByte2 = 0;
                
      // Extract more operands if necessary, if the opcode
      // is call_vs2 or call_vn2 and there are four operands already,
      // there is a need to check out the second op type byte
      // (Standards document 1.0, S 4.4.3.1 and S 4.5.1)
      // Note: we need to make sure that OperandCount is VAR, because
      // ----- the opcode for CALL_VN2 overlaps with CALL_2N
      boolean isVcall = false;      
      if (info.getOperandCount() == OperandCount.VAR
          && (info.getOpcode() == VariableStaticInfo.OP_CALL_VS2
              || info.getOpcode() == VariableStaticInfo.OP_CALL_VN2)) {
        
        // There is a second op type byte
        isVcall = true;
        optypeByte2 = memaccess.readUnsignedByte(currentAddress++);
      }
      
      // Extract first operand half
      currentAddress = extractOperandsWithTypeByte(info, optypeByte1,
                                                   currentAddress);
      
      // Extract second operand half
      if (isVcall && info.getNumOperands() == 4) {
        
        currentAddress = extractOperandsWithTypeByte(info, optypeByte2,
                                                     currentAddress);
      }
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
    
    int nextAddress = currentAddress;
    if (optype == Operand.TYPENUM_LARGE_CONSTANT) {
      
      info.addOperand(new Operand(optype,
          memaccess.readShort(nextAddress)));
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
        
        info.setBranchOffset((short) (offsetByte1 & 0x3f));
        return currentAddress + 1;
        
      } else {
     
        short offsetByte2 = memaccess.readUnsignedByte(currentAddress + 1);
        short offset;
        
        if ((offsetByte1 & 0x20) != 0) { // Bit 14 set = negative
          
          offset = (short)
            ((0xC000 | ((offsetByte1 << 8) | (offsetByte2 & 0xff))));
          
        } else {
          
          offset = (short)
            (((offsetByte1 & 0x3f) << 8) | (offsetByte2 & 0xff));
        }
        info.setBranchOffset(offset);
        return currentAddress + 2;
      }
    }
    return currentAddress;
  }
}
