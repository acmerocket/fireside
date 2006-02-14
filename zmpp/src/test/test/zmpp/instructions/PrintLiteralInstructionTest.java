/*
 * $Id$
 * 
 * Created on 06.10.2005
 * Copyright 2005 by Wei-ju Wu
 *
 * This file is part of The Z-machine Preservation Project (ZMPP).
 *
 * ZMPP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZMPP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZMPP; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package test.zmpp.instructions;

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
  
  public void testIllegalOpcode() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 221"));
    PrintLiteralInstruction illegal = new PrintLiteralInstruction(
        machine, 0xdd, memoryAccess, 0);
    illegal.execute();
  }
  
  public void testPrint() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(3));
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    mockMemAccess.expects(once()).method("readUnsignedShort").with(eq(4712)).will(returnValue(0x8000));
    PrintLiteralInstruction print = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT, memoryAccess, 4711);
    print.execute();
  }
  
  public void testPrintRet() {
    
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockMachine.expects(once()).method("printZString").with(eq(4712));
    mockMachine.expects(once()).method("newline");
    mockCpu.expects(once()).method("popRoutineContext").with(eq((short) 1));
    
    PrintLiteralInstruction print_ret = new PrintLiteralInstruction(
        machine, PrintLiteralStaticInfo.OP_PRINT_RET, memoryAccess, 4711);
    print_ret.execute();
  }  
}
