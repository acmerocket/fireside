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

import org.junit.Test;
import org.zmpp.instructions.Short1Instruction;
import org.zmpp.instructions.Short1StaticInfo;

/**
 * Test class for V5-specific 1OP instruction behavior.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Instruction1OpV5Test extends InstructionTestBase {

  protected void setUp() throws Exception {
	  super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion")
    	.will(returnValue(5));
  }

  @Test
  public void testStoresResultV5() {    
    Short1Instruction info = new Short1Instruction(machine, Short1StaticInfo.OP_CALL_1N);
    assertFalse(info.storesResult());
  }  
}
