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

import org.jmock.Expectations;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.RoutineContext;

import test.zmpp.instructions.Instruction2OpV3Test.Instruction2OpMock;

/**
 * Test class for OP2 instructions on V5.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class Instruction2OpV5Test extends InstructionTestBase {

  @Override
	@Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(5);
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
  @Test
  public void testThrowInvalid() {
    final List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(contexts));
      one (machine).halt("@throw from an invalid stack frame state");
    }});
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    z_throw.execute();
  }

  /**
   * This is the expected situation, in this case we expect that the
   * pop routine context is called as many times until the specified
   * stack frame number is reached and than the function returns with
   * the specified return value.
   */
  @Test
  public void testThrowUnwind() {
    final List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));
    contexts.add(new RoutineContext(3000, 3));
    contexts.add(new RoutineContext(4000, 4));
    contexts.add(new RoutineContext(5000, 5));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(contexts));
      exactly(2).of (machine).returnWith(with(any(char.class)));
    }});
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (char) 2);
    z_throw.execute();
    assertTrue(z_throw.returned);
    assertEquals((short) 42, z_throw.returnValue);
  }  

  private Instruction2OpMock createInstructionMock(int opcode, int typenum1,
  		char value1, int typenum2, char value2) {
  	return Instruction2OpV3Test.createInstructionMock(machine, opcode,
  			typenum1, value1, typenum2, value2);
  }
}
