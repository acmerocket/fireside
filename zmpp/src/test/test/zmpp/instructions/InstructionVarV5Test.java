package test.zmpp.instructions;

import org.junit.Before;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;

import test.zmpp.instructions.InstructionVarV3Test.VariableInstructionMock;

public class InstructionVarV5Test extends InstructionTestBase {

  @Before
  protected void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
  }

  public void testAreadStoresValueInV5() {
    VariableInstruction aread = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_AREAD);    
    assertTrue(aread.storesResult());
  }
  
  public void testNotStoresValueInV5() {
    VariableInstruction not5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_NOT);    
    assertTrue(not5.storesResult());
    
    VariableInstruction callvn5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN);    
    assertFalse(callvn5.storesResult());
    
    VariableInstruction callvn2_5 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_CALL_VN2);    
    assertFalse(callvn2_5.storesResult());
  }  

  public void testNotInV5() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0x12), eq((short) 0x5555));     
    
    VariableInstructionMock not =
      new VariableInstructionMock(machine, VariableStaticInfo.OP_NOT);
    not.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0xaaaa));
    not.setStoreVariable((short) 0x12);    
    not.execute();
    
    assertTrue(not.nextInstructionCalled);
  }
}
