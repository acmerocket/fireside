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
import org.zmpp.vm.PortableGameState;


public class ExtendedInstruction extends AbstractInstruction {

  /**
   * Constructor.
   * 
   * @param machine a Machine object
   * @param opcode the opcode
   */
  public ExtendedInstruction(Machine machine, int opcode) {
    
    super(machine, opcode);
  }
  
  /**
   * {@inheritDoc}
   */
  public InstructionForm getInstructionForm() {
    
    return InstructionForm.VARIABLE;
  }

  /**
   * {@inheritDoc}
   */
  public OperandCount getOperandCount() {
    
    return OperandCount.EXT;
  }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionStaticInfo getStaticInfo() {
    
    return ExtendedStaticInfo.getInstance();
  }
  
  public InstructionResult doInstruction() {

    switch (getOpcode()) {    

    case ExtendedStaticInfo.OP_SAVE:
      save();
      break;
    case ExtendedStaticInfo.OP_RESTORE:
      restore();
      break;
    case ExtendedStaticInfo.OP_LOG_SHIFT:
      log_shift();
      break;
    case ExtendedStaticInfo.OP_ART_SHIFT:
      art_shift();
      break;
    case ExtendedStaticInfo.OP_SET_FONT:
      set_font();
      break;
    case ExtendedStaticInfo.OP_SAVE_UNDO:
      save_undo();
      break;
    case ExtendedStaticInfo.OP_RESTORE_UNDO:
      restore_undo();
      break;
    case ExtendedStaticInfo.OP_PRINT_UNICODE:
      print_unicode();
      break;
    case ExtendedStaticInfo.OP_CHECK_UNICODE:
      check_unicode();
      break;
    default:
      throwInvalidOpcode();
      break;
    }

    // TODO
    return new InstructionResult(TRUE, false);
  }
  
  private void save_undo() {
    
    // Target PC offset is two because of the extra opcode byte and 
    // operand type byte compared to the 0OP instruction
    int pc = getMachine().getCpu().getProgramCounter() + 3;
    boolean success = getMachine().save_undo(pc);
    storeResult((short) (success ? TRUE : FALSE));
    nextInstruction();
  }
  
  private void restore_undo() {
    
    PortableGameState gamestate = getMachine().restore_undo();
    if (gamestate != null) {

      int storevar = gamestate.getStoreVariable(getMachine());      
      getCpu().setVariable(storevar, (short) RESTORE_TRUE);
      
    } else {
      
      storeResult((short) FALSE);
    }
  }
  
  private void art_shift() {
    
    short number = getValue(0);
    short places = getValue(1);
    number = (short) ((places >= 0) ? number << places : number >> (-places));
    storeResult(number);
    nextInstruction();
  }
  
  private void log_shift() {
    
    short number = getValue(0);
    short places = getValue(1);
    number = (short) ((places >= 0) ? number << places : number >>> (-places));
    storeResult(number);
    nextInstruction();
  }
  
  private void set_font() {

    int previousFont = getMachine().getScreen().setFont(getValue(0));
    storeResult((short) previousFont);
    nextInstruction();
  }
  
  private void save() {

    // Saving to tables is not supported yet, this is the standard save feature
    // Offset is 3 because there are two opcode bytes + 1 optype byte before
    // the actual store var byte
    saveToStorage(getMachine().getCpu().getProgramCounter() + 3);
  }
  
  private void restore() {

    // Reading from tables is not supported yet, this is the standard 
    // restore feature
    restoreFromStorage();
  }
  
  private void print_unicode() {
 
    short zchar = getValue(0);
    getMachine().getOutput().printZsciiChar(zchar, false);
    nextInstruction();
  }
  
  private void check_unicode() {

    // always return true, set bit 0 for can print and bit 1 for
    // can read
    storeResult((short) 3);
    nextInstruction();
  }
}
