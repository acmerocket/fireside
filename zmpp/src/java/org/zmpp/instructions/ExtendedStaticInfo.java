/*
 * $Id$
 * 
 * Created on 2005/12/19
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


public class ExtendedStaticInfo implements InstructionStaticInfo {

  private static final int[][] VALID_VERSIONS = {
    
    { 5, 6, 7, 8},  // SAVE
    { 5, 6, 7, 8},  // RESTORE
    { 5, 6, 7, 8 }, // LOG_SHIFT
    { 5, 6, 7, 8 }, // ART_SHIFT
    { 5, 6, 7, 8 }, // SET_FONT
    { },            // 0x05
    { },            // 0x06
    { },            // 0x07
    { },            // 0x08
    { 5, 6, 7, 8 }, // SAVE_UNDO    
    { 5, 6, 7, 8 }, // RESTORE_UNDO
    { 5, 6, 7, 8 }, // PRINT_UNICODE
    { 5, 6, 7, 8 }, // CHECK_UNICODE
  };  

  private static final ExtendedStaticInfo instance = new ExtendedStaticInfo();
  
  public static ExtendedStaticInfo getInstance() {
    
    return instance;
  }

  /**
   * List of opcodes. See Z-Machine Standards document 1.0 for
   * explanations.
   */
  public static final int OP_SAVE                 = 0x00;
  public static final int OP_RESTORE              = 0x01;
  public static final int OP_LOG_SHIFT            = 0x02;
  public static final int OP_ART_SHIFT            = 0x03;
  public static final int OP_SET_FONT             = 0x04;
  public static final int OP_SAVE_UNDO            = 0x09;
  public static final int OP_RESTORE_UNDO         = 0x0a;
  public static final int OP_PRINT_UNICODE        = 0x0b;
  public static final int OP_CHECK_UNICODE        = 0x0c;
  
  public int[] getValidVersions(int opcode) {

    return (opcode < VALID_VERSIONS.length) ? VALID_VERSIONS[opcode] :
      new int[0];
  }

  /**
   * {@inheritDoc}
   */
  public boolean storesResult(int opcode, int version) {
    
    switch (opcode) {
    
    case ExtendedStaticInfo.OP_SAVE:
    case ExtendedStaticInfo.OP_RESTORE:
    case ExtendedStaticInfo.OP_LOG_SHIFT:
    case ExtendedStaticInfo.OP_ART_SHIFT:
    case ExtendedStaticInfo.OP_SET_FONT:
    case ExtendedStaticInfo.OP_SAVE_UNDO:
    case ExtendedStaticInfo.OP_RESTORE_UNDO:
    case ExtendedStaticInfo.OP_CHECK_UNICODE:
      return true;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isBranch(int opcode, int version) {
    
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isOutput(int opcode, int version) {
    
    return opcode == OP_PRINT_UNICODE;
  }

  /**
   * {@inheritDoc}
   */
  public String getOpName(int opcode, int version) {

    switch (opcode) {
    
    case ExtendedStaticInfo.OP_SAVE:
      return "SAVE";
    case ExtendedStaticInfo.OP_RESTORE:
      return "RESTORE";
    case ExtendedStaticInfo.OP_LOG_SHIFT:
      return "LOG_SHIFT";
    case ExtendedStaticInfo.OP_ART_SHIFT:
      return "ART_SHIFT";
    case ExtendedStaticInfo.OP_SET_FONT:
      return "SET_FONT";
    case ExtendedStaticInfo.OP_SAVE_UNDO:
      return "SAVE_UNDO";
    case ExtendedStaticInfo.OP_RESTORE_UNDO:
      return "RESTORE_UNDO";
    case ExtendedStaticInfo.OP_PRINT_UNICODE:
      return "PRINT_UNICODE";
    case ExtendedStaticInfo.OP_CHECK_UNICODE:
      return "CHECK_UNICODE";
    }
    return "unknown";
  }
}