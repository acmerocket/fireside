/*
 * $Id$
 * 
 * Created on 10/06/2005
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
import org.junit.runner.RunWith;
import org.zmpp.instructions.PrintLiteralInstruction;
import org.zmpp.instructions.PrintLiteralStaticInfo;

/**
 * This class tests the static and dynamic aspects of the
 * PrintLiteralInstruction.
 * 
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class PrintLiteralInstructionTest extends InstructionTestBase {

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(5);
  }

  @Test
  public void testIllegalOpcode() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: SHORT operand count: C0OP opcode: 221");
    }});
    PrintLiteralInstruction illegal = new PrintLiteralInstruction(
        machine, 0xdd, machine, 0);
    illegal.execute();
  }
  
  @Test
  public void testPrint() {    
    context.checking(new Expectations() {{
      one (machine).incrementPC(3);
      one (machine).printZString(4712);
      one (machine).readUnsigned16(4712); will(returnValue((char) 0x8000));
    }});
    PrintLiteralInstruction print = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT, machine, 4711);
    print.execute();
  }
  
  @Test
  public void testPrintRet() {
    context.checking(new Expectations() {{
      one (machine).printZString(4712);
      one (machine).newline();
      one (machine).returnWith((short) 1);
    }});
    PrintLiteralInstruction print_ret = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT_RET, machine, 4711);
    print_ret.execute();
  }  
}
