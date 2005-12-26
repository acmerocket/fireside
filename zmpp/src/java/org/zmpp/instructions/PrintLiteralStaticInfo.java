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

/**
 * The static aspects of a PrintLiteralInstruction are stored here.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PrintLiteralStaticInfo implements InstructionStaticInfo {

  /**
   * The valid versions.
   */
  private static final int[][] VALID_VERSIONS = {
    
    {}, // 0x00
    {}, // 0x01
    { 1, 2, 3, 4, 5, 6, 7, 8 }, // PRINT
    { 1, 2, 3, 4, 5, 6, 7, 8 }  // PRINT_RET
  };  

  /**
   * Opcodes.
   */
  public static final int OP_PRINT              = 0x02;
  public static final int OP_PRINT_RET          = 0x03;

  
  /**
   * Singleton instance.
   */
  private static final PrintLiteralStaticInfo instance =
    new PrintLiteralStaticInfo();
  
  /**
   * Returns an instance of PrintLiteralStaticInfo.
   * 
   * @return a PrintLiteralStaticInfo object
   */
  public static PrintLiteralStaticInfo getInstance() {
  
    return instance;
  }
  
  /**
   * {@inheritDoc}
   */
  public int[] getValidVersions(int opcode) {

    return (opcode < VALID_VERSIONS.length) ? VALID_VERSIONS[opcode] :
      new int[0];
  }


  /**
   * {@inheritDoc}
   */
  public boolean storesResult(int opcode, int version) {

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
    
    return true;
  }
  
  /**
   * {@inheritDoc}
   */
  public String getOpName(int opcode, int version) {
    
    switch (opcode) {
    
    case OP_PRINT_RET: return "PRINT_RET";
    case OP_PRINT: return "PRINT";
    }
    return "unknown";
  }  
}
