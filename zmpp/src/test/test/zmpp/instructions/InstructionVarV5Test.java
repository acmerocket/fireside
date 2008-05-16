/*
 * $Id: LongInstructionTest.java 524 2007-11-15 00:32:16Z weiju $
 * 
 * Created on 10/04/2005
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

import org.junit.Before;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;

import test.zmpp.instructions.InstructionVarV3Test.VariableInstructionMock;

public class InstructionVarV5Test extends InstructionTestBase {

  @Override
  @Before
  protected void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
  }

  public void testAreadStoresValueInV5() {
    VariableInstruction aread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_AREAD);    
    assertTrue(aread.storesResult());
  }
  
  public void testNotStoresValueInV5() {
    VariableInstruction not5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_NOT);    
    assertTrue(not5.storesResult());
    
    VariableInstruction callvn5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);    
    assertFalse(callvn5.storesResult());
    
    VariableInstruction callvn2_5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);    
    assertFalse(callvn2_5.storesResult());
  }  

  public void testNotInV5() {
    mockMachine.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0x5555));     
    
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);
    not.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xaaaa));
    not.setStoreVariable((short) 0x12);    
    not.execute();
    
    assertTrue(not.nextInstructionCalled);
  }
}
