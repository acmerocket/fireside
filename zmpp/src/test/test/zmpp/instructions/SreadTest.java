/*
 * $Id$
 * 
 * Created on 12/02/2005
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

import org.jmock.Mock;
import org.zmpp.encoding.ZsciiEncoding;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;
import org.zmpp.vm.InputFunctions;
import org.zmpp.vm.Tokenizer;

/**
 * This class solely concentrates on the sread instruction which is more
 * complicated.
 * 
 * @author Wei-ju Wu
 * @version 1.0
 */
public class SreadTest extends InstructionTestBase {

  private Mock mockInputFunctions, mockTokenizer;
  private InputFunctions inputFunctions;
  private Tokenizer tokenizer;
  
  protected void setUp() throws Exception {
    
    super.setUp();
    mockInputFunctions = mock(InputFunctions.class);
    inputFunctions = (InputFunctions) mockInputFunctions.proxy();
    mockTokenizer = mock(Tokenizer.class);
    tokenizer = (Tokenizer) mockTokenizer.proxy();
  }
  
  // This is a first template setup for one of the central functions
  // in the Z-machine, the parser.
  public void testSreadVersion3() {
    
    // common things
    mockFileHeader.expects(atLeastOnce()).method("getVersion").will(returnValue(3));
    mockMachine.expects(once()).method("updateStatusLine");
    mockMachine.expects(atLeastOnce()).method("getServices").will(returnValue(services));
    mockServices.expects(atLeastOnce()).method("getInputFunctions").will(returnValue(inputFunctions));
    mockServices.expects(atLeastOnce()).method("getTokenizer").will(returnValue(tokenizer));
    mockInputFunctions.expects(once()).method("readLine").with(eq(4711), eq(0), eq(0)).will(returnValue(ZsciiEncoding.NEWLINE));
    mockTokenizer.expects(once()).method("tokenize").with(eq(4711), eq(5711), eq(0), eq(false));    
    mockMachine.expects(once()).method("getProgramCounter").will(returnValue(7711));
    mockMachine.expects(once()).method("setProgramCounter").with(eq(7716));    
    
    VariableInstruction sread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_SREAD);
    sread.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 4711));
    sread.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 5711));
    sread.setLength(5);
    sread.execute();
  }
}
