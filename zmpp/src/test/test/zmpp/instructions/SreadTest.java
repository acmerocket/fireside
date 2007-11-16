/*
 * $Id$
 * 
 * Created on 12/02/2005
 * Copyright 2005-2007 by Wei-ju Wu
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

import org.junit.Test;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;

/**
 * This class solely concentrates on the sread instruction which is more
 * complicated.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class SreadTest extends InstructionTestBase {

  // This is a first template setup for one of the central functions
  // in the Z-machine, the parser.
  @Test
  public void testSreadVersion3() {
    
    // common things
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("updateStatusLine");
    mockMachine.expects(once()).method("readLine").with(eq(4711), eq(0), eq(0)).will(returnValue(ZsciiEncoding.NEWLINE));
    mockMachine.expects(once()).method("tokenize").with(eq(4711), eq(5711), eq(0), eq(false));    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));    
    
    VariableInstruction sread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_SREAD);
    sread.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    sread.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 5711));
    sread.setLength(5);
    sread.execute();
  }
}
