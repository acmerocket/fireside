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

import org.zmpp.base.MemoryReadAccess;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.ZObject;


/**
 * This class represents instructions of type LONG, 2OP.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class LongInstruction extends AbstractInstruction {

  /**
   * The operand count.
   */
  private OperandCount operandCount;
  
  /**
   * Constructor.
   * 
   * @param machineState a reference to a MachineState object
   * @param opcode the instruction's opcode
   */
  public LongInstruction(Machine machineState, int opcode) {
    
    super(machineState, opcode);
    this.operandCount = OperandCount.C2OP;
  }
  
  /**
   * Constructor.
   * 
   * @param machineState the machine state
   * @param operandCount the operand count
   * @param opcode the opcode
   */
  public LongInstruction(Machine machineState,
      OperandCount operandCount, int opcode) {
    
    super(machineState, opcode);
    this.operandCount = operandCount;
  }
  
  /**
   * {@inheritDoc}
   */
  public InstructionResult doInstruction() {
   
    switch (getOpcode()) {
    
      case LongStaticInfo.OP_JE:
        je();
        break;
      case LongStaticInfo.OP_JL:
        jl();
        break;
      case LongStaticInfo.OP_JG:
        jg();
        break;
      case LongStaticInfo.OP_JIN:
        jin();
        break;
      case LongStaticInfo.OP_DEC_CHK:
        decChk();
        break;
      case LongStaticInfo.OP_INC_CHK:
        incChk();
        break;
      case LongStaticInfo.OP_TEST:
        test();
        break;
      case LongStaticInfo.OP_OR:
        or();
        break;
      case LongStaticInfo.OP_AND:
        and();
        break;
      case LongStaticInfo.OP_TEST_ATTR:
        testAttr();
        break;
      case LongStaticInfo.OP_SET_ATTR:
        setAttr();
        break;
      case LongStaticInfo.OP_CLEAR_ATTR:
        clearAttr();
        break;        
      case LongStaticInfo.OP_STORE:
        store();
        break;
      case LongStaticInfo.OP_INSERT_OBJ:
        insertObj();
        break;
      case LongStaticInfo.OP_LOADW:
        loadw();
        break;        
      case LongStaticInfo.OP_LOADB:
        loadb();
        break;        
      case LongStaticInfo.OP_GET_PROP:
        getProp();
        break;        
      case LongStaticInfo.OP_GET_PROP_ADDR:
        getPropAddr();
        break;        
      case LongStaticInfo.OP_GET_NEXT_PROP:
        getNextProp();
        break;        
      case LongStaticInfo.OP_ADD:
        add();
        break;
      case LongStaticInfo.OP_SUB:
        sub();
        break;
      case LongStaticInfo.OP_MUL:
        mul();
        break;
      case LongStaticInfo.OP_DIV:
        div();
        break;
      case LongStaticInfo.OP_MOD:
        mod();
        break;
      case LongStaticInfo.OP_CALL_2S:
        call(1);
        break;
      case LongStaticInfo.OP_CALL_2N:
        call(1);
        break;
      case LongStaticInfo.OP_SET_COLOUR:
        setColour();
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
  public InstructionForm getInstructionForm() { return InstructionForm.LONG; }
  
  /**
   * {@inheritDoc}
   */
  public OperandCount getOperandCount() { return operandCount; }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionStaticInfo getStaticInfo() {
    
    return LongStaticInfo.getInstance();
  }
  
  private void je() {
    
    boolean equalsFollowing = false;
    short op1 = getValue(0);
    if (getNumOperands() <= 1) {

      getMachine().halt("je expects at least two operands, only " +
                        "one provided");
    } else {
      
      for (int i = 1; i < getNumOperands(); i++) {
        
        if (op1 == getValue(i)) {
          
          equalsFollowing = true;
          break;
        }
      }
      branchOnTest(equalsFollowing);
    }
  }
    
  private void jl() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    branchOnTest(op1 < op2);
  }
  
  private void jg() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    branchOnTest(op1 > op2);
  }
  
  private void jin() {
    
    int obj1 = getUnsignedValue(0);
    int obj2 = getUnsignedValue(1);
    int parentOfObj1 = 0;
    
    if (obj1 > 0) {
      
      parentOfObj1 = getMachine().getObjectTree().getObject(obj1).getParent();

    } else {
      
      getMachine().warn("@jin illegal access to object " + obj1);
    }
    branchOnTest(parentOfObj1 == obj2);
  }
  
  private void decChk() {
    
    int varnum = getUnsignedValue(0);
    short value = getValue(1);
    short varValue = (short) (getMachine().getVariable(varnum) - 1);
    
    getMachine().setVariable(varnum, varValue);
    branchOnTest(varValue < value);
  }
  
  private void incChk() {
    
    int varnum = getUnsignedValue(0);
    short value = getValue(1);
    short varValue = (short) (getMachine().getVariable(varnum) + 1);
    
    getMachine().setVariable(varnum, varValue);
    branchOnTest(varValue > value);
  }
  
  private void test() {
    
    int op1 = getUnsignedValue(0);
    int op2 = getUnsignedValue(1);
    branchOnTest((op1 & op2) == op2);
  }
  
  private void or() {
    
    int op1 = getUnsignedValue(0);
    int op2 = getUnsignedValue(1);
    storeResult((short) ((op1 | op2) & 0xffff));
    nextInstruction();
  }
  
  private void and() {
    
    int op1 = getUnsignedValue(0);
    int op2 = getUnsignedValue(1);
    storeResult((short) ((op1 & op2) & 0xffff));
    nextInstruction();
  }
  
  private void add() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    storeResult((short) (op1 + op2));
    nextInstruction();
  }
  
  private void sub() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    storeResult((short) (op1 - op2));
    nextInstruction();
  }
  
  private void mul() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    storeResult((short)(op1 * op2));
    nextInstruction();
  }
  
  private void div() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    storeResult((short) (op1 / op2));
    nextInstruction();
  }
  
  private void mod() {
    
    short op1 = getValue(0);
    short op2 = getValue(1);
    storeResult((short) (op1 % op2));
    nextInstruction();
  }
  
  private void testAttr() {
    
    int obj = getUnsignedValue(0);
    int attr = getUnsignedValue(1);
    
    if (obj > 0) {
      
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
      branchOnTest(zobj.isAttributeSet(attr));
      
    } else {
      
      getMachine().warn("@test_attr illegal access to object " + obj);
      branchOnTest(false);
    }
  }
  
  private void setAttr() {
    
    int obj = getUnsignedValue(0);
    int attr = getUnsignedValue(1);
    if (obj > 0) {
      
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
      zobj.setAttribute(attr);
      
    } else {
      
      getMachine().warn("@set_attr illegal access to object " + obj);
    }
    nextInstruction();
  }
  
  private void clearAttr() {
    
    int obj = getUnsignedValue(0);
    int attr = getUnsignedValue(1);
    if (obj > 0) {
      
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
      zobj.clearAttribute(attr);
      
    } else {
      
      getMachine().warn("@clear_attr illegal access to object " + obj);
    }
    nextInstruction();
  }
  
  private void store() {
    
    int varnum = getUnsignedValue(0);
    short value = getValue(1);
    
    // Handle stack variable as a special case (standard 1.1)
    if (varnum == 0) {
      
      getMachine().setStackTopElement(value);
      
    } else {
      
      getMachine().setVariable(varnum, value);
    }
    nextInstruction();
  }
  
  private void insertObj() {
    
    int obj = getUnsignedValue(0);
    int dest = getUnsignedValue(1);
    ObjectTree objectTree = getMachine().getObjectTree();
    objectTree.insertObject(dest, obj);
    nextInstruction();
  }
  
  private void loadw() {
    
    int arrayAddress = getUnsignedValue(0);
    int index = getUnsignedValue(1);
    MemoryReadAccess memaccess = getMachine().getMemoryAccess();
    storeResult(memaccess.readShort(arrayAddress + 2 * index));
    nextInstruction();
  }
  
  private void loadb() {
    
    int arrayAddress = getUnsignedValue(0);
    int index = getUnsignedValue(1);
    MemoryReadAccess memaccess = getMachine().getMemoryAccess();
    storeResult((short) memaccess.readUnsignedByte(arrayAddress + index));
    nextInstruction();
  }
  
  private void getProp() {
    
    int obj = getUnsignedValue(0);
    int property = getUnsignedValue(1);
    
    if (obj > 0) {
      
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
      int numBytes = zobj.getPropertySize(property);
      short value;
    
      if (!zobj.isPropertyAvailable(property)) {
     
        // Retrieve and store default
        value = getMachine().getObjectTree().getPropertyDefault(property);
      
      } else if (numBytes == 1) {
      
        value = zobj.getPropertyByte(property, 0);
      
      } else {
      
        byte byte1 = zobj.getPropertyByte(property, 0);
        byte byte2 = zobj.getPropertyByte(property, 1);      
        value = (short) (byte1 << 8 | (byte2 & 0xff));
      }
      storeResult(value);
      
    } else {
      
      getMachine().warn("@get_prop illegal access to object " + obj);
    }
    nextInstruction();
  }
  
  private void getPropAddr() {
    
    int obj = getUnsignedValue(0);
    int property = getUnsignedValue(1);    
    
    if (obj > 0) {
      
      short value = 0;
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
    
      if (zobj.isPropertyAvailable(property)) {
      
        value = (short) (zobj.getPropertyAddress(property) & 0xffff);
      }
      storeResult(value);

    } else {
      
      getMachine().warn("@get_prop_addr illegal access to object " + obj);
    }
    
    nextInstruction();
  }
  
  private void getNextProp() {
    
    int obj = getUnsignedValue(0);
    int property = getUnsignedValue(1);
    short value = 0;
    
    if (obj > 0) {
      
      ZObject zobj = getMachine().getObjectTree().getObject(obj);
    
      if (property == 0 || zobj.isPropertyAvailable(property)) {
      
        value = (short) (zobj.getNextProperty(property) & 0xffff);
        storeResult(value);
        nextInstruction();
      
      } else {
      
        getMachine().halt("the property [" + property + "] of object [" + obj
                          + "] does not exist");
      }
      
    } else {
      
      // issue warning and continue
      getMachine().warn("@get_next_prop illegal access to object " + obj);
      nextInstruction();
    }
  }
  
  private void setColour() {
    
    getMachine().getScreen().setForegroundColor(getValue(0));
    getMachine().getScreen().setBackgroundColor(getValue(1));
    nextInstruction();
  }  
}
