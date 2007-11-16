package test.zmpp.instructions;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.Operand;
import org.zmpp.instructions.VariableInstruction;
import org.zmpp.instructions.VariableStaticInfo;
import org.zmpp.instructions.AbstractInstruction.OperandCount;

public class InstructionVarV6Test extends InstructionTestBase {

  @Before
  protected void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(6));
  }

  @Test
  public void testPullStoresValueInV6() {
    VariableInstruction pull6 = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);    
    assertTrue(pull6.storesResult());
  }
  
  @Test
  public void testPullV6NoUserStack() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getVariable").with(eq(0x00)).will(returnValue((short) 0x14));;
    
    mockCpu.expects(once()).method("setVariable").with(eq(0x15), eq((short) 0x14));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.setStoreVariable((short) 0x15);
    pull.setLength(5);
    pull.execute();
  }  

  public void testPullV6UserStack() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("popUserStack").with(eq(0x1234)).will(returnValue((short) 0x15));    
    mockCpu.expects(once()).method("setVariable").with(eq(0x15), eq((short) 0x15));
    mockCpu.expects(once()).method("incrementProgramCounter").with(eq(5));

    VariableInstruction pull = new VariableInstruction(machine,
        OperandCount.VAR, VariableStaticInfo.OP_PULL);
    pull.addOperand(new Operand(Operand.TYPENUM_LARGE_CONSTANT, (short) 0x1234));
    pull.setStoreVariable((short) 0x15);
    pull.setLength(5);
    pull.execute();
  }  
  
}
