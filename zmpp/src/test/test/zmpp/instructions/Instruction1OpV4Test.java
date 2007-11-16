package test.zmpp.instructions;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.Short1Instruction;
import org.zmpp.instructions.Short1StaticInfo;

import test.zmpp.instructions.Instruction1OpV3Test.Instruction1OpMock;

/**
 * Test class for V4-specific 1OP instruction behavior.
 * @author Wei-ju Wu
 * @version 1.0
 */
public class Instruction1OpV4Test extends InstructionTestBase {

	@Before
  protected void setUp() throws Exception {
	  super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion")
    	.will(returnValue(4));
  }
	
	@Test
	public void testStoresResult() {
		Short1Instruction info;
		info = new Short1Instruction(machine, Short1StaticInfo.OP_GET_SIBLING);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_GET_CHILD);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_GET_PARENT);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_GET_PROP_LEN);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_LOAD);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_NOT);
		assertTrue(info.storesResult());
		info.setOpcode(Short1StaticInfo.OP_CALL_1S);
		assertTrue(info.storesResult());

		// no store
		info.setOpcode(Short1StaticInfo.OP_DEC);
		assertFalse(info.storesResult());    
	}

  @Test
  public void testIsBranch() {    
    Short1Instruction info;
    info = new Short1Instruction(machine, Short1StaticInfo.OP_JZ);    
    assertTrue(info.isBranch());
    info.setOpcode(Short1StaticInfo.OP_GET_SIBLING);
    assertTrue(info.isBranch());
    info.setOpcode(Short1StaticInfo.OP_GET_CHILD);
    assertTrue(info.isBranch());
    
    // no branch
    info.setOpcode(Short1StaticInfo.OP_GET_PARENT);
    assertFalse(info.isBranch());    
  }  

  // ***********************************************************************
  // ********* NOT
  // ******************************************  
  
  @Test
  public void testNot() {
    mockMachine.expects(atLeastOnce()).method("getCpu")
    	.will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable")
    	.with(eq(0x12), eq((short) 0x5555));     
    
	  // Create instruction	  
	  Instruction1OpMock not = createInstructionMock(Short1StaticInfo.OP_NOT,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 0xaaaa);      
    not.setStoreVariable((short) 0x12);
	  not.execute();
	  assertTrue(not.nextInstructionCalled);
  }  

  // ***********************************************************************
  // ********* CALL_1S
  // ******************************************
  
  public void testCall1s() {
    short[] args = {};
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(4611));
    mockCpu.expects(once()).method("call")
    	.with(eq(4611), eq(4623), eq(args), eq((short) 0));
    
    Short1Instruction call1s = createInstructionMock(
    		Short1StaticInfo.OP_CALL_1S,
        Operand.TYPENUM_LARGE_CONSTANT, (short) 4611);
    call1s.execute();
  }

  private Instruction1OpMock createInstructionMock(int opcode, int typenum,
  		short value) {
  	return Instruction1OpV3Test.createInstructionMock(machine, opcode,
  			typenum, value);
  }
}
