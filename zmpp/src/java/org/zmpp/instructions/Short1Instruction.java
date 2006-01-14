/*
 * $Id$
 * 
 * Created on 10/03/2005
 * Copyright 2005-2006 by Wei-ju Wu
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

import org.zmpp.vm.Machine;
import org.zmpp.vm.ZObject;


/**
 * This class represents instructions of type SHORT, 1OP.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Short1Instruction extends AbstractInstruction {

  /**
   * Constructor.
   *  
   * @param machineState a reference to the MachineState object
   * @param opcode the instruction's opcode
   */
  public Short1Instruction(Machine machineState, int opcode) {
    
    super(machineState, opcode);
  }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionResult doInstruction() {
    
    switch (getOpcode()) {
      case Short1StaticInfo.OP_JZ:
        jz();
        break;
      case Short1StaticInfo.OP_GET_SIBLING:
        getSibling();
        break;
      case Short1StaticInfo.OP_GET_CHILD:
        getChild();
        break;
      case Short1StaticInfo.OP_GET_PARENT:
        getParent();
        break;
      case Short1StaticInfo.OP_GET_PROP_LEN:
        getPropLen();
        break;
      case Short1StaticInfo.OP_INC:        
        inc();
        break;
      case Short1StaticInfo.OP_DEC:
        dec();
        break;
      case Short1StaticInfo.OP_PRINT_ADDR:
        printAddr();
        break;        
      case Short1StaticInfo.OP_REMOVE_OBJ:
        removeObj();
        break;        
      case Short1StaticInfo.OP_PRINT_OBJ:
        printObj();
        break;        
      case Short1StaticInfo.OP_JUMP:
        jump();
        break;
      case Short1StaticInfo.OP_RET:
        ret();
        break;
      case Short1StaticInfo.OP_PRINT_PADDR:
        printPaddr();
        break;
      case Short1StaticInfo.OP_LOAD:
        load();
        break;
      case Short1StaticInfo.OP_NOT:
        if (getStoryFileVersion() <= 4) {
          
          not();
          
        } else {
          
          call1n();
        }
        break;
      case Short1StaticInfo.OP_CALL_1S:
        call1s();
        break;
      default:
        throwInvalidOpcode();
    }
    
    // TODO
    return new InstructionResult(TRUE, false);
  }

  /**
   * {@inheritDoc}
   */
  public InstructionForm getInstructionForm() {
    
    return InstructionForm.SHORT;
  }
  
  /**
   * {@inheritDoc}
   */
  public OperandCount getOperandCount() {
    
    return OperandCount.C1OP;
  }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionStaticInfo getStaticInfo() {

    return Short1StaticInfo.getInstance();
  }  
  
  /**
   * inc instruction.
   */
  private void inc() {
    
    short varNum = getValue(0);
    short value = getMachine().getVariable(varNum);
    getMachine().setVariable(varNum, (short) (value + 1));
    nextInstruction();
  }
  
  /**
   * dec instruction.
   */
  private void dec() {
    
    short varNum = getValue(0);
    short value = (short) getMachine().getVariable(varNum);
    getMachine().setVariable(varNum, (short) (value - 1));
    nextInstruction();
  }

  /**
   * not instruction.
   */
  private void not()  {
	
	  int notvalue = ~getUnsignedValue(0);
	  storeResult((short) (notvalue & 0xffff));
	  nextInstruction();
  }
  
  /**
   * jump instruction. The offset can be negative.
   */
  private void jump() {
    
    // Unconditional jump
    getMachine().setProgramCounter(
        getMachine().getProgramCounter() + getValue(0) + 1);
  }
  
  /**
   * load instruction.
   */
  private void load() {
    
    int varnum = getValue(0);
    short value = varnum == 0 ? getMachine().getStackTopElement() :
                                getMachine().getVariable(varnum);
    storeResult(value);
    nextInstruction();    
  }
  
  /**
   * jz instruction.
   */
  private void jz() {

    branchOnTest(getValue(0) == 0);
  }
  
  /**
   * get_parent instruction.
   */
  private void getParent() {

    int obj = getUnsignedValue(0);
    int parent = 0;
    if (obj > 0) {
      
      parent = getObjectTree().getObject(obj).getParent();

    } else {
      
      getMachine().warn("@get_parent illegal access to object " + obj);
    }
    storeResult((short) (parent & 0xffff));
    nextInstruction();
  }
  
  private void getSibling() {

    int obj = getUnsignedValue(0);
    int sibling = 0;
    if (obj > 0) {
      
      sibling = getObjectTree().getObject(obj).getSibling();
      
    } else {
      
      getMachine().warn("@get_sibling illegal access to object " + obj);
    }
    storeResult((short) (sibling & 0xffff));
    branchOnTest(sibling > 0);
  }
  
  private void getChild() {

    int obj = getUnsignedValue(0);
    int child = 0;
    if (obj > 0) {
      
      child = getObjectTree().getObject(obj).getChild();

    } else {
      
      getMachine().warn("@get_child illegal access to object " + obj);
    }
    storeResult((short) (child & 0xffff));
    branchOnTest(child > 0);
  }

  private void printAddr() {
   
    getMachine().printZString(getUnsignedValue(0));
    nextInstruction();
  }
  
  private void printPaddr() {
    
    getMachine().printZString(
        getMachine().translatePackedAddress(getUnsignedValue(0), false));
    nextInstruction();
  }
  
  private void ret() {
    
    returnFromRoutine(getValue(0));
  }
  
  private void printObj() {
    
    int obj = getUnsignedValue(0);
    if (obj > 0) {
      
      ZObject zobj = getObjectTree().getObject(obj);
      getMachine().printZString(zobj.getPropertiesDescriptionAddress());
      
    } else {
      
      getMachine().warn("@print_obj illegal access to object " + obj);
    }
    nextInstruction();
  }
  
  private void removeObj() {
    
    int obj = getUnsignedValue(0);
    if (obj > 0) {
      
      getObjectTree().removeObject(obj);
    }
    nextInstruction();
  }  

  private void getPropLen() {
    
    int propertyAddress = getUnsignedValue(0);    
    short proplen = (short)
      getObjectTree().getPropertyLength(propertyAddress);
    storeResult(proplen);
    nextInstruction();
  }
  
  private void call1s() {
    
    call(0);
  }
  
  private void call1n() {
    
    call(0);
  }
    
}
