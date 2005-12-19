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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.zmpp.base.MemoryAccess;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.Machine;
import org.zmpp.vm.ScreenModel;
import org.zmpp.vm.TextCursor;
import org.zmpp.vm.ZObject;
import org.zmpp.vmutil.ZCharConverter;
import org.zmpp.vmutil.ZsciiEncoding;
import org.zmpp.vmutil.ZCharConverter.Alphabet;


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
        readChar();
        break;
      case VariableStaticInfo.OP_SCAN_TABLE:
        scanTable();
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
      case VariableStaticInfo.OP_COPY_TABLE:
        copyTable();
        break;
      case VariableStaticInfo.OP_PRINT_TABLE:
        printTable();
        break;
      case VariableStaticInfo.OP_CHECK_ARG_COUNT:
        checkArgCount();
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
    getMachine().printZsciiChar(zchar);
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
    
    int version = getMachine().getStoryFileHeader().getVersion();
    if (version <= 3) {
      
      getMachine().updateStatusLine();
    }
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    int textbuffer = getUnsignedValue(0);
    int parsebuffer = getUnsignedValue(1);
    int bufferlen = memaccess.readUnsignedByte(textbuffer);
    getMachine().readLine(textbuffer + 1, bufferlen);
    //System.out.printf("sread(), parsebuffer = %x\n", parsebuffer);
    
    if (version < 5 || (version >= 5 && parsebuffer != 0)) {
      
      // Do not tokenise if parsebuffer is 0 (See specification of read)
      tokeniseAfterRead(textbuffer, parsebuffer);
    }
    
    if (storesResult()) {

      // The specification suggests that we store the terminating character
      // here, we will assume it as being NEWLINE (Character 13) always
      storeResult(ZsciiEncoding.NEWLINE);
    }
    
    nextInstruction();
  }
  
  private void tokeniseAfterRead(int textbuffer, int parsebuffer) {
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    Dictionary dictionary = getMachine().getDictionary();
    int version = getMachine().getStoryFileHeader().getVersion();
    int bufferlen = memaccess.readUnsignedByte(textbuffer);
    int charsTyped = (version >= 5) ?
                      memaccess.readUnsignedByte(textbuffer + 1) :
                      0;
    
    // from version 5, text starts at position 2
    int textoffset = (version < 5) ? 1 : 2; 
    String input = bufferToString(textbuffer + textoffset, bufferlen,
                                  charsTyped);
    List<String> tokens = tokenize(input);
    
    Map<String, Integer> parsedTokens = new HashMap<String, Integer>();
    
    // Write the number of tokens in byte 1 of the parse buffer
    int maxwords = memaccess.readUnsignedByte(parsebuffer);
    
    // Do not go beyond the limit of maxwords
    int numParsedTokens = Math.min(maxwords, tokens.size());
    
    // Write the number of parsed tokens into byte 1 of the parse buffer
    memaccess.writeUnsignedByte(parsebuffer + 1, (short) numParsedTokens);
    
    int parseaddr = parsebuffer + 2;
    
    for (int i = 0; i < numParsedTokens; i++) {
      
      String token = tokens.get(i);      
      int entryAddress = dictionary.lookup(token);
      
      int startIndex = 0;
      if (parsedTokens.containsKey(token)) {
          
        int timesContained = parsedTokens.get(token);
        parsedTokens.put(token, timesContained + 1);
          
        for (int j = 0; j < timesContained; j++) {
          
          int found = input.indexOf(token, startIndex);
          startIndex = found + token.length();
        }
          
      } else {
          
        parsedTokens.put(token, 1);          
      }
      int tokenIndex = input.indexOf(token, startIndex);    
      tokenIndex = tokenIndex + 1; // because of the length byte
      
      // write out the entry to the parse buffer
      memaccess.writeUnsignedShort(parseaddr, entryAddress);     
      memaccess.writeUnsignedByte(parseaddr + 2, (short) token.length());
      memaccess.writeUnsignedByte(parseaddr + 3, (short) tokenIndex);
      parseaddr += 4;
    }
  }
  
  /**
   * Turns the buffer into a Java string. This function reads at most
   * |bufferlen| bytes and treats each byte as an ASCII character.
   * The characters will be concatenated to the result string.
   * 
   * @param address the buffer address
   * @param bufferlen the buffer length
   * @param charsTyped from version 5, this is the number of characters
   * to include in the input
   * @return the string contained in the buffer
   */
  private String bufferToString(int address, int bufferlen, int charsTyped) {
    
    MemoryAccess memaccess = getMachine().getMemoryAccess();
    
    // If charsTyped is set, use that value as the limit
    int numChars = (charsTyped > 0) ? charsTyped : bufferlen;
    
    // read input from text buffer
    StringBuilder buffer = new StringBuilder();
    for (int i = 0; i < numChars; i++) {
      
      short charByte = memaccess.readUnsignedByte(address + i);
      if (charByte == 0) break;
      buffer.append((char) charByte);
    }
    
    return buffer.toString().toLowerCase();
  }
  
  /**
   * Turns the specified input string into tokens. It will take whitespace
   * implicitly and dictionary separators explicitly to tokenize the
   * stream, dictionary specified separators are included in the result list.
   * 
   * @param input the input string
   * @return the tokens
   */
  private List<String> tokenize(String input) {
    
    List<String> result = new ArrayList<String>();
    String whitespace = " \n\t\r\f";
    
    // Retrieve the defined separators
    StringBuilder separators = new StringBuilder();
    Dictionary dictionary = getMachine().getDictionary();
    for (int i = 0, n = dictionary.getNumberOfSeparators(); i < n; i++) {
      
      byte delim = dictionary.getSeparator(i);
      separators.append(ZCharConverter.decode(Alphabet.A0, delim));
    }
    
    // The tokenizer will also return the delimiters
    String delim = whitespace + separators.toString();
    StringTokenizer tok = new StringTokenizer(input, delim, true);
    
    while (tok.hasMoreTokens()) {
      
      String token = tok.nextToken();
      if (!Character.isWhitespace(token.charAt(0))) {
        
        result.add(token);
      }
    }
    //System.out.println("tokens: " + result);
    return result;
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
  
  private void scanTable() {
    
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

  private void readChar() {
    
    storeResult(getMachine().readChar());
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
    tokeniseAfterRead(textbuffer, parsebuffer);
    nextInstruction();
  }
  
  private void checkArgCount() {
    
    int argumentNumber = getUnsignedValue(0);
    int currentNumArgs =
      getMachine().getCurrentRoutineContext().getNumArguments();
    branchOnTest(argumentNumber <= currentNumArgs);
  }
  
  private void copyTable() {
    
    // TODO
  }
  
  private void printTable() {
    
    // TODO
  }

}
