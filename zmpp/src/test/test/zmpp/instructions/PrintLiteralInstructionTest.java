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

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.PrintLiteralInstruction;
import org.zmpp.instructions.PrintLiteralStaticInfo;

/**
 * This class tests the static and dynamic aspects of the
 * PrintLiteralInstruction.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class PrintLiteralInstructionTest extends InstructionTestBase {

  @Before
  protected void setUp() throws Exception {
	super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
  }

  @Test
  public void testIllegalOpcode() {
    mockMachine.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 221"));
    PrintLiteralInstruction illegal = new PrintLiteralInstruction(
        machine, 0xdd, memory, 0);
    illegal.execute();
  }
  
  @Test
  public void testPrint() {    
    mockMachine.expects(once()).method("incrementPC").with(eq(3));
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    mockMemory.expects(once()).method("readUnsignedShort").with(eq(4712)).will(returnValue(0x8000));
    PrintLiteralInstruction print = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT, memory, 4711);
    print.execute();
  }
  
  @Test
  public void testPrintRet() {
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    mockMachine.expects(once()).method("newline");
    mockMachine.expects(once()).method("returnWith").with(eq((short) 1));
    
    PrintLiteralInstruction print_ret = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT_RET, memory, 4711);
    print_ret.execute();
  }  
}
