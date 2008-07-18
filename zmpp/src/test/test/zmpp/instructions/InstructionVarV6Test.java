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
import org.zmpp.instructions.AbstractInstruction.OperandCount;

/**
 * Test class for VAR instruction on V6.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class InstructionVarV6Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(6);
  }

  @Test
  public void testPullStoresValueInV6() {
    VariableInstruction pull6 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);    
    assertTrue(pull6.storesResult());
  }
  
  @Test
  public void testPullV6NoUserStack() {
    context.checking(new Expectations() {{
      one (machine).popStack(0x00); will(returnValue((short) 0x14));
      one (machine).setVariable(0x15, (short) 0x14);
      one (machine).incrementPC(5);
    }});
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.setStoreVariable((short) 0x15);
    pull.setLength(5);
    pull.execute();
  }  

  @Test
  public void testPullV6UserStack() {
    context.checking(new Expectations() {{
      one (machine).popStack(0x1234); will(returnValue((short) 0x15));
      one (machine).setVariable(0x15, (short) 0x15);
      one (machine).incrementPC(5);
    }});
    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1234));
    pull.setStoreVariable((short) 0x15);
    pull.setLength(5);
    pull.execute();
  }  
  
}
