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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.RoutineContext;

import test.zmpp.instructions.Instruction2OpV3Test.Instruction2OpMock;

public class Instruction2OpV5Test extends InstructionTestBase {

	@Before
  public void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
  }

	@Test
  public void testStoresResultV5() {
    LongInstruction info = new LongInstruction(machine, LongStaticInfo.OP_CALL_2N);
    assertFalse(info.storesResult());
  }

  /**
   * We simulate the situation that the current stack is smaller than
   * it could be handled by throw, we should halt the machine, since it
   * is not specified how the machine should behave in this case.
   */
  public void testThrowInvalid() {
    
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));
    mockMachine.expects(once()).method("getRoutineContexts").will(returnValue(contexts));
    mockMachine.expects(once()).method("halt").with(eq("@throw from an invalid stack frame state"));
    
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    z_throw.execute();
  }

  /**
   * This is the expected situation, in this case we expect that the
   * pop routine context is called as many times until the specified
   * stack frame number is reached and than the function returns with
   * the specified return value.
   */
  public void testThrowUnwind() {
    
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));
    contexts.add(new RoutineContext(3000, 3));
    contexts.add(new RoutineContext(4000, 4));
    contexts.add(new RoutineContext(5000, 5));
    
    mockMachine.expects(once()).method("getRoutineContexts").will(returnValue(contexts));
    mockMachine.expects(exactly(2)).method("returnWith").withAnyArguments();
    
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    z_throw.execute();
    assertTrue(z_throw.returned);
    assertEquals((short) 42, z_throw.returnValue);
  }  

  private Instruction2OpMock createInstructionMock(int opcode, int typenum1,
  		short value1, int typenum2, short value2) {
  	return Instruction2OpV3Test.createInstructionMock(machine, opcode,
  			typenum1, value1, typenum2, value2);
  }
}
