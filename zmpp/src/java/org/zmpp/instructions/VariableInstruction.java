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

import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.ZObject;
import org.zmpp.vmutil.ZCharEncoder;


/**
 * This class represents instructions of type VARIABLE.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class VariableInstruction extends AbstractInstruction {

  /**
   * The operand count.
   */
  private OperandCount operandCount;
  
  /**
   * Constructor.
   * 
   * @param machineState a reference to a MachineState object
   * @param operandCount the operand count
   * @param opcode the instruction's opcode
   */
  public VariableInstruction(Machine machineState, 
      OperandCount operandCount, int opcode) {
    
    super(machineState, opcode);
    this.operandCount = operandCount;
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
    
    return operandCount;
  }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionStaticInfo getStaticInfo() {

    return VariableStaticInfo.getInstance();
  }
  
  /**
   * {@inheritDoc}
   */
  protected InstructionResult doInstruction() {
    
    switch (getOpcode()) {
      
      case VariableStaticInfo.OP_CALL:
        call();
        break;
      case VariableStaticInfo.OP_CALL_VS2:
        call();
        break;
      case VariableStaticInfo.OP_STOREW:
        storew();
        break;
      case VariableStaticInfo.OP_STOREB:
        storeb();
        break;
      case VariableStaticInfo.OP_PUT_PROP:
        putProp();
        break;
      case VariableStaticInfo.OP_SREAD:
        sread();
        break;
      case VariableStaticInfo.OP_PRINT_CHAR:
        printChar();
        break;
      case VariableStaticInfo.OP_PRINT_NUM:
        printNum();
        break;
      case VariableStaticInfo.OP_RANDOM:
        random();
        break;
      case VariableStaticInfo.OP_PUSH:
        push();
        break;
      case VariableStaticInfo.OP_PULL:
        pull();
        break;
      case VariableStaticInfo.OP_SPLIT_WINDOW:
        splitWindow();
        break;
      case VariableStaticInfo.OP_SET_TEXT_STYLE:
        setTextStyle();
        break;
      case VariableStaticInfo.OP_BUFFER_MODE:
        bufferMode();
        break;
      case VariableStaticInfo.OP_SET_WINDOW:
        setWindow();
        break;
      case VariableStaticInfo.OP_OUTPUTSTREAM:
        outputstream();
        break;
      case VariableStaticInfo.OP_INPUTSTREAM:
        inputstream();
        break;
      case VariableStaticInfo.OP_SOUND_EFFECT:
        soundEffect();
        break;
      case VariableStaticInfo.OP_ERASE_WINDOW:
        eraseWindow();
        break;
      case VariableStaticInfo.OP_ERASE_LINE:
        eraseLine();
        break;
      case VariableStaticInfo.OP_SET_CURSOR:
        setCursor();
        break;
      case VariableStaticInfo.OP_GET_CURSOR:
        getCursor();
        break;
      case VariableStaticInfo.OP_READ_CHAR:
        read_char();
        break;
      case VariableStaticInfo.OP_SCAN_TABLE:
        scan_table();
        break;
      case VariableStaticInfo.OP_NOT:
        not();
        break;
      case VariableStaticInfo.OP_CALL_VN:
      case VariableStaticInfo.OP_CALL_VN2:
        call();
        break;
      case VariableStaticInfo.OP_TOKENISE:
        tokenise();
        break;
      case VariableStaticInfo.OP_ENCODE_TEXT:
        encode_text();
        break;
      case VariableStaticInfo.OP_COPY_TABLE:
        copy_table();
        break;
      case VariableStaticInfo.OP_PRINT_TABLE:
        print_table();
        break;
      case VariableStaticInfo.OP_CHECK_ARG_COUNT:
        check_arg_count();
        break;
      default:
        throwInvalidOpcode();
    }
    // TODO
    return new InstructionResult(TRUE, false);
  }
  
  private void call() {

    call(getNumOperands() - 1);
  }
    
  private void storew() {
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    int array = getUnsignedValue(0);
    int wordIndex = getUnsignedValue(1);
    short value = getValue(2);
    
    memaccess.writeShort(array + wordIndex * 2, value);
    nextInstruction();
  }
  
  private void storeb() {
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    int array = getUnsignedValue(0);
    int byteIndex = getUnsignedValue(1);
    byte value = (byte) getValue(2);
    
    memaccess.writeByte(array + byteIndex, value);
    nextInstruction();
  }
  
  private void putProp() {
    
    int obj = getUnsignedValue(0);
    int property = getUnsignedValue(1);
    short value = getValue(2);
    
    if (obj > 0) {
      
      ZObject object = getMachine().getObjectTree().getObject(obj);
    
      if (object.isPropertyAvailable(property)) {
      
        if (object.getPropertySize(property) == 1) {
        
          object.setPropertyByte(property, 0, (byte) (value & 0xff));
        
        } else {
        
          object.setPropertyByte(property, 0, (byte) ((value >> 8) & 0xff));
          object.setPropertyByte(property, 1, (byte) (value & 0xff));
        }
        nextInstruction();
      
      } else {
      
        getMachine().halt("put_prop: the property [" + property
            + "] of object [" + obj + "] does not exist");
      }
      
    } else {
      
      // Issue warning for non-existent object
      getMachine().warn("@put_prop illegal access to object " + obj);
      nextInstruction();    
    } 
  }
  
  private void printChar() {
    
    short zchar = getValue(0);
    getMachine().printZsciiChar(zchar, false);
    nextInstruction();
  }
  
  private void printNum() {
    
    short number = getValue(0);
    getMachine().printNumber(number);
    nextInstruction();
  }
  
  private void push() {
    
    short value = getValue(0);
    getMachine().setVariable(0, value);
    nextInstruction();
  }
  
  private void pull() {
    
    int varnum = getUnsignedValue(0);
    short value = getMachine().getVariable(0);
    
    // standard 1.1
    if (varnum == 0) {
      
      getMachine().setStackTopElement(value);
      
    } else {
      
      getMachine().setVariable(varnum, value);
    }
    nextInstruction();
  }
  
  private void outputstream() {
    
    // Stream number should be a signed byte
    short streamnumber = getValue(0);
    
    if (streamnumber < 0 && streamnumber >= -3) {
      
      getMachine().selectOutputStream(-streamnumber, false);
    
    } else if (streamnumber > 0 && streamnumber <= 3) {
      
      if (streamnumber == Machine.OUTPUTSTREAM_MEMORY) {
       
        int tableAddress = this.getUnsignedValue(1);
        //System.out.printf("Select stream 3 on table: %x\n", tableAddress);
        getMachine().selectOutputStream3(tableAddress);
        
      } else {
      
        getMachine().selectOutputStream(streamnumber, true);
      }
    }
    nextInstruction();
  }
  
  private void inputstream() {
    
    getMachine().selectInputStream(getUnsignedValue(0));
    nextInstruction();
  }
  
  private void random() {
    
    short range = getValue(0);
    storeResult(getMachine().random(range));
    nextInstruction();
  }
  
  private void sread() {
    
    //System.out.println("sread()");
    int version = getMachine().getStoryFileHeader().getVersion();
    if (version <= 3) {
      
      getMachine().updateStatusLine();
    }
    
    int textbuffer = getUnsignedValue(0);
    int parsebuffer = 0;
    int time = 0;
    short packedAddress = 0;
    
    if (getNumOperands() >= 2) parsebuffer = getUnsignedValue(1);
    if (getNumOperands() >= 3) time = getUnsignedValue(2);
    if (getNumOperands() >= 4) packedAddress = getValue(3);
    
    short terminal = getMachine().getInputFunctions().readLine(
        textbuffer, time, packedAddress);
    
    if (version < 5 || (version >= 5 && parsebuffer > 0)) {
      
      // Do not tokenise if parsebuffer is 0 (See specification of read)
      getMachine().getInputFunctions().tokenize(textbuffer, parsebuffer,
                                                0, false);
    }
    
    if (storesResult()) {

      // The specification suggests that we store the terminating character
      // here, this can be NULL or NEWLINE at the moment
      storeResult(terminal);
    }
    
    nextInstruction();
  }
  
  
  /**
   * Implements the sound_effect instruction.
   */
  private void soundEffect() {
    
    int soundnum = 1;
    int effect = 0;
    int volume = 0;
    int routine = 0;
    
    // Truly variable
    // If no operands are set, this function will still try to send something
    if (getNumOperands() >= 1) {
      
      soundnum = getUnsignedValue(0);
    }
    
    if (getNumOperands() >= 2) {
      
      effect = getUnsignedValue(1);
    }
    
    if (getNumOperands() >= 3) {
      
      volume = getUnsignedValue(2);
    }
    
    if (getNumOperands() == 4) {
      
      routine = getUnsignedValue(3);
    }
    
    getMachine().playSoundEffect(soundnum, effect, volume, routine);
    nextInstruction();
  }
  
  private void splitWindow() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.splitWindow(getUnsignedValue(0));
    }
    nextInstruction();
  }
  
  private void setWindow() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.setWindow(getUnsignedValue(0));
    }
    nextInstruction();
  }
  
  private void setTextStyle() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.setTextStyle(getUnsignedValue(0));
    }
    nextInstruction();
  }
  
  private void bufferMode() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.setBufferMode(getUnsignedValue(0) > 0);
    }
    nextInstruction();
  }
  
  private void eraseWindow() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.eraseWindow(getValue(0));
    }
    nextInstruction();    
  }
  
  private void eraseLine() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.eraseLine(getValue(0));
    }
    nextInstruction();    
  }
  
  private void setCursor() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      screenModel.setTextCursor(getUnsignedValue(0), getUnsignedValue(1));
    }
    nextInstruction();    
  }
  
  private void getCursor() {
    
    ScreenModel screenModel = getMachine().getScreen();
    if (screenModel != null) {
      
      TextCursor cursor = screenModel.getTextCursor();
      MemoryAccess memaccess = getMachine().getMemoryAccess();
      int arrayAddr = getUnsignedValue(0);
      memaccess.writeShort(arrayAddr, (short) cursor.getLine());
      memaccess.writeShort(arrayAddr + 2, (short) cursor.getColumn());
    }
    nextInstruction();
  }
  
  private void scan_table() {
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    short x = getValue(0);
    int table = getUnsignedValue(1);
    int length = getUnsignedValue(2);
    int form  = 0x82; // default value
    if (getNumOperands() == 4) {
      
      form = getUnsignedValue(3);
    }
    int fieldlen = form & 0x7f;
    boolean isWordTable = (form & 0x80) > 0;
    int pointer = table;
    boolean found = false;
    
    for (int i = 0; i < length; i++) {
        
      short current = isWordTable ? memaccess.readShort(pointer) :
                                    memaccess.readByte(pointer);
      if (current == x) {
        
        storeResult((short) pointer);
        found = true;
        break;
      }
      pointer += fieldlen;
    }
    
    // not found
    if (!found) {
      
      storeResult((short) 0);
    }
    branchOnTest(found);
  }

  private void read_char() {
    
    int time = 0;
    int routineAddress = 0;
    if (getNumOperands() >= 2) {
     
      time = getUnsignedValue(1);
    }
    if (getNumOperands() >= 3) {
      
      routineAddress = getValue(2);
    }
    storeResult(getMachine().getInputFunctions().readChar(time,
        routineAddress));
    nextInstruction();
  }
  
  /**
   * not instruction. Actually a copy from Short1Instruction, probably we
   * can remove this duplication.
   */
  private void not()  {
  
    int notvalue = ~getUnsignedValue(0);
    storeResult((short) (notvalue & 0xffff));
    nextInstruction();
  }
  
  private void tokenise() {
    
    //System.out.println("tokenise()");
    int textbuffer = getUnsignedValue(0);
    int parsebuffer = getUnsignedValue(1);
    int dictionary = 0;
    int flag = 0;
    if (getNumOperands() >= 3) dictionary = getUnsignedValue(2);
    if (getNumOperands() >= 4) flag = getUnsignedValue(3);
    if (dictionary != 0) System.out.println("use user dictionary");
    if (flag != 0) System.out.println("tokenise flag is set");
    getMachine().getInputFunctions().tokenize(textbuffer, parsebuffer,
        dictionary, (flag != 0));
    nextInstruction();
  }
  
  private void check_arg_count() {
    
    int argumentNumber = getUnsignedValue(0);
    int currentNumArgs =
      getMachine().getCurrentRoutineContext().getNumArguments();
    branchOnTest(argumentNumber <= currentNumArgs);
  }
  
  private void copy_table() {
    
    int first = getUnsignedValue(0);
    int second = getUnsignedValue(1);
    int size = getValue(2);
    MemoryAccess memaccess = getMachine().getMemoryAccess();

    if (second == 0) {
      
      // Clear size bytes of first
      size = Math.abs(size);
      for (int i = 0; i < size; i++) {
        
        memaccess.writeByte(first + i, (byte) 0);
      }
      
    } else {
      
      if (size < 0 || first > second) {
        
        // copy forward
        size = -size;
        for (int i = 0; i < size; i++) {
          
          memaccess.writeByte(second + i, memaccess.readByte(first + i));
        }
        
      } else {
          
        // backwards
        size = Math.abs(size);
        for (int i = size - 1; i >= 0; i--) {
          
          memaccess.writeByte(second + i, memaccess.readByte(first + i));
        }
      }
    }
    nextInstruction();
  }
  
  /**
   * Do the print_table instruction. This method takes a text and formats
   * it in a specified format. It requires access to the cursor position
   * in order to be implemented correctly, otherwise horizontal home
   * position would always be set to the left position of the window.
   * Interestingly, the text is not encoded, so the characters should be
   * accessed one by one in ZSCII format.
   */
  private void print_table() {
    
    int zsciiText = getUnsignedValue(0);
    int width = getUnsignedValue(1);
    int height = 1;
    int skip = 0;
    if (getNumOperands() >= 3) height = getUnsignedValue(2);
    if (getNumOperands() == 4) skip = getUnsignedValue(3);
    
    System.out.printf("@print_table, zscii-text = %d, width = %d," +
        " height = %d, skip = %d\n", zsciiText, width, height, skip);
    short zchar = 0;
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    TextCursor cursor = getMachine().getScreen().getTextCursor();
    int column = cursor.getColumn();
    int row = cursor.getLine();
    
    for (int i = 0; i < height; i++) {
      
      for (int j = 0; j < width; j++) {
        
        int offset = (width * i) + j;
        zchar = memaccess.readUnsignedByte(zsciiText + offset);
        getMachine().printZsciiChar(zchar, false);
      }
      row += skip + 1;
      getMachine().getScreen().setTextCursor(row, column);
    }
    nextInstruction();
  }
  
  private void encode_text() {
    
    int zsciiText = getUnsignedValue(0);
    int length = getUnsignedValue(1);
    int from = getUnsignedValue(2);
    int codedText = getUnsignedValue(3);
    System.out.printf("@encode_text, zscii-text = %d, length = %d," +
        " from = %d, coded-text = %d\n", zsciiText, length, from, codedText);
    
    ZCharEncoder encoder = new ZCharEncoder();
    encoder.encode(getMachine().getMemoryAccess(), zsciiText + from,
        length, codedText);
    nextInstruction();
  }
}