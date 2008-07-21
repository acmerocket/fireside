/*
 * $Id$
 * 
 * Created on 10/03/2005
 * Copyright 2005-2008 by Wei-ju Wu
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


import org.zmpp.base.Memory;
import static org.zmpp.base.MemoryUtil.toUnsigned16;
import org.zmpp.blorb.BlorbImage.Resolution;
import org.zmpp.vm.Machine;
import org.zmpp.vm.PortableGameState;
import org.zmpp.vm.ScreenModel;
import static org.zmpp.base.MemoryUtil.signedToUnsigned16;


/**
 * Class implementing the EXT instruction form.
 * @author Wei-ju Wu
 * @version 1.5
 */
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
  public OperandCount getOperandCount() { return OperandCount.EXT; }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionStaticInfo getStaticInfo() {
    
    return ExtendedStaticInfo.getInstance();
  }
  
  /**
   * {@inheritDoc}
   */
  public void doInstruction() {
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
    case ExtendedStaticInfo.OP_MOUSE_WINDOW:
      mouse_window();
      break;
    case ExtendedStaticInfo.OP_PICTURE_DATA:
      picture_data();
      break;
    case ExtendedStaticInfo.OP_DRAW_PICTURE:
      draw_picture();
      break;
    case ExtendedStaticInfo.OP_ERASE_PICTURE:
      erase_picture();
      break;
    case ExtendedStaticInfo.OP_MOVE_WINDOW:
      move_window();
      break;
    case ExtendedStaticInfo.OP_WINDOW_SIZE:
      window_size();
      break;
    case ExtendedStaticInfo.OP_WINDOW_STYLE:
      window_style();
      break;
    case ExtendedStaticInfo.OP_SET_MARGINS:
      set_margins();
      break;
    case ExtendedStaticInfo.OP_GET_WIND_PROP:
      get_wind_prop();
      break;
    case ExtendedStaticInfo.OP_PICTURE_TABLE:
      picture_table();
      break;
    case ExtendedStaticInfo.OP_PUT_WIND_PROP:
      put_wind_prop();
      break;
    case ExtendedStaticInfo.OP_PUSH_STACK:
      push_stack();
      break;
    case ExtendedStaticInfo.OP_POP_STACK:
      pop_stack();
      break;
    case ExtendedStaticInfo.OP_READ_MOUSE:
      read_mouse();
      break;
    case ExtendedStaticInfo.OP_SCROLL_WINDOW:
      scroll_window();
      break;
    default:
      throwInvalidOpcode();
      break;
    }
  }
  
  private void save_undo() {
    // Target PC offset is two because of the extra opcode byte and 
    // operand type byte compared to the 0OP instruction
    final int pc = getMachine().getPC() + 3;
    final boolean success = getMachine().save_undo(pc);
    storeResult(success ? TRUE : FALSE);
    nextInstruction();
  }
  
  private void restore_undo() {    
    final PortableGameState gamestate = getMachine().restore_undo();
    if (gamestate == null) {
      storeResult(FALSE);
    } else {
      final int storevar = gamestate.getStoreVariable(getMachine());      
      getMachine().setVariable(storevar, RESTORE_TRUE);      
    }
  }
  
  private void art_shift() {
    short number = getSignedValue(0);
    final short places = getSignedValue(1);
    number = (short) ((places >= 0) ? number << places : number >> (-places));
    storeResult(signedToUnsigned16(number));
    nextInstruction();
  }
  
  private void log_shift() {
    char number = getUnsignedValue(0);
    final short places = getSignedValue(1);
    number = (char) ((places >= 0) ? number << places : number >>> (-places));
    storeResult(number);
    nextInstruction();
  }
  
  private void set_font() {
    final char previousFont =
      getMachine().getScreen().setFont(getUnsignedValue(0));
    storeResult(previousFont);
    nextInstruction();
  }
  
  private void save() {
    // Saving to tables is not supported yet, this is the standard save feature
    // Offset is 3 because there are two opcode bytes + 1 optype byte before
    // the actual store var byte
    saveToStorage(getMachine().getPC() + 3);
  }
  
  private void restore() {
    // Reading from tables is not supported yet, this is the standard 
    // restore feature
    restoreFromStorage();
  }
  
  private void print_unicode() {
    final char zchar = (char) getUnsignedValue(0);
    getMachine().printZsciiChar(zchar);
    nextInstruction();
  }
  
  private void check_unicode() {
    // always return true, set bit 0 for can print and bit 1 for
    // can read
    storeResult((char) 3);
    nextInstruction();
  }
  
  private void mouse_window() {
    getMachine().getScreen6().setMouseWindow(getSignedValue(0));
    nextInstruction();
  }
  
  private void picture_data() {
    final int picnum = getUnsignedValue(0);
    final int array = getUnsignedValue(1);
    boolean result = false;
    
    if (picnum == 0) {
      writePictureFileInfo(array);
      // branch if any pictures are available: this information is only
      // available in the 1.1 spec
      result = getMachine().getPictureManager().getNumPictures() > 0;  
    } else {
      final Resolution picdim =
        getMachine().getPictureManager().getPictureSize(picnum);
      if (picdim != null) {
        final Memory memory = getMemory();
        memory.writeUnsigned16(array, toUnsigned16(picdim.getHeight()));
        memory.writeUnsigned16(array + 2, toUnsigned16(picdim.getWidth()));
        result = true;
      }
    }
    branchOnTest(result);
  }
  
  private void writePictureFileInfo(final int array) {
    final Memory memory = getMemory();
    memory.writeUnsigned16(array,
        toUnsigned16(getMachine().getPictureManager().getNumPictures()));
    memory.writeUnsigned16(array + 2,
        toUnsigned16(getMachine().getPictureManager().getRelease()));
  }
  
  private void draw_picture() {
    final int picnum = getUnsignedValue(0);
    int x = 0, y = 0;
    
    if (getNumOperands() > 1) {
      y = getUnsignedValue(1);
    }
    
    if (getNumOperands() > 2) {
      x = getUnsignedValue(2);
    }
    getMachine().getScreen6().getSelectedWindow().drawPicture(
        getMachine().getPictureManager().getPicture(picnum), y, x);
    nextInstruction();
  }

  private void erase_picture() {
    final int picnum = getUnsignedValue(0);
    int x = 1, y = 1;
    
    if (getNumOperands() > 1) {
      y = getUnsignedValue(1);
    }
    
    if (getNumOperands() > 2) {
      x = getUnsignedValue(2);
    }
    getMachine().getScreen6().getSelectedWindow().erasePicture(
        getMachine().getPictureManager().getPicture(picnum), y, x);
    nextInstruction();
  }
  
  private void move_window() {
    getMachine().getScreen6().getWindow(getUnsignedValue(0)).move(
        getUnsignedValue(1), getUnsignedValue(2));
    nextInstruction();
  }
  
  private void window_size() {
    final short window = getSignedValue(0);
    final char height = getUnsignedValue(1);
    final char width = getUnsignedValue(2);
    getMachine().getScreen6().getWindow(window).setSize(height, width);
    nextInstruction();
  }
  
  private void window_style() {
    int operation = 0;
    if (getNumOperands() > 2) {
      operation = getUnsignedValue(2);
    }    
    getWindow(getSignedValue(0)).setStyle(getUnsignedValue(1), operation);
    nextInstruction();
  }
  
  private void set_margins() {
    int window = ScreenModel.CURRENT_WINDOW;
    if (getNumOperands() == 3) {      
      window = getSignedValue(2);
    }
    getWindow(window).setMargins(getUnsignedValue(0), getUnsignedValue(1));
    nextInstruction();
  }
  
  private void get_wind_prop() {
    int window = getSignedValue(0);
    int propnum = getUnsignedValue(1);
    char result;
    result = (char) getWindow(window).getProperty(propnum);
    storeResult(result);
    nextInstruction();
  }

  private void put_wind_prop() {
    short window = getSignedValue(0);
    char propnum = getUnsignedValue(1);
    short value = getSignedValue(2);
    getWindow(window).putProperty(propnum, value);
    nextInstruction();
  }
  
  private void picture_table() {
    // @picture_table is a no-op, because all pictures are held in memory
    // anyways
    nextInstruction();
  }
  
  private void pop_stack() {
    int numItems = getUnsignedValue(0);
    int stack = 0;
    if (getNumOperands() == 2) {      
      stack = getUnsignedValue(1);
    }
    for (int i = 0; i < numItems; i++) {
      getMachine().popStack(stack);
    }
    nextInstruction();
  }
  
  private void push_stack() {
    char value = getUnsignedValue(0);
    int stack = 0;
    if (getNumOperands() == 2) {      
      stack = getUnsignedValue(1);
    }
    branchOnTest(getMachine().pushStack(stack, value));
  }
  
  private void scroll_window() {
    getWindow(getSignedValue(0)).scroll(getSignedValue(1));
    nextInstruction();
  }
  
  private void read_mouse() {
    int array = getUnsignedValue(0);
    getMachine().getScreen6().readMouse(array);
    nextInstruction();
  }
}
