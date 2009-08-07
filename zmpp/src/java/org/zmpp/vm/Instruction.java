/*
 * Created on 09/24/2005
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
package org.zmpp.vm;

/**
 * This interface defines an instruction's public methods.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
public interface Instruction {

  /** The available operand count types. */
  public enum OperandCount { C0OP, C1OP, C2OP, VAR, EXT }
  
  /** The available instruction forms. */
  public enum InstructionForm { LONG, SHORT, VARIABLE, EXTENDED }

  // Opcode numbers for 0OP
  public static final int C0OP_RTRUE              = 0x00;
  public static final int C0OP_RFALSE             = 0x01;
  public static final int C0OP_PRINT              = 0x02;
  public static final int C0OP_PRINT_RET          = 0x03;
  public static final int C0OP_NOP                = 0x04;
  public static final int C0OP_SAVE               = 0x05;
  public static final int C0OP_RESTORE            = 0x06;
  public static final int C0OP_RESTART            = 0x07;
  public static final int C0OP_RET_POPPED         = 0x08;
  public static final int C0OP_POP                = 0x09; // Versions 1-4
  public static final int C0OP_CATCH              = 0x09; // Versions 5-8
  public static final int C0OP_QUIT               = 0x0a;
  public static final int C0OP_NEW_LINE           = 0x0b;
  public static final int C0OP_SHOW_STATUS        = 0x0c;
  public static final int C0OP_VERIFY             = 0x0d;
  public static final int C0OP_PIRACY             = 0x0f;

  // Opcode numbers for 1OP
  public static final int C1OP_JZ                 = 0x00;
  public static final int C1OP_GET_SIBLING        = 0x01;
  public static final int C1OP_GET_CHILD          = 0x02;
  public static final int C1OP_GET_PARENT         = 0x03;
  public static final int C1OP_GET_PROP_LEN       = 0x04;
  public static final int C1OP_INC                = 0x05;
  public static final int C1OP_DEC                = 0x06;
  public static final int C1OP_PRINT_ADDR         = 0x07;
  public static final int C1OP_CALL_1S            = 0x08;
  public static final int C1OP_REMOVE_OBJ         = 0x09;
  public static final int C1OP_PRINT_OBJ          = 0x0a;
  public static final int C1OP_RET                = 0x0b;
  public static final int C1OP_JUMP               = 0x0c;
  public static final int C1OP_PRINT_PADDR        = 0x0d;
  public static final int C1OP_LOAD               = 0x0e;
  public static final int C1OP_NOT                = 0x0f; // Versions 1-4
  public static final int C1OP_CALL_1N            = 0x0f; // Versions >= 5

  // Opcode numbers for 2OP
  public static final int C2OP_JE                 = 0x01;
  public static final int C2OP_JL                 = 0x02;
  public static final int C2OP_JG                 = 0x03;
  public static final int C2OP_DEC_CHK            = 0x04;
  public static final int C2OP_INC_CHK            = 0x05;
  public static final int C2OP_JIN                = 0x06;
  public static final int C2OP_TEST               = 0x07;
  public static final int C2OP_OR                 = 0x08;
  public static final int C2OP_AND                = 0x09;
  public static final int C2OP_TEST_ATTR          = 0x0a;
  public static final int C2OP_SET_ATTR           = 0x0b;
  public static final int C2OP_CLEAR_ATTR         = 0x0c;
  public static final int C2OP_STORE              = 0x0d;
  public static final int C2OP_INSERT_OBJ         = 0x0e;
  public static final int C2OP_LOADW              = 0x0f;
  public static final int C2OP_LOADB              = 0x10;
  public static final int C2OP_GET_PROP           = 0x11;
  public static final int C2OP_GET_PROP_ADDR      = 0x12;
  public static final int C2OP_GET_NEXT_PROP      = 0x13;
  public static final int C2OP_ADD                = 0x14;
  public static final int C2OP_SUB                = 0x15;
  public static final int C2OP_MUL                = 0x16;
  public static final int C2OP_DIV                = 0x17;
  public static final int C2OP_MOD                = 0x18;
  public static final int C2OP_CALL_2S            = 0x19;
  public static final int C2OP_CALL_2N            = 0x1a;
  public static final int C2OP_SET_COLOUR         = 0x1b;
  public static final int C2OP_THROW              = 0x1c;

  // Opcode numbers for VAR
  public static final int VAR_CALL                = 0x00; // Versions 1-3
  public static final int VAR_CALL_VS             = 0x00; // Versions 4-8
  public static final int VAR_STOREW              = 0x01;
  public static final int VAR_STOREB              = 0x02;
  public static final int VAR_PUT_PROP            = 0x03;
  public static final int VAR_SREAD               = 0x04; // Versions 1-4
  public static final int VAR_AREAD               = 0x04; // Versions >= 5
  public static final int VAR_PRINT_CHAR          = 0x05;
  public static final int VAR_PRINT_NUM           = 0x06;
  public static final int VAR_RANDOM              = 0x07;
  public static final int VAR_PUSH                = 0x08;
  public static final int VAR_PULL                = 0x09;
  public static final int VAR_SPLIT_WINDOW        = 0x0a;
  public static final int VAR_SET_WINDOW          = 0x0b;
  public static final int VAR_CALL_VS2            = 0x0c;
  public static final int VAR_ERASE_WINDOW        = 0x0d;
  public static final int VAR_ERASE_LINE          = 0x0e;
  public static final int VAR_SET_CURSOR          = 0x0f;
  public static final int VAR_GET_CURSOR          = 0x10;
  public static final int VAR_SET_TEXT_STYLE      = 0x11;
  public static final int VAR_BUFFER_MODE         = 0x12;
  public static final int VAR_OUTPUT_STREAM       = 0x13;
  public static final int VAR_INPUT_STREAM        = 0x14;
  public static final int VAR_SOUND_EFFECT        = 0x15;
  public static final int VAR_READ_CHAR           = 0x16;
  public static final int VAR_SCAN_TABLE          = 0x17;
  public static final int VAR_NOT                 = 0x18; // Versions >= 5
  public static final int VAR_CALL_VN             = 0x19; // Versions >= 5
  public static final int VAR_CALL_VN2            = 0x1a; // Versions >= 5
  public static final int VAR_TOKENISE            = 0x1b; // Versions >= 5
  public static final int VAR_ENCODE_TEXT         = 0x1c; // Versions >= 5
  public static final int VAR_COPY_TABLE          = 0x1d;
  public static final int VAR_PRINT_TABLE         = 0x1e;
  public static final int VAR_CHECK_ARG_COUNT     = 0x1f; // Versions >= 5

  // Opcode numbers for EXT
  public static final int EXT_SAVE                = 0x00;
  public static final int EXT_RESTORE             = 0x01;
  public static final int EXT_LOG_SHIFT           = 0x02;
  public static final int EXT_ART_SHIFT           = 0x03;
  public static final int EXT_SET_FONT            = 0x04;
  public static final int EXT_DRAW_PICTURE        = 0x05;
  public static final int EXT_PICTURE_DATA        = 0x06;
  public static final int EXT_ERASE_PICTURE       = 0x07;
  public static final int EXT_SET_MARGINS         = 0x08;  
  public static final int EXT_SAVE_UNDO           = 0x09;
  public static final int EXT_RESTORE_UNDO        = 0x0a;
  public static final int EXT_PRINT_UNICODE       = 0x0b;
  public static final int EXT_CHECK_UNICODE       = 0x0c;
  public static final int EXT_MOVE_WINDOW         = 0x10;
  public static final int EXT_WINDOW_SIZE         = 0x11;
  public static final int EXT_WINDOW_STYLE        = 0x12;
  public static final int EXT_GET_WIND_PROP       = 0x13;
  public static final int EXT_SCROLL_WINDOW       = 0x14;
  public static final int EXT_POP_STACK           = 0x15;
  public static final int EXT_READ_MOUSE          = 0x16;
  public static final int EXT_MOUSE_WINDOW        = 0x17;
  public static final int EXT_PUSH_STACK          = 0x18;
  public static final int EXT_PUT_WIND_PROP       = 0x19;
  public static final int EXT_PRINT_FORM          = 0x1a;
  public static final int EXT_MAKE_MENU           = 0x1b;  
  public static final int EXT_PICTURE_TABLE       = 0x1c;  

   /** The constant for false. */
  public static final char FALSE = 0;
  
  /** The constant for true. */
  public static final char TRUE = 1;

  /** The constant for true from restore. */
  public static final char RESTORE_TRUE = 2;
  
  /**
   * Execute the instruction.
   */
  void execute();
  
  /**
   * Returns true if this instruction prints output.
   * @return true if prints output, false otherwise
   */
  boolean isOutput();
}
