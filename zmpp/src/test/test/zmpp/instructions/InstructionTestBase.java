package test.zmpp.instructions;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.zmpp.base.MemoryAccess;
import org.zmpp.io.OutputStream;
import org.zmpp.vm.Dictionary;
import org.zmpp.vm.Machine;
import org.zmpp.vm.MachineServices;
import org.zmpp.vm.ObjectTree;
import org.zmpp.vm.StoryFileHeader;
import org.zmpp.vm.ZObject;

public abstract class InstructionTestBase extends MockObjectTestCase {

  protected Mock mockMachine;
  protected Machine machine;
  protected Mock mockServices;
  protected MachineServices services;
  protected Mock mockOutputStream;
  protected OutputStream outputStream;
  protected Mock mockObjectTree;
  protected ObjectTree objectTree;
  protected Mock mockZObject;
  protected ZObject zobject;
  protected Mock mockMemAccess;
  protected MemoryAccess memoryAccess;
  protected Mock mockFileHeader;
  protected StoryFileHeader storyfileHeader;
  protected Mock mockDictionary;
  protected Dictionary dictionary;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    
    super.setUp();
  
    mockMachine = mock(Machine.class);
    machine = (Machine) mockMachine.proxy();
    mockServices = mock(MachineServices.class);
    services = (MachineServices) mockServices.proxy();
    mockOutputStream = mock(OutputStream.class);
    outputStream = (OutputStream) mockOutputStream.proxy();    
    mockObjectTree = mock(ObjectTree.class);
    objectTree = (ObjectTree) mockObjectTree.proxy();
    mockZObject = mock(ZObject.class);
    zobject = (ZObject) mockZObject.proxy();
    mockMemAccess = mock(MemoryAccess.class);
    memoryAccess = (MemoryAccess) mockMemAccess.proxy();
    mockFileHeader = mock(StoryFileHeader.class);
    storyfileHeader = (StoryFileHeader) mockFileHeader.proxy();
    mockDictionary = mock(Dictionary.class);
    dictionary = (Dictionary) mockDictionary.proxy();    

    
    mockMachine.expects(atLeastOnce()).method("getServices").will(returnValue(services));
    mockServices.expects(atLeastOnce()).method("getStoryFileHeader").will(returnValue(storyfileHeader));
  }
}
