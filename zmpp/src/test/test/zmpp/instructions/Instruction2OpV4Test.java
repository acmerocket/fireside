package test.zmpp.instructions;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.LongInstruction;
import org.zmpp.instructions.LongStaticInfo;
import org.zmpp.instructions.Operand;

import test.zmpp.instructions.Instruction2OpV3Test.Instruction2OpMock;

public class Instruction2OpV4Test extends InstructionTestBase {

	@Before
  public void setUp() throws Exception {
    super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
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

  public void testCall2s() {
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(4611));
    short[] args = { 2 };
    short returnvalue = 0;
    mockCpu.expects(once()).method("call").with(eq(1), eq(4616), eq(args), eq(returnvalue));
    
    LongInstruction call2s = createInstructionMock(LongStaticInfo.OP_CALL_2S,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 1 ,
        Operand.TYPENUM_SMALL_CONSTANT, (short) 2);
    call2s.execute();
  }  

  // ***********************************************************************
  // ********* CALL_2N
  // ******************************************
  
  public void testCall2nIllegalInVersion4() {
    mockMachine.expects(once()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
        "illegal instruction, type: LONG operand count: C2OP opcode: 26"        
        ));
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
