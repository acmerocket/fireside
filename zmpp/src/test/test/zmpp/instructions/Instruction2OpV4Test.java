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
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;

import test.zmpp.instructions.Instruction2OpV3Test.Instruction2OpMock;

/**
 * Test class for OP2 instructions on V4.
 * @author Wei-ju Wu
 * @version 1.5
 */
@RunWith(JMock.class)
public class Instruction2OpV4Test extends InstructionTestBase {

  @Override
	@Before
  public void setUp() throws Exception {
    super.setUp();
    expectStoryVersion(4);
  }
	
  @Test	
  public void testStoresResult() {
    LongInstruction info;
    info = new LongInstruction(machine, LongStaticInfo.OP_OR);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_AND);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_LOADW);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_LOADB);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_PROP);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_PROP_ADDR);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_GET_NEXT_PROP);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_ADD);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_SUB);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_MUL);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_DIV);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_MOD);
    assertTrue(info.storesResult());
    info.setOpcode(LongStaticInfo.OP_CALL_2S);
    assertTrue(info.storesResult());

    // no store
    info.setOpcode(LongStaticInfo.OP_JG);
    assertFalse(info.storesResult());
  }

  @Test
  public void testCall2s() {
    final short[] args = { 2 };
    final int returnvalue = 0;
    context.checking(new Expectations() {{
      one (machine).getPC(); will(returnValue(4611));
      one (machine).call(1, 4616, args, returnvalue);
    }});
    LongInstruction call2s = createInstructionMock(LongStaticInfo.OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2s.execute();
  }  

  // ***********************************************************************
  // ********* CALL_2N
  // ******************************************
  
  @Test
  public void testCall2nIllegalInVersion4() {
    context.checking(new Expectations() {{
      one (machine).halt("illegal instruction, type: LONG operand count: C2OP opcode: 26");
    }});
    LongInstruction call2n = createInstructionMock(LongStaticInfo.OP_CALL_2N,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2n.execute();
  }  

  private Instruction2OpMock createInstructionMock(int opcode, int typenum1,
  		short value1, int typenum2, short value2) {
  	return Instruction2OpV3Test.createInstructionMock(machine, opcode,
  			typenum1, value1, typenum2, value2);
  }
}
