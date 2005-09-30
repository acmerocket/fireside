/*
 * $Id$
 * 
 * Created on 24.09.2005
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
package org.zmpp.vm;

/**
 * This class represents an instruction's operational portion.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Instruction {

  /**
   * The instruction information.
   */
  private InstructionInfo info;
  
  /**
   * Constructor.
   * 
   * @param info the instruction information
   */
  public Instruction(InstructionInfo info) {
    
    this.info = info;
  }
  
  /**
   * Retrieves the instruction's information.
   * 
   * @return the instruction information
   */
  public InstructionInfo getInfo() { return info; }
  
  /**
   * Execute the instruction.
   */
  public void execute() {
    
  }
  
  /**
   * This translates a packed address into a real address.
   * 
   * @param packedAddress the packed address
   * @return the real address
   */
  private int translatePackedAddress(int packedAddress) {
    
    return packedAddress * 2;
  }  
}
