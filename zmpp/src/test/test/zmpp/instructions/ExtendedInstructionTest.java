/*
 * $Id$
 * 
 * Created on 12/08/2005
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
package test.zmpp.instructions;

import org.zmpp.instructions.ExtendedInstruction;
import org.zmpp.instructions.ExtendedStaticInfo;
import org.zmpp.instructions.Operand;

/**
 * This class tests the dynamic and static aspects of the extended
 * instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class ExtendedInstructionTest extends InstructionTestBase {

  protected void setUp() throws Exception {
  
    super.setUp();
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
  }
  
  public void testStoresResult() {
    
    ExtendedInstruction instr = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    assertTrue(instr.storesResult());
    instr = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_RESTORE_UNDO);
    assertTrue(instr.storesResult());
    instr = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_LOG_SHIFT);
    assertTrue(instr.storesResult());
    instr = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    assertTrue(instr.storesResult());
    instr = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SET_FONT);
    assertTrue(instr.storesResult());
  }
  
  public void testSaveUndoSuccess() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(3));
    mockMachine.expects(atLeastOnce()).method("save_undo").will(returnValue(true));
    mockCpu.expects(atLeastOnce()).method("setVariable").with(eq(0), eq((short) 1));
    
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }

  public void testSaveUndoFail() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(3));
    mockMachine.expects(atLeastOnce()).method("save_undo").will(returnValue(false));
    mockCpu.expects(atLeastOnce()).method("setVariable").with(eq(0), eq((short) 0));
    
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }
  
  // **************************************************************************
  // ******** ART_SHIFT
  // **********************************
  
  public void testArtShift0() {
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 12));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();
  }
    
  public void testArtShiftPositivePositiveShift() {

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 24));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();    
    
  }    

  public void testArtShiftNegativePositiveShift() {

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) -24));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  public void testArtShiftPositiveNegativeShift() {

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) 6));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  public void testArtShiftNegativeNegativeShift() {

    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(atLeastOnce()).method("incrementProgramCounter").with(eq(3));
    mockCpu.expects(once()).method("setVariable").with(eq(1), eq((short) -6));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }
  
  // **************************************************************************
  // ******** LOG_SHIFT
  // **********************************
}
