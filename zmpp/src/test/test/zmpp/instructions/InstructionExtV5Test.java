/*
 * $Id: ExtendedInstructionTest.java 520 2007-11-13 19:14:51Z weiju $
 * 
 * Created on 12/08/2005
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
public class InstructionExtV5Test extends InstructionTestBase {

  protected void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
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
    
    mockMachine.expects(once()).method("getPC").will(returnValue(1234));
    mockMachine.expects(once()).method("incrementPC").with(eq(3));
    mockMachine.expects(atLeastOnce()).method("save_undo").will(returnValue(true));
    mockMachine.expects(atLeastOnce()).method("setVariable").with(eq(0), eq((short) 1));
    
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }

  public void testSaveUndoFail() {
    mockMachine.expects(once()).method("getPC").will(returnValue(1234));
    mockMachine.expects(once()).method("incrementPC").with(eq(3));
    mockMachine.expects(atLeastOnce()).method("save_undo").will(returnValue(false));
    mockMachine.expects(atLeastOnce()).method("setVariable").with(eq(0), eq((short) 0));
    
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }
  
  // **************************************************************************
  // ******** ART_SHIFT
  // **********************************
  
  public void testArtShift0() {
    mockMachine.expects(atLeastOnce()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("setVariable").with(eq(1), eq((short) 12));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();
  }
    
  public void testArtShiftPositivePositiveShift() {
    mockMachine.expects(atLeastOnce()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("setVariable").with(eq(1), eq((short) 24));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();    
    
  }    

  public void testArtShiftNegativePositiveShift() {
    mockMachine.expects(atLeastOnce()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("setVariable").with(eq(1), eq((short) -24));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  public void testArtShiftPositiveNegativeShift() {
    mockMachine.expects(atLeastOnce()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("setVariable").with(eq(1), eq((short) 6));
    
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  public void testArtShiftNegativeNegativeShift() {
    mockMachine.expects(atLeastOnce()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("setVariable").with(eq(1), eq((short) -6));
    
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

  // TODO
  
}
