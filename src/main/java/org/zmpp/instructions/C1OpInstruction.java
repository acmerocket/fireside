/*
 * Created on 2008/07/24
 * Copyright (c) 2005-2010, Wei-ju Wu.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of Wei-ju Wu nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.zmpp.instructions;

import org.zmpp.vm.Machine;

/**
 * Implementation of 1OP instructions.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class C1OpInstruction extends AbstractInstruction {

  public C1OpInstruction(Machine machine, int opcodeNum,
                         Operand[] operands, char storeVar,
                         BranchInfo branchInfo, int opcodeLength) {
    super(machine, opcodeNum, operands, storeVar, branchInfo, opcodeLength);
  }

  protected OperandCount getOperandCount() { return OperandCount.C1OP; }

  public void execute() {
    switch (getOpcodeNum()) {
      case C1OP_JZ:
        jz();
        break;
      case C1OP_GET_SIBLING:
        get_sibling();
        break;
      case C1OP_GET_CHILD:
        get_child();
        break;
      case C1OP_GET_PARENT:
        get_parent();
        break;
      case C1OP_GET_PROP_LEN:
        get_prop_len();
        break;
      case C1OP_INC:
        inc();
        break;
      case C1OP_DEC:
        dec();
        break;
      case C1OP_PRINT_ADDR:
        print_addr();
        break;
      case C1OP_REMOVE_OBJ:
        remove_obj();
        break;
      case C1OP_PRINT_OBJ:
        print_obj();
        break;
      case C1OP_JUMP:
        jump();
        break;
      case C1OP_RET:
        ret();
        break;
      case C1OP_PRINT_PADDR:
        print_paddr();
        break;
      case C1OP_LOAD:
        load();
        break;
      case C1OP_NOT:
        if (getStoryVersion() <= 4) {
          not();
        } else {
          call_1n();
        }
        break;
      case C1OP_CALL_1S:
        call_1s();
        break;
      default:
        throwInvalidOpcode();
    }
  }

  private void inc() {
    final char varNum = getUnsignedValue(0);
    final short value = getSignedVarValue(varNum);
    setSignedVarValue(varNum, (short) (value + 1));
    nextInstruction();
  }

  private void dec() {
    final char varNum = getUnsignedValue(0);
    final short value = getSignedVarValue(varNum);
    setSignedVarValue(varNum, (short) (value - 1));
    nextInstruction();
  }

  private void not()  {
    final int notvalue = ~getUnsignedValue(0);
    storeUnsignedResult((char) (notvalue & 0xffff));
    nextInstruction();
  }

  private void jump() {
    getMachine().incrementPC(getSignedValue(0) + 1);
  }

  private void load() {
    final char varnum = getUnsignedValue(0);
    final char value = varnum == 0 ? getMachine().getStackTop() :
      getMachine().getVariable(varnum);
    storeUnsignedResult(value);
    nextInstruction();
  }

  private void jz() {
    branchOnTest(getUnsignedValue(0) == 0);
  }

  private void get_parent() {
    final int obj = getUnsignedValue(0);
    int parent = 0;
    if (obj > 0) {
      parent = getMachine().getParent(obj);
    } else {
      getMachine().warn("@get_parent illegal access to object " + obj);
    }
    storeUnsignedResult((char) (parent & 0xffff));
    nextInstruction();
  }

  private void get_sibling() {
    final int obj = getUnsignedValue(0);
    int sibling = 0;
    if (obj > 0) {
      sibling = getMachine().getSibling(obj);
    } else {
      getMachine().warn("@get_sibling illegal access to object " + obj);
    }
    storeUnsignedResult((char) (sibling & 0xffff));
    branchOnTest(sibling > 0);
  }

  private void get_child() {
    final int obj = getUnsignedValue(0);
    int child = 0;
    if (obj > 0) {
      child = getMachine().getChild(obj);
    } else {
      getMachine().warn("@get_child illegal access to object " + obj);
    }
    storeUnsignedResult((char) (child & 0xffff));
    branchOnTest(child > 0);
  }

  private void print_addr() {
    getMachine().printZString(getUnsignedValue(0));
    nextInstruction();
  }

  private void print_paddr() {
    getMachine().printZString(
        getMachine().unpackStringAddress(getUnsignedValue(0)));
    nextInstruction();
  }

  private void ret() {
    returnFromRoutine(getUnsignedValue(0));
  }

  private void print_obj() {
    final int obj = getUnsignedValue(0);
    if (obj > 0) {
      getMachine().printZString(
        getMachine().getPropertiesDescriptionAddress(obj));
    } else {
      getMachine().warn("@print_obj illegal access to object " + obj);
    }
    nextInstruction();
  }

  private void remove_obj() {
    final int obj = getUnsignedValue(0);
    if (obj > 0) {
      getMachine().removeObject(obj);
    }
    nextInstruction();
  }

  private void get_prop_len() {
    final int propertyAddress = getUnsignedValue(0);
    final char proplen = (char)
      getMachine().getPropertyLength(propertyAddress);
    storeUnsignedResult(proplen);
    nextInstruction();
  }
  private void call_1s() { call(0); }
  private void call_1n() { call(0); }
}
