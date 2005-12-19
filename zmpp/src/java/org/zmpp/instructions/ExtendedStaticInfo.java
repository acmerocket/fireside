/*
 * $Id$
 * 
 * Created on 2005/12/19
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
  };  

  private static final ExtendedStaticInfo instance = new ExtendedStaticInfo();
  
  public static ExtendedStaticInfo getInstance() {
    
    return instance;
  }
  
  public int[] getValidVersions(int opcode) {

    return (opcode < VALID_VERSIONS.length) ? VALID_VERSIONS[opcode] :
      new int[0];
  }

  /**
   * {@inheritDoc}
   */
  public boolean storesResult(int opcode, int version) {
    
    switch (opcode) {
    
    case Short1StaticInfo.OP_SAVE:
    case Short1StaticInfo.OP_RESTORE:
    case Short1StaticInfo.OP_LOG_SHIFT:
    case Short1StaticInfo.OP_ART_SHIFT:
    case Short1StaticInfo.OP_SET_FONT:
    case Short1StaticInfo.OP_SAVE_UNDO:
    case Short1StaticInfo.OP_RESTORE_UNDO:
      return true;
    }
    return false;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isBranch(int opcode, int versio) {
    
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public String getOpName(int opcode, int version) {

    switch (opcode) {
    
    case Short1StaticInfo.OP_SAVE:
      return "SAVE";
    case Short1StaticInfo.OP_RESTORE:
      return "RESTORE";
    case Short1StaticInfo.OP_LOG_SHIFT:
      return "LOG_SHIFT";
    case Short1StaticInfo.OP_ART_SHIFT:
      return "ART_SHIFT";
    case Short1StaticInfo.OP_SET_FONT:
      return "SET_FONT";
    case Short1StaticInfo.OP_SAVE_UNDO:
      return "SAVE_UNDO";
    case Short1StaticInfo.OP_RESTORE_UNDO:
      return "RESTORE_UNDO";
    }
    return "unknown";
  }
}
