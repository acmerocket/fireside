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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.instructions.ExtendedInstruction;
import org.zmpp.instructions.ExtendedStaticInfo;
import org.zmpp.instructions.Operand;

/**
 * This class tests the dynamic and static aspects of the extended
 * instructions.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class InstructionExtV5Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(5);
  }

  @Test
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
  
  @Test
  public void testSaveUndoSuccess() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).incrementPC(3);
      one (machine).save_undo(with(any(int.class))); will(returnValue(true));
      atLeast(1).of (machine).setVariable(0, (short) 1);
    }});
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }

  @Test
  public void testSaveUndoFail() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).incrementPC(3);
      one (machine).save_undo(with(any(int.class))); will(returnValue(false));
      atLeast(1).of (machine).setVariable(0, (short) 0);
    }});
    ExtendedInstruction save_undo = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_SAVE_UNDO);
    save_undo.setLength(3);
    save_undo.execute();
  }
  
  // **************************************************************************
  // ******** ART_SHIFT
  // **********************************

  @Test
  public void testArtShift0() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      atLeast(1).of (machine).setVariable(1, (short) 12);
    }});
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 0));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();
  }

  @Test
  public void testArtShiftPositivePositiveShift() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      atLeast(1).of (machine).setVariable(1, (short) 24);
    }});
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();    
    
  }    

  @Test
  public void testArtShiftNegativePositiveShift() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      atLeast(1).of (machine).setVariable(1, (short) -24);
    }});
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  @Test
  public void testArtShiftPositiveNegativeShift() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      atLeast(1).of (machine).setVariable(1, (short) 6);
    }});
    ExtendedInstruction art_shift = new ExtendedInstruction(machine, ExtendedStaticInfo.OP_ART_SHIFT);
    art_shift.setLength(3);
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) 12));
    art_shift.addOperand(new Operand(Operand.TYPENUM_SMALL_CONSTANT, (short) -1));
    art_shift.setStoreVariable((short) 0x01);
    art_shift.execute();        
  }    

  @Test
  public void testArtShiftNegativeNegativeShift() {
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      atLeast(1).of (machine).setVariable(1, (short) -6);
    }});
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
