/*
 * Created on 2008/07/24
 * Copyright 2005-2009 by Wei-ju Wu
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

import org.zmpp.vm.Machine;

/**
 * Instruction of form 0Op.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class C0OpInstruction extends AbstractInstruction {
  private String str;
  public C0OpInstruction(Machine machine, int opcodeNum,
                         Operand[] operands, String str, char storeVar,
                         BranchInfo branchInfo, int opcodeLength) {
    super(machine, opcodeNum, operands, storeVar, branchInfo, opcodeLength);
    this.str = str;
  }
  
  protected OperandCount getOperandCount() { return OperandCount.C0OP; }
  
  public void execute() {
    
    switch (getOpcodeNum()) {

      case C0OP_RTRUE:
        returnFromRoutine(TRUE);
        break;
      case C0OP_RFALSE:
        returnFromRoutine(FALSE);
        break;
      case C0OP_PRINT:
        getMachine().print(str);
        nextInstruction();
        break;
      case C0OP_PRINT_RET:
        getMachine().print(str);
        getMachine().newline();
        returnFromRoutine(TRUE);
        break;
      case C0OP_NOP:
        nextInstruction();
        break;
      case C0OP_SAVE:
        saveToStorage(getMachine().getPC() + 1);
        break;
      case C0OP_RESTORE:        
        restoreFromStorage();
        break;
      case C0OP_RESTART:
        getMachine().restart();
        break;
      case C0OP_QUIT:
        getMachine().quit();
        break;
      case C0OP_RET_POPPED:        
        returnFromRoutine(getMachine().getVariable((char) 0));
        break;
      case C0OP_POP:
        if (getMachine().getVersion() < 5) {
          pop();
        } else {
          z_catch();
        }
        break;
      case C0OP_NEW_LINE:
        getMachine().newline();
        nextInstruction();
        break;
      case C0OP_SHOW_STATUS:
        getMachine().updateStatusLine();          
        nextInstruction();
        break;
      case C0OP_VERIFY:
        branchOnTest(getMachine().hasValidChecksum());
        break;
      case C0OP_PIRACY:
        branchOnTest(true);
        break;
      default:        
        throwInvalidOpcode();    
    }
  }
  
  private boolean isPrint() {
    return InstructionInfoDb.getInstance().getInfo(getOperandCount(),
            getOpcodeNum(), getStoryVersion()).isPrint();
  }

  @Override
  protected String getOperandString() {
    if (isPrint()) {
      return String.format("\"%s\"", str);
    }
    return super.getOperandString();
  }

  private void pop() {
    getMachine().getVariable((char) 0);
    nextInstruction();    
  }
  
  private void z_catch() {
    // Stores the index of the current stack frame
    storeUnsignedResult((char) (getMachine().getRoutineContexts().size() - 1));
    nextInstruction();
  }
}
