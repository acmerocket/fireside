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
import org.zmpp.instructions.Short0Instruction;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.vm.RoutineContext;

import test.zmpp.instructions.Instruction0OpV3Test.Instruction0OpMock;

/**
 * Test class for OP0 instructions on V5.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class Instruction0OpV5Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
    expectStoryVersion(5);
  }

  @Test
  public void testStoresResult() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_POP); // CATCH
    assertTrue(info.storesResult());
  }
  
  @Test
  public void testSaveIllegalInV5() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 5");
    }});
    Short0Instruction save = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    save.execute();
  }
    
  @Test
  public void testRestoreIllegalInV5() {    
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 6");
    }});
    Short0Instruction restore = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    restore.execute();
  }

  @Test
  public void testShowStatusVersion5IsIllegal() {    
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 12");
    }});
    Instruction0OpMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);
    showstatus.execute();
  }

  @Test
  public void testCatch() {
    final List<RoutineContext> routineContexts = new ArrayList<RoutineContext>();
    routineContexts.add(new RoutineContext(1234, 1));
    routineContexts.add(new RoutineContext(2345, 0));
    routineContexts.add(new RoutineContext(3456, 2));
    context.checking(new Expectations() {{
      one (machine).getRoutineContexts(); will(returnValue(routineContexts));
      one (machine).setVariable(0x12, (short) 2);
    }});
    Instruction0OpMock zcatch = createInstructionMock(Short0StaticInfo.OP_POP);
    zcatch.setStoreVariable((short) 0x12);
    
    zcatch.execute();
    assertTrue(zcatch.nextInstructionCalled);
  }
  
  private Instruction0OpMock createInstructionMock(int opcode) {
    return Instruction0OpV3Test.createInstructionMock(machine, opcode);
  }  
}
