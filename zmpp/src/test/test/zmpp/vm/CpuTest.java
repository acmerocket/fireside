/**
 * $Id$
 */
package test.zmpp.vm;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.vm.Cpu;
import org.zmpp.vm.CpuImpl;
import org.zmpp.vm.GameData;
import org.zmpp.vm.InstructionDecoder;
import org.zmpp.vm.Machine;

public class CpuTest extends MockObjectTestCase {

  private Mock mockMachine, mockDecoder, mockGameData;
  private Machine machine;
  private InstructionDecoder decoder;
  private GameData gamedata;
  private Cpu cpu;
  
  public void setUp() throws Exception {
    

    mockMachine = mock(Machine.class);
    mockDecoder = mock(InstructionDecoder.class);
    mockGameData = mock(GameData.class);
    machine = (Machine) mockMachine.proxy();
    decoder = (InstructionDecoder) mockDecoder.proxy();
    gamedata = (GameData) mockGameData.proxy();
    mockMachine.expects(atLeastOnce()).method("getGameData").will(returnValue(gamedata));
    cpu = new CpuImpl(machine, decoder);
  }
  
  public void testSetProgramCounter() {
   
    cpu.setProgramCounter(1234);
    assertEquals(1234, cpu.getProgramCounter());
  }
  
  public void testIncrementProgramCounter() {
    
    cpu.setProgramCounter(1000);
    cpu.incrementProgramCounter(0);
    assertEquals(1000, cpu.getProgramCounter());
    
    cpu.setProgramCounter(1000);
    cpu.incrementProgramCounter(123);
    assertEquals(1123, cpu.getProgramCounter());
    
    cpu.setProgramCounter(1000);
    cpu.incrementProgramCounter(-32);
    assertEquals(968, cpu.getProgramCounter());
  }
}
