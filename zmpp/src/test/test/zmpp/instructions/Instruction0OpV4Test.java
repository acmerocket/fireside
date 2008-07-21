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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.zmpp.instructions.Short0Instruction;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.vm.PortableGameState;

import test.zmpp.instructions.Instruction0OpV3Test.Instruction0OpMock;

/**
 * Test class for OP0 instructions under V4.
 * @author Wei-ju Wu
 * @version 1.5
 */
public class Instruction0OpV4Test extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
	  super.setUp();
    expectStoryVersion(4);
  }

  @Test
  public void testIsBranch() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);    
    assertFalse(info.isBranch());
    info.setOpcode(Short0StaticInfo.OP_RESTORE);
    assertFalse(info.isBranch());
  }  

  @Test
  public void testStoresResult() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    assertTrue(info.storesResult());
    info = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    assertTrue(info.storesResult());    
  }

  @Test
  public void testSaveSuccess() {
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(1234));
      one (machine).save(with(any(int.class))); will(returnValue(true));
      one (machine).setVariable(0, (char) 1);
    }});
    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.nextInstructionCalled);
  }

  @Test
  public void testRestoreSuccessV4() {
    final PortableGameState gamestate = new PortableGameState();
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(gamestate));
      // Store variable
      one (machine).setVariable(5, (char) 2);
      one (machine).readUnsigned8(0); will(returnValue((char) 5));
    }});
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFailV4() {
    context.checking(new Expectations() {{
      one (machine).restore(); will(returnValue(null));
      one (machine).setVariable(0, (char) 0);
    }});
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  @Test
  public void testShowStatusVersion4IsIllegal() {    
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 12");
    }});
    Instruction0OpMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);
    showstatus.execute();
  }  

  private Instruction0OpMock createInstructionMock(int opcode) {
    return Instruction0OpV3Test.createInstructionMock(machine, opcode);
  }  
}
