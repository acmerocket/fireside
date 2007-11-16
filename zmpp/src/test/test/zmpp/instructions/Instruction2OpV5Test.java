package test.zmpp.instructions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;
import org.zmpp.vm.RoutineContext;

import test.zmpp.instructions.Instruction2OpV3Test.Instruction2OpMock;

public class Instruction2OpV5Test extends InstructionTestBase {

	@Before
  public void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(5));
  }

	@Test
  public void testStoresResultV5() {
    LongInstruction info = new LongInstruction(machine, LongStaticInfo.OP_CALL_2N);
    assertFalse(info.storesResult());
  }

  /**
   * We simulate the situation that the current stack is smaller than
   * it could be handled by throw, we should halt the machine, since it
   * is not specified how the machine should behave in this case.
   */
  public void testThrowInvalid() {
    
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getRoutineContexts").will(returnValue(contexts));
    mockCpu.expects(once()).method("halt").with(eq("@throw from an invalid stack frame state"));
    
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    z_throw.execute();
  }

  /**
   * This is the expected situation, in this case we expect that the
   * pop routine context is called as many times until the specified
   * stack frame number is reached and than the function returns with
   * the specified return value.
   */
  public void testThrowUnwind() {
    
    List<RoutineContext> contexts = new ArrayList<RoutineContext>();
    contexts.add(new RoutineContext(1000, 1));
    contexts.add(new RoutineContext(2000, 2));
    contexts.add(new RoutineContext(3000, 3));
    contexts.add(new RoutineContext(4000, 4));
    contexts.add(new RoutineContext(5000, 5));
    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getRoutineContexts").will(returnValue(contexts));
    mockCpu.expects(exactly(2)).method("popRoutineContext").withAnyArguments();
    
    Instruction2OpMock z_throw = createInstructionMock(
        LongStaticInfo.OP_THROW,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 42,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    z_throw.execute();
    assertTrue(z_throw.returned);
    assertEquals((short) 42, z_throw.returnValue);
  }  

  private Instruction2OpMock createInstructionMock(int opcode, int typenum1,
  		short value1, int typenum2, short value2) {
  	return Instruction2OpV3Test.createInstructionMock(machine, opcode,
  			typenum1, value1, typenum2, value2);
  }
}
