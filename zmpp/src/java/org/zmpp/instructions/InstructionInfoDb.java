/*
 * $Id$
 * Created on 2008/07/23
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

import java.util.HashMap;
import java.util.Map;
import static org.zmpp.vm.Instruction.*;
import static org.zmpp.vm.Instruction.OperandCount.*;

/**
 * This is the new representation for the information about instructions in
 * the Z-machine. As opposed to the old xStaticInfo classes, this is a database
 * containing all the information. It can be regarded as static configuration
 * which is compiled into the application.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class InstructionInfoDb {
  

  // Commonly used version ranges
  private static final int[] ALL_VERSIONS = { 1, 2, 3, 4, 5, 6, 7, 8 };
  private static final int[] EXCEPT_V6    = { 1, 2, 3, 4, 5, 7, 8 };
  private static final int[] V1_TO_V3     = { 1, 2, 3 };
  private static final int[] V1_TO_V4     = { 1, 2, 3, 4 };
  private static final int[] V5_TO_V8     = { 5, 6, 7, 8 };
  private static final int[] V3_TO_V8     = { 3, 4, 5, 6, 7, 8 };
  private static final int[] V4_TO_V8     = { 4, 5, 6, 7, 8 };
  private static final int[] V4           = { 4 };
  private static final int[] V6           = { 6 };
  
  /**
   * Information structure about the instruction.
   */
  public class InstructionInfo {
    private String name;
    private boolean isStore, isBranch, isPrint, isOutput;
    public InstructionInfo(String name, boolean isBranch, boolean isStore,
                           boolean isPrint, boolean isOutput) {
      this.name = name;
      this.isBranch = isBranch;
      this.isStore = isStore;
      this.isPrint = isPrint;
      this.isOutput = isOutput;
    }
    public boolean isStore() { return isStore; }
    public boolean isBranch() { return isBranch; }
    public boolean isPrint() { return isPrint; }
    public boolean isOutput() { return isOutput; }
    public String getName() { return name; }
  }
  
  // Factory methods to create the common InstructionInfo types
  
  private InstructionInfo createInfo(String name) {
    return new InstructionInfo(name, false, false, false, false);
  }
  private InstructionInfo createBranchAndStore(String name) {
    return new InstructionInfo(name, true, true, false, false);
  }
  private InstructionInfo createStore(String name) {
    return new InstructionInfo(name, false, true, false, false);
  }
  private InstructionInfo createBranch(String name) {
    return new InstructionInfo(name, true, false, false, false);
  }
  private InstructionInfo createPrint(String name) {
    return new InstructionInfo(name, false, false, true, true);
  }
  private InstructionInfo createOutput(String name) {
    return new InstructionInfo(name, false, false, false, true);
  }
  
  /** The hashmap to represent the database */
  private Map<String, InstructionInfo> infoMap =
          new HashMap<String, InstructionInfo>();
  
  /**
   * Private constructor.
   */
  private InstructionInfoDb() {
    // 0OP
    addInfoForAll(createInfo("RTRUE"), C0OP, C0OP_RTRUE);
    addInfoForAll(createInfo("RFALSE"), C0OP, C0OP_RFALSE);
    addInfoForAll(createPrint("PRINT"), C0OP, C0OP_PRINT);
    addInfoForAll(createPrint("PRINT_RET"), C0OP, C0OP_PRINT_RET);
    addInfoForAll(createInfo("NOP"), C0OP, C0OP_NOP);
    addInfoFor(createBranch("SAVE"), C0OP, C0OP_SAVE, V1_TO_V3);
    addInfoFor(createBranch("RESTORE"), C0OP, C0OP_RESTORE, V1_TO_V3);
    addInfoFor(createStore("SAVE"), C0OP, C0OP_SAVE, V4);
    addInfoFor(createStore("RESTORE"), C0OP, C0OP_RESTORE, V4);
    addInfoForAll(createInfo("RESTART"), C0OP, C0OP_RESTART);
    addInfoForAll(createInfo("RET_POPPED"), C0OP, C0OP_RET_POPPED);
    addInfoFor(createInfo("POP"), C0OP, C0OP_POP, V1_TO_V4);
    addInfoFor(createStore("CATCH"), C0OP, C0OP_CATCH, V5_TO_V8);
    addInfoForAll(createInfo("QUIT"), C0OP, C0OP_QUIT);
    addInfoForAll(createOutput("NEW_LINE"), C0OP, C0OP_NEW_LINE);
    addInfoFor(createInfo("SHOW_STATUS"), C0OP, C0OP_SHOW_STATUS,
               new int[] {3});
    addInfoFor(createBranch("VERIFY"), C0OP, C0OP_VERIFY,
               new int[] {3, 4, 5, 6, 7, 8});
    addInfoFor(createInfo("PIRACY"), C0OP, C0OP_PIRACY, V5_TO_V8);
    
    // 1OP
    addInfoForAll(createBranch("JZ"), C1OP, C1OP_JZ);
    addInfoForAll(createBranchAndStore("GET_SIBLING"), C1OP, C1OP_GET_SIBLING);
    addInfoForAll(createBranchAndStore("GET_CHILD"), C1OP, C1OP_GET_CHILD);
    addInfoForAll(createStore("GET_PARENT"), C1OP, C1OP_GET_PARENT);
    addInfoForAll(createStore("GET_PROP_LEN"), C1OP, C1OP_GET_PROP_LEN);
    addInfoForAll(createInfo("INC"), C1OP, C1OP_INC);
    addInfoForAll(createInfo("DEC"), C1OP, C1OP_DEC);
    addInfoForAll(createOutput("PRINT_ADDR"), C1OP, C1OP_PRINT_ADDR);
    addInfoFor(createStore("CALL_1S"), C1OP, C1OP_CALL_1S, V4_TO_V8);
    addInfoForAll(createInfo("REMOVE_OBJ"), C1OP, C1OP_REMOVE_OBJ);
    addInfoForAll(createOutput("PRINT_OBJ"), C1OP, C1OP_PRINT_OBJ);
    addInfoForAll(createInfo("RET"), C1OP, C1OP_RET);
    addInfoForAll(createInfo("JUMP"), C1OP, C1OP_JUMP);
    addInfoForAll(createOutput("PRINT_PADDR"), C1OP, C1OP_PRINT_PADDR);
    addInfoForAll(createStore("LOAD"), C1OP, C1OP_LOAD);
    addInfoFor(createStore("NOT"), C1OP, C1OP_NOT, V1_TO_V4);
    addInfoFor(createInfo("CALL_1N"), C1OP, C1OP_CALL_1N, V5_TO_V8);

    // 2OP
    addInfoForAll(createBranch("JE"), C2OP, C2OP_JE);
    addInfoForAll(createBranch("JL"), C2OP, C2OP_JL);
    addInfoForAll(createBranch("JG"), C2OP, C2OP_JG);
    addInfoForAll(createBranch("DEC_CHK"), C2OP, C2OP_DEC_CHK);
    addInfoForAll(createBranch("INC_CHK"), C2OP, C2OP_INC_CHK);
    addInfoForAll(createBranch("JIN"), C2OP, C2OP_JIN);
    addInfoForAll(createBranch("TEST"), C2OP, C2OP_TEST);
    addInfoForAll(createStore("OR"), C2OP, C2OP_OR);
    addInfoForAll(createStore("AND"), C2OP, C2OP_AND);
    addInfoForAll(createBranch("TEST_ATTR"), C2OP, C2OP_TEST_ATTR);
    addInfoForAll(createInfo("SET_ATTR"), C2OP, C2OP_SET_ATTR);
    addInfoForAll(createInfo("CLEAR_ATTR"), C2OP, C2OP_CLEAR_ATTR);
    addInfoForAll(createInfo("STORE"), C2OP, C2OP_STORE);
    addInfoForAll(createInfo("INSERT_OBJ"), C2OP, C2OP_INSERT_OBJ);
    addInfoForAll(createStore("LOADW"), C2OP, C2OP_LOADW);
    addInfoForAll(createStore("LOADB"), C2OP, C2OP_LOADB);
    addInfoForAll(createStore("GET_PROP"), C2OP, C2OP_GET_PROP);
    addInfoForAll(createStore("GET_PROP_ADDR"), C2OP, C2OP_GET_PROP_ADDR);
    addInfoForAll(createStore("GET_NEXT_PROP"), C2OP, C2OP_GET_NEXT_PROP);
    addInfoForAll(createStore("ADD"), C2OP, C2OP_ADD);
    addInfoForAll(createStore("SUB"), C2OP, C2OP_SUB);
    addInfoForAll(createStore("MUL"), C2OP, C2OP_MUL);
    addInfoForAll(createStore("DIV"), C2OP, C2OP_DIV);
    addInfoForAll(createStore("MOD"), C2OP, C2OP_MOD);
    addInfoFor(createStore("CALL_2S"), C2OP, C2OP_CALL_2S, V4_TO_V8);
    addInfoFor(createInfo("CALL_2N"), C2OP, C2OP_CALL_2N, V5_TO_V8);
    addInfoFor(createInfo("SET_COLOUR"), C2OP, C2OP_SET_COLOUR, V5_TO_V8);
    addInfoFor(createInfo("THROW"), C2OP, C2OP_THROW, V5_TO_V8);

    // VAR
    addInfoFor(createStore("CALL"), VAR, VAR_CALL, V1_TO_V3);
    addInfoFor(createStore("CALL_VS"), VAR, VAR_CALL_VS, V4_TO_V8);
    addInfoForAll(createInfo("STOREW"), VAR, VAR_STOREW);
    addInfoForAll(createInfo("STOREB"), VAR, VAR_STOREB);
    addInfoForAll(createInfo("PUT_PROP"), VAR, VAR_PUT_PROP);
    addInfoFor(createInfo("SREAD"), VAR, VAR_SREAD, V1_TO_V4);
    addInfoFor(createStore("AREAD"), VAR, VAR_AREAD, V5_TO_V8);
    addInfoForAll(createOutput("PRINT_CHAR"), VAR, VAR_PRINT_CHAR);
    addInfoForAll(createOutput("PRINT_NUM"), VAR, VAR_PRINT_NUM);
    addInfoForAll(createStore("RANDOM"), VAR, VAR_RANDOM);
    addInfoForAll(createInfo("PUSH"), VAR, VAR_PUSH);
    addInfoFor(createInfo("PULL"), VAR, VAR_PULL, EXCEPT_V6);
    addInfoFor(createStore("PULL"), VAR, VAR_PULL, V6);
    addInfoFor(createOutput("SPLIT_WINDOW"), VAR, VAR_SPLIT_WINDOW,
               V3_TO_V8);
    addInfoFor(createInfo("SET_WINDOW"), VAR, VAR_SET_WINDOW, V3_TO_V8);
    addInfoFor(createStore("CALL_VS2"), VAR, VAR_CALL_VS2, V4_TO_V8);
    addInfoFor(createOutput("ERASE_WINDOW"), VAR, VAR_ERASE_WINDOW,
               V4_TO_V8);
    addInfoFor(createOutput("ERASE_LINE"), VAR, VAR_ERASE_LINE, V4_TO_V8);
    addInfoFor(createInfo("SET_CURSOR"), VAR, VAR_SET_CURSOR, V4_TO_V8);
    addInfoFor(createInfo("GET_CURSOR"), VAR, VAR_GET_CURSOR, V4_TO_V8);
    addInfoFor(createInfo("SET_TEXT_STYLE"), VAR, VAR_SET_TEXT_STYLE,
               V4_TO_V8);
    addInfoFor(createInfo("BUFFER_MODE"), VAR, VAR_BUFFER_MODE,
               V4_TO_V8);
    addInfoFor(createInfo("OUTPUT_STREAM"), VAR, VAR_OUTPUT_STREAM,
               V3_TO_V8);
    addInfoFor(createInfo("INPUT_STREAM"), VAR, VAR_INPUT_STREAM,
               V3_TO_V8);
    addInfoFor(createInfo("SOUND_EFFECT"), VAR, VAR_SOUND_EFFECT,
               V3_TO_V8);
    addInfoFor(createStore("READ_CHAR"), VAR, VAR_READ_CHAR, V4_TO_V8);
    addInfoFor(createBranchAndStore("SCAN_TABLE"), VAR, VAR_SCAN_TABLE,
               V4_TO_V8);
    addInfoFor(createStore("NOT"), VAR, VAR_NOT, V5_TO_V8);
    addInfoFor(createInfo("CALL_VN"), VAR, VAR_CALL_VN, V5_TO_V8);
    addInfoFor(createInfo("CALL_VN2"), VAR, VAR_CALL_VN2, V5_TO_V8);
    addInfoFor(createInfo("TOKENISE"), VAR, VAR_TOKENISE, V5_TO_V8);
    addInfoFor(createInfo("ENCODE_TEXT"), VAR, VAR_ENCODE_TEXT, V5_TO_V8);
    addInfoFor(createInfo("COPY_TABLE"), VAR, VAR_COPY_TABLE, V5_TO_V8);
    addInfoFor(createOutput("PRINT_TABLE"), VAR, VAR_PRINT_TABLE, V5_TO_V8);
    addInfoFor(createBranch("CHECK_ARG_COUNT"), VAR, VAR_CHECK_ARG_COUNT,
               V5_TO_V8);
    
    // EXT
    addInfoFor(createStore("SAVE"), EXT, EXT_SAVE, V5_TO_V8);
    addInfoFor(createStore("RESTORE"), EXT, EXT_RESTORE, V5_TO_V8);
    addInfoFor(createStore("LOG_SHIFT"), EXT, EXT_LOG_SHIFT, V5_TO_V8);
    addInfoFor(createStore("ART_SHIFT"), EXT, EXT_ART_SHIFT, V5_TO_V8);
    addInfoFor(createStore("SET_FONT"), EXT, EXT_SET_FONT, V5_TO_V8);
    addInfoFor(createOutput("DRAW_PICTURE"), EXT, EXT_DRAW_PICTURE, V6);
    addInfoFor(createBranch("PICTURE_DATA"), EXT, EXT_PICTURE_DATA, V6);
    addInfoFor(createOutput("ERASE_PICTURE"), EXT, EXT_ERASE_PICTURE, V6);
    addInfoFor(createInfo("SET_MARGINS"), EXT, EXT_SET_MARGINS, V6);
    addInfoFor(createStore("SAVE_UNDO"), EXT, EXT_SAVE_UNDO, V5_TO_V8);
    addInfoFor(createStore("RESTORE_UNDO"), EXT, EXT_RESTORE_UNDO,
               V5_TO_V8);
    addInfoFor(createOutput("PRINT_UNICODE"), EXT, EXT_PRINT_UNICODE,
               V5_TO_V8);
    addInfoFor(createInfo("CHECK_UNICODE"), EXT, EXT_CHECK_UNICODE,
               V5_TO_V8);
    addInfoFor(createOutput("MOVE_WINDOW"), EXT, EXT_MOVE_WINDOW, V6);
    addInfoFor(createInfo("WINDOW_SIZE"), EXT, EXT_WINDOW_SIZE, V6);
    addInfoFor(createInfo("WINDOW_STYLE"), EXT, EXT_WINDOW_STYLE, V6);
    addInfoFor(createStore("GET_WIND_PROP"), EXT, EXT_GET_WIND_PROP, V6);
    addInfoFor(createOutput("SCROLL_WINDOW"), EXT, EXT_SCROLL_WINDOW, V6);
    addInfoFor(createInfo("POP_STACK"), EXT, EXT_POP_STACK, V6);
    addInfoFor(createInfo("READ_MOUSE"), EXT, EXT_READ_MOUSE, V6);
    addInfoFor(createInfo("MOUSE_WINDOW"), EXT, EXT_MOUSE_WINDOW, V6);
    addInfoFor(createBranch("PUSH_STACK"), EXT, EXT_PUSH_STACK, V6);
    addInfoFor(createInfo("PUT_WIND_PROP"), EXT, EXT_PUT_WIND_PROP, V6);
    addInfoFor(createOutput("PRINT_FORM"), EXT, EXT_PRINT_FORM, V6);
    addInfoFor(createBranch("MAKE_MENU"), EXT, EXT_MAKE_MENU, V6);
    addInfoFor(createInfo("PICTURE_TABLE"), EXT, EXT_PICTURE_TABLE, V6);
  }
  
  /**
   * Adds the specified info struct for all Z-machine versions.
   * @param info the InstructionInfo
   * @param opCount the OperandCount
   * @param opcodeNum the opcode number
   */
  private void addInfoForAll(InstructionInfo info, OperandCount opCount,
                             int opcodeNum) {
    addInfoFor(info, opCount, opcodeNum, ALL_VERSIONS);
  }

  /**
   * Adds the specified InstructionInfo for the specified Z-machine versions.
   * @param info the InstructionInfo
   * @param opCount the OperandCount
   * @param opcodeNum the opcode number
   * @param versions the valid versions
   */
  private void addInfoFor(InstructionInfo info, OperandCount opCount,
                          int opcodeNum, int[] versions) {
    for (int version : versions) {
      infoMap.put(createKey(opCount, opcodeNum, version), info);
    }
  }
  
  private static InstructionInfoDb instance = new InstructionInfoDb();
  
  /**
   * Returns the Singleton instance of the database.
   * @return the database instance
   */
  public static InstructionInfoDb getInstance() { return instance; }

  /**
   * Creates the hash key for the specified instruction information.
   * @param form the form
   * @param opcodeNum the opcode number
   * @param version the story version
   * @return the key
   */
  private String createKey(OperandCount opCount, int opcodeNum, int version) {
    return opCount.toString() + ":" + opcodeNum + ":" + version;
  }

  /**
   * Returns the information struct for the specified instruction.
   * @param form the instruction form
   * @param opcodeNum the opcode number
   * @param version the story version
   * @return the instruction info struct
   */
  public InstructionInfo getInfo(OperandCount opCount, int opcodeNum,
                                       int version) {
    //System.out.println("GENERATING KEY: " + createKey(opCount, opcodeNum, version));
    return infoMap.get(createKey(opCount, opcodeNum, version));
  }

  /**
   * Determines if the specified operation is valid.
   * @param form the instruction form
   * @param opcodeNum the opcode number
   * @param version the story version
   * @return true if valid, false otherwise
   */
  public boolean isValid(OperandCount opCount, int opcodeNum,
                         int version) {
    return infoMap.containsKey(createKey(opCount, opcodeNum, version));
  }
  
  public void printKeys() {
    System.out.println("INFO MAP KEYS: ");
    for (String key : infoMap.keySet()) {
      if (key.startsWith("C1OP:0")) System.out.println(key);
    }
  }
}
