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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;

import org.zmpp.vm.Instruction.OperandCount;
import test.zmpp.instructions.InstructionVarV3Test.VariableInstructionMock;

/**
 * Test class for VAR instructions on V5.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class InstructionVarV5Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(5);
  }

  @Test
  public void testAreadStoresValueInV5() {
    VariableInstruction aread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_AREAD);    
    assertTrue(aread.storesResult());
  }
  
  @Test
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

  @Test
  public void testNotInV5() {
    context.checking(new Expectations() {{
      one (machine).setVariable((char) 0x12, (char) 0x5555);
    }});
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);
    not.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (char) 0xaaaa));
    not.setStoreVariable((char) 0x12);    
    not.execute();    
    assertTrue(not.nextInstructionCalled);
  }
}
