package test.zmpp.instructions;

import org.junit.Before;
import org.junit.Test;
import org.zmpp.instructions.Short0Instruction;
import org.zmpp.instructions.Short0StaticInfo;
import org.zmpp.vm.PortableGameState;

import test.zmpp.instructions.Instruction0OpV3Test.Instruction0OpMock;

public class Instruction0OpV4Test extends InstructionTestBase {

  @Before
  protected void setUp() throws Exception {
	  super.setUp();
    mockMachine.expects(atLeastOnce()).method("getVersion").will(returnValue(4));
  }

  @Test
  public void testIsBranch() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);    
    assertFalse(info.isBranch());
    info.setOpcode(Short0StaticInfo.OP_RESTORE);
    assertFalse(info.isBranch());
  }  

  @Test
  public void testStoresResult() {    
    Short0Instruction info;   
    info = new Short0Instruction(machine, Short0StaticInfo.OP_SAVE);
    assertTrue(info.storesResult());
    info = new Short0Instruction(machine, Short0StaticInfo.OP_RESTORE);
    assertTrue(info.storesResult());    
  }

  @Test
  public void testSaveSuccess() {    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("getProgramCounter").will(returnValue(1234));
    mockMachine.expects(once()).method("save").will(returnValue(true));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 1));

    Instruction0OpMock save = createInstructionMock(Short0StaticInfo.OP_SAVE);
    save.execute();
    assertTrue(save.nextInstructionCalled);
  }

  @Test
  public void testRestoreSuccessV4() {
    PortableGameState gamestate = new PortableGameState();
    mockMachine.expects(once()).method("restore").will(returnValue(gamestate));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(5), eq((short) 2));
    
    // Store variable
    mockMachine.expects(atLeastOnce()).method("getGameData")
  		.will(returnValue(gamedata));
    mockGameData.expects(once()).method("getMemory").will(returnValue(memory));
    mockMemory.expects(once()).method("readUnsignedByte").with(eq(0)).will(returnValue((short) 5));
    
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertFalse(restore.nextInstructionCalled);
  }

  @Test
  public void testRestoreFailV4() {
    mockMachine.expects(once()).method("restore").will(returnValue(null));
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("setVariable").with(eq(0), eq((short) 0));
    
    Instruction0OpMock restore = createInstructionMock(Short0StaticInfo.OP_RESTORE);
    restore.execute();
    assertTrue(restore.nextInstructionCalled);
  }

  @Test
  public void testShowStatusVersion4IsIllegal() {    
    mockMachine.expects(atLeastOnce()).method("getCpu").will(returnValue(cpu));
    mockCpu.expects(once()).method("halt").with(eq(
      "illegal instruction, type: SHORT operand count: C0OP opcode: 12"));
    
    Instruction0OpMock showstatus = createInstructionMock(Short0StaticInfo.OP_SHOW_STATUS);
    showstatus.execute();
  }  

  private Instruction0OpMock createInstructionMock(int opcode) {
    return Instruction0OpV3Test.createInstructionMock(machine, opcode);
  }  
}