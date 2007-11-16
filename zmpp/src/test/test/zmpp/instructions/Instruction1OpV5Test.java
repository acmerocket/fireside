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
